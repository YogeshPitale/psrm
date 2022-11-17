package com.wf.psrm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wf.psrm.consumer.WireDetailsEventsConsumer;
import com.wf.psrm.domain.CustomRules;
import com.wf.psrm.domain.RiskMonitor;
import com.wf.psrm.domain.RiskMonitorCalculator;
import com.wf.psrm.domain.RulesList;
import com.wf.psrm.domain.WireDetailsEvent;
import com.wf.psrm.jpa.RiskMonitorRepository;
import com.wf.psrm.jpa.WireDetailsEventsRepository;
import com.wf.psrm.service.WireDetailsEventsService;
import com.wf.psrm.util.FeignServiceUtil;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@EmbeddedKafka(topics = { "events" }, partitions = 2)
@TestPropertySource(properties = { "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
		"spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}" })
public class WireDetailsEventsServiceTest {

	@Autowired
	EmbeddedKafkaBroker embeddedKafkaBroker;

	@Autowired
	KafkaTemplate<Integer, String> kafkaTemplate;

	@Autowired
	KafkaListenerEndpointRegistry endpointRegistry;

	@SpyBean
	WireDetailsEventsConsumer wireDetailsEventsConsumer;

	@SpyBean
	WireDetailsEventsService wireDetailsEventsService;

	@MockBean
	FeignServiceUtil feignServiceUtil;

	@Autowired
	WireDetailsEventsRepository wireDetailsEventsRepository;

	@Autowired
	RiskMonitorRepository riskMonitorRepository;

	@Autowired
	ObjectMapper mapper;

	@BeforeEach
	void setUp() {

		for (MessageListenerContainer messageListenerContainer : endpointRegistry.getListenerContainers()) {
			ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
		}

		wireDetailsEventsService.kickOffTheDay(231090234, 65236000);
	}

	@AfterEach
	void tearDown() {
		wireDetailsEventsRepository.deleteAll();
		riskMonitorRepository.deleteAll();
		wireDetailsEventsService.reset();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCalculations() throws ExecutionException, InterruptedException, IOException {

		// given
		String json[] = new String[4];

		WireDetailsEvent event1 = WireDetailsEvent.builder().appId("PMTS").amt(282493.5).ccy("USD").pmtRail("CEO")
				.nm("ABC").payeeiswells("Y").payoriswells("N").evtDtTm("2022-02-16T04:03:19").build();
		json[0] = mapper.writeValueAsString(event1);

		WireDetailsEvent event2 = WireDetailsEvent.builder().appId("PMTS").amt(282493.5).ccy("USD").pmtRail("CEO")
				.nm("ABC").payeeiswells("Y").payoriswells("N").evtDtTm("2022-02-16T04:03:19").build();
		json[1] = mapper.writeValueAsString(event2);

		WireDetailsEvent event3 = WireDetailsEvent.builder().appId("PMTS").amt(726837.5).ccy("USD").pmtRail("CEO")
				.nm("ABC").payeeiswells("N").payoriswells("Y").evtDtTm("2022-02-16T04:03:19").build();
		json[2] = mapper.writeValueAsString(event3);

		WireDetailsEvent event4 = WireDetailsEvent.builder().appId("PMTS").amt(726837.5).ccy("USD").pmtRail("CEO")
				.nm("ABC").payeeiswells("N").payoriswells("Y").evtDtTm("2022-02-16T04:03:19").build();
		json[3] = mapper.writeValueAsString(event4);

		for (int i = 0; i < 4; i++) {
			kafkaTemplate.sendDefault(json[i]).get();
//			Thread.sleep(500);
		}

		// when
		when(feignServiceUtil.getRulesList(Mockito.any()))
				.thenReturn(ResponseEntity.status(HttpStatus.OK).body(new RulesList(false, new ArrayList<>())));

		CountDownLatch latch = new CountDownLatch(1);
		latch.await(3, TimeUnit.SECONDS);

		// then
		verify(wireDetailsEventsConsumer, times(4)).onMessage(isA(ConsumerRecord.class));
		verify(wireDetailsEventsService, times(4)).processWireDetailsEvent(isA(ConsumerRecord.class));

		List<WireDetailsEvent> wireDetailsEvents = (List<WireDetailsEvent>) wireDetailsEventsRepository.findAll();
		List<RiskMonitor> riskMonitors = (List<RiskMonitor>) riskMonitorRepository.findAll();
		assertEquals(4, wireDetailsEvents.size());
		assertEquals(4, riskMonitors.size());

		RiskMonitorCalculator riskMonitorCalculator = (RiskMonitorCalculator) ReflectionTestUtils
				.getField(wireDetailsEventsService, "c1");

		assertEquals(230201546, riskMonitorCalculator.getCurrentPosition());
		assertEquals(-888688, riskMonitorCalculator.getNetFedWirePosition());
		assertEquals(564987, riskMonitorCalculator.getFedwireCredits());
		assertEquals(1453675, riskMonitorCalculator.getFedwireDebits());
		assertEquals(6523600, riskMonitorCalculator.getSafetyfactor());
		assertEquals(288913946, riskMonitorCalculator.getMaxAvailable());
	}

	@Test
	void resetTest() {

		assertTrue(wireDetailsEventsService.setThrottle(true));
		assertEquals(900000, wireDetailsEventsService.setAmount(900000));
		assertEquals((float) 0.5, wireDetailsEventsService.setThrottleMaxAvailable((float) 50));

		wireDetailsEventsService.reset();

		assertEquals(new RiskMonitorCalculator(), ReflectionTestUtils.getField(wireDetailsEventsService, "c1"));
		assertEquals(800000, ReflectionTestUtils.getField(wireDetailsEventsService, "dynamicAmount"));
		assertEquals(false, ReflectionTestUtils.getField(wireDetailsEventsService, "throttleValue"));
		assertEquals((float) 0.3, ReflectionTestUtils.getField(wireDetailsEventsService, "throttleMaxAvailable"));
	}

	@Test
	public void testFeignClient() {

		when(feignServiceUtil.getRulesList(Mockito.any()))
				.thenReturn(ResponseEntity.status(HttpStatus.OK).body(new RulesList(false, new ArrayList<>())));

		assertEquals(new RulesList(false, new ArrayList<>()),
				feignServiceUtil.getRulesList(new CustomRules()).getBody());
	}
}

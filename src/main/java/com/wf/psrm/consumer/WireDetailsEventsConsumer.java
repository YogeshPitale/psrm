package com.wf.psrm.consumer;

import java.io.IOException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wf.psrm.domain.RiskMonitorCalculator;
import com.wf.psrm.domain.WireDetailsEvent;
import com.wf.psrm.service.WireDetailsEventsService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@CrossOrigin
@RequestMapping
public class WireDetailsEventsConsumer {

	@Autowired
	private WireDetailsEventsService wireDetailsEventsService;

	RiskMonitorCalculator latestRISKInstance;

	@Autowired
	ObjectMapper objectMapper;

	SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

	@KafkaListener(topics = { "events" })
	public void onMessage(ConsumerRecord<String, String> consumerRecord) throws IOException {

		try {
			log.info("ConsumerRecord : {} ", consumerRecord);
			latestRISKInstance = wireDetailsEventsService.processWireDetailsEvent(consumerRecord);
			WireDetailsEvent wireDetailsEvent = objectMapper.readValue(consumerRecord.value(), WireDetailsEvent.class);
			latestRISKInstance.setTimeStamp(wireDetailsEvent.getEvtDtTm());
			emitter.send(latestRISKInstance);
			log.info("Sent latestRisk Instance to UI with current position"+latestRISKInstance.getCurrentPosition());
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	@GetMapping("/emitter")
	public SseEmitter eventEmitter() throws IOException, InterruptedException {

		emitter.onTimeout(() -> {
			emitter.complete();
		});
		return emitter;
	}
}

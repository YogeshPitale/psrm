package com.wf.psrm.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wf.psrm.domain.CustomRules;
import com.wf.psrm.domain.ReactiveRiskMonitor;
import com.wf.psrm.domain.RiskMonitor;
import com.wf.psrm.domain.RiskMonitorCalculator;
import com.wf.psrm.domain.RiskMonitorMoney;
import com.wf.psrm.domain.RulesList;
import com.wf.psrm.domain.WireDetailsEvent;
import com.wf.psrm.jpa.RiskMonitorReactiveRepository;
import com.wf.psrm.jpa.RiskMonitorRepository;
import com.wf.psrm.jpa.WireDetailsEventsRepository;
import com.wf.psrm.util.FeignServiceUtil;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unused")
@Service
@Slf4j
public class WireDetailsEventsService {

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	KafkaTemplate<String, String> kafkaTemplate;

	@Autowired
	private WireDetailsEventsRepository wireDetailsEventsRepository;

	@Autowired
	private RiskMonitorRepository riskMonitorRepository;

	@Autowired
	private RiskMonitorReactiveRepository reactiveRepository;

	@Autowired
	RiskMonitorCalculator c1;

	RiskMonitor rM;

	RiskMonitorMoney riskMonitorMoney;

	private static Integer dynamicAmount = 800000;

	private static Boolean throttleValue = false;

	private static float throttleMaxAvailable = (float) 0.3;

//	@Autowired
//	private KieContainer kieContainer;

	@Autowired
	private FeignServiceUtil feignServiceUtil;

	@Autowired
	private SequenceGeneratorService sequenceGenerator;

	public RiskMonitorCalculator processWireDetailsEvent(ConsumerRecord<String, String> consumerRecord)
			throws IOException {

		WireDetailsEvent wireDetailsEvent = objectMapper.readValue(consumerRecord.value(), WireDetailsEvent.class);
		log.info("wireDetailsEvent : {} ", wireDetailsEvent);

		wireDetailsEvent.setId(sequenceGenerator.generateSequence(WireDetailsEvent.SEQUENCE_NAME));
		save(wireDetailsEvent);

		RiskMonitor tempMonitor = new RiskMonitor(rM);
		tempMonitor.setId(wireDetailsEvent.getId());
		tempMonitor.setTimeStamp(wireDetailsEvent.getEvtDtTm());
		tempMonitor.setNm(wireDetailsEvent.getNm());
		if (wireDetailsEvent.getPayeeiswells().equals("Y") && wireDetailsEvent.getPayoriswells().equals("N")) {
			tempMonitor.setCreditAmt(wireDetailsEvent.getAmt());
			tempMonitor.setDebitAmt(-1);
			tempMonitor.setStatus("Received");
			rM.addCredit(wireDetailsEvent.getAmt());
			rM.calculate();
			riskMonitorMoney.addCredit(wireDetailsEvent.getAmt());
			riskMonitorMoney.calculate();
		} else {
			tempMonitor.setCreditAmt(-1);
			tempMonitor.setDebitAmt(wireDetailsEvent.getAmt());

			CustomRules customRules = new CustomRules(wireDetailsEvent.getNm(), wireDetailsEvent.getPmtRail(),
					throttleValue, dynamicAmount, throttleMaxAvailable, tempMonitor.getDebitAmt(), rM.getCap());
//			RulesList rulesList = getList(customRules);

			ResponseEntity<Object> responseEntity = feignServiceUtil.getRulesList(customRules);

			ObjectMapper mapper = new ObjectMapper();
			RulesList rulesList = mapper.readValue(mapper.writeValueAsString(responseEntity.getBody()),
					RulesList.class);

			if (rulesList.getStatusOnHold()) {
				tempMonitor.setReasonForHold(rulesList.getList().toString());
				log.info(tempMonitor.getReasonForHold());
				tempMonitor.setStatus("On Hold");
				rM.setOnHoldCount();
				log.info("Transaction On Hold");
			} else {
				tempMonitor.setStatus("Released");
				rM.addDebit(wireDetailsEvent.getAmt());
				rM.calculate();
				riskMonitorMoney.addDebit(wireDetailsEvent.getAmt());
				riskMonitorMoney.calculate();
			}
		}
		tempMonitor.setPmtRail(wireDetailsEvent.getPmtRail());
		tempMonitor.setTimeStamp(wireDetailsEvent.getEvtDtTm());
		save(tempMonitor);

//		ReactiveRiskMonitor reactiveRiskMonitor = new ReactiveRiskMonitor(tempMonitor);
		ObjectMapper mapper = new ObjectMapper();
		ReactiveRiskMonitor reactiveRiskMonitor = mapper.readValue(mapper.writeValueAsString(tempMonitor), ReactiveRiskMonitor.class);
		save(reactiveRiskMonitor);
//		save(rM);

//		c1.update(rM);
		log.info("rM : " + rM);
		log.info("riskMonitorMoney : " + riskMonitorMoney);
		c1.update(riskMonitorMoney);

		log.info(c1.toString());
		return c1;
	}

	private void save(RiskMonitor rM) {
//		riskMonitorRepository.save(rM).subscribe(result -> log.info("RiskMonitor has been saved: {}", result));
		riskMonitorRepository.save(rM);
		log.info("Successfully Persisted the RiskMonitor {} ", rM);
	}

	private void save(WireDetailsEvent wireDetailsEvent) {
//		wireDetailsEventsRepository.save(wireDetailsEvent).subscribe(result -> log.info("wireDetailsEvent has been saved: {}", result));
		wireDetailsEventsRepository.save(wireDetailsEvent);
		log.info("Successfully Persisted the Event {} ", wireDetailsEvent);
	}

	private void save(ReactiveRiskMonitor reactiveRiskMonitor) {
		reactiveRepository.save(reactiveRiskMonitor).subscribe(result -> log.info("ReactiveRiskMonitor has been saved: {}", result));
//		log.info("Successfully Persisted the ReactiveRiskMonitor {} ", reactiveRiskMonitor);
	}

	public void handleRecovery(ConsumerRecord<String, String> record) {

		String key = record.key();
		String message = record.value();

		ListenableFuture<SendResult<String, String>> listenableFuture = kafkaTemplate.sendDefault(key, message);
		listenableFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
			@Override
			public void onFailure(Throwable ex) {
				handleFailure(key, message, ex);
			}

			@Override
			public void onSuccess(SendResult<String, String> result) {
				handleSuccess(key, message, result);
			}
		});
	}

	private void handleFailure(String key, String value, Throwable ex) {
		log.error("Error Sending the Message and the exception is {}", ex.getMessage());
		try {
			throw ex;
		} catch (Throwable throwable) {
			log.error("Error in OnFailure: {}", throwable.getMessage());
		}
	}

	private void handleSuccess(String key, String value, SendResult<String, String> result) {
		log.info("Message Sent SuccessFully for the key : {} and the value is {} , partition is {}", key, value,
				result.getRecordMetadata().partition());
	}

	public void kickOffTheDay(double initialBalance, double cap) {
		c1.setCap(cap);
		c1.setInitialBalance(initialBalance);
		rM = new RiskMonitor(c1);
		riskMonitorMoney = new RiskMonitorMoney(c1);
		rM.calculate();
		log.info("RiskMonitor initialize to cap:" + cap + " Initial balance" + initialBalance);
		// riskMonitorMoney = new RiskMonitorMoney(c1);

	}

	public List<RiskMonitor> getAllRiskMonitor() {
		return (List<RiskMonitor>) riskMonitorRepository.findAll();
	}

	public int getCount() {
		if (rM == null) {
			log.info("Initial count 0");
			return 0;
		}
		log.info("Returning count" + rM.getOnHoldCount());
		return rM.getOnHoldCount();
	}

	public Boolean setThrottle(Boolean throttleValue) {
		WireDetailsEventsService.throttleValue = throttleValue;
		log.info("throttleValue: " + WireDetailsEventsService.throttleValue);
		return WireDetailsEventsService.throttleValue;
	}

	public Integer setAmount(Integer amount) {
		WireDetailsEventsService.dynamicAmount = amount;
		log.info("dynamicAmount: " + WireDetailsEventsService.dynamicAmount);
		return WireDetailsEventsService.dynamicAmount;
	}

	public Float setThrottleMaxAvailable(Float throttleMaxAvailable) {
		WireDetailsEventsService.throttleMaxAvailable = throttleMaxAvailable / 100;
		log.info("throttleMaxAvailable: " + WireDetailsEventsService.throttleMaxAvailable);
		return WireDetailsEventsService.throttleMaxAvailable;
	}

	public void reset() {
		riskMonitorRepository.deleteAll();
		c1 = new RiskMonitorCalculator();
		dynamicAmount = 800000;
		throttleValue = false;
		throttleMaxAvailable = (float) 0.3;
	}

//	public RulesList getList(CustomRules cRules) {
//		RulesList rulesList = new RulesList();
//		KieSession kieSession = kieContainer.newKieSession();
//		kieSession.setGlobal("rulesList", rulesList);
//		kieSession.insert(cRules);
//		kieSession.fireAllRules();
//		kieSession.dispose();
//		return rulesList;
//	}
}

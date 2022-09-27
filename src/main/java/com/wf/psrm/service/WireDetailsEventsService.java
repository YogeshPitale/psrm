package com.wf.psrm.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

//import com.wf.psrm.domain.RiskMonitorMoney;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wf.psrm.controller.PsrmController;
import com.wf.psrm.domain.RiskMonitor;
import com.wf.psrm.domain.RiskMonitorCalculator;
import com.wf.psrm.domain.WireDetailsEvent;
import com.wf.psrm.jpa.RiskMonitorRepository;
import com.wf.psrm.jpa.WireDetailsEventsRepository;

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
	RiskMonitorCalculator c1;

	RiskMonitor rM;

	private static Integer dynamicAmount = 800000;

	private static Boolean throttleValue = false;

//	RiskMonitorMoney riskMonitorMoney;

	public RiskMonitorCalculator processWireDetailsEvent(ConsumerRecord<String, String> consumerRecord)
			throws IOException {

		WireDetailsEvent wireDetailsEvent = objectMapper.readValue(consumerRecord.value(), WireDetailsEvent.class);
		log.info("wireDetailsEvent : {} ", wireDetailsEvent);
		if (wireDetailsEvent.getPayeeiswells().equals("Y") && wireDetailsEvent.getPayoriswells().equals("N")) {
			rM.addCredit(wireDetailsEvent.getAmt());
			// riskMonitorMoney.addCredit(wireDetailsEvent.getAmt());
		} else {
			rM.addDebit(wireDetailsEvent.getAmt());
			// riskMonitorMoney.addDebit(wireDetailsEvent.getAmt());
		}
		rM.calculate();
		// riskMonitorMoney.calculate();
		save(wireDetailsEvent);

		RiskMonitor tempMonitor = new RiskMonitor(rM);
		tempMonitor.setTimeStamp(wireDetailsEvent.getEvtDtTm());
		tempMonitor.setNm(wireDetailsEvent.getNm());
		if (wireDetailsEvent.getPayeeiswells().equals("Y") && wireDetailsEvent.getPayoriswells().equals("N")) {
			tempMonitor.setCreditAmt(wireDetailsEvent.getAmt());
			tempMonitor.setDebitAmt(-1);
			tempMonitor.setStatus("Released");
		} else {
			tempMonitor.setCreditAmt(-1);
			tempMonitor.setDebitAmt(wireDetailsEvent.getAmt());
			if (throttleValue || wireDetailsEvent.getNm().equalsIgnoreCase("CITI")
					|| wireDetailsEvent.getPmtRail().equalsIgnoreCase("RTL") || tempMonitor.getDebitAmt() > dynamicAmount) {
				tempMonitor.setStatus("On Hold");
				rM.setOnHoldCount();
				log.info("Transaction On Hold");
			} else {
				tempMonitor.setStatus("Released");
			}
		}
		tempMonitor.setPmtRail(wireDetailsEvent.getPmtRail());
		save(tempMonitor);
//		save(rM);

		c1.update(rM);
		// c1.update(riskMonitorMoney);
		log.info(c1.toString());
		return c1;
	}

	private void save(RiskMonitor rM) {
		riskMonitorRepository.save(rM);
		log.info("Successfully Persisted the RiskMonitor {} ", rM);
	}

	private void save(WireDetailsEvent wireDetailsEvent) {
		wireDetailsEventsRepository.save(wireDetailsEvent);
		log.info("Successfully Persisted the Event {} ", wireDetailsEvent);
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
		log.info("Rismonitor initialize to cap:" + cap + " Initial balance" + initialBalance);
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
}

package com.wf.psrm.service;

import java.io.IOException;
import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.dao.RecoverableDataAccessException;
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

	public RiskMonitorCalculator processWireDetailsEvent(ConsumerRecord<String, String> consumerRecord)
			throws IOException {

		WireDetailsEvent wireDetailsEvent = objectMapper.readValue(consumerRecord.value(), WireDetailsEvent.class);
		log.info("wireDetailsEvent : {} ", wireDetailsEvent);
		if (wireDetailsEvent.getPayeeiswells().equals("Y") && wireDetailsEvent.getPayoriswells().equals("N")) {
			rM.addCredit(wireDetailsEvent.getAmt());
		} else {
			rM.addDebit(wireDetailsEvent.getAmt());
		}
		rM.calculate();
		save(wireDetailsEvent);
		save(rM);
		c1.update(rM);
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
		rM=new RiskMonitor(c1);
	}
}

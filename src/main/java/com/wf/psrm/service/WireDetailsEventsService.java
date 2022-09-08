package com.wf.psrm.service;

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

	public void processWireDetailsEvent(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {

		WireDetailsEvent wireDetailsEvent = objectMapper.readValue(consumerRecord.value(), WireDetailsEvent.class);
		log.info("wireDetailsEvent : {} ", wireDetailsEvent);

//		if (wireDetailsEvent.getWireDetailsEventId() != null && wireDetailsEvent.getWireDetailsEventId() == 000) {
//			throw new RecoverableDataAccessException("Temporary Network Issue");
//		}
//
//		switch (wireDetailsEvent.getWireDetailsEventType()) {
//		case NEW:
//			save(wireDetailsEvent);
//			break;
//		case UPDATE:
//			// validate the libraryevent
//			validate(wireDetailsEvent);
//			save(wireDetailsEvent);
//			break;
//		default:
//			log.info("Invalid Library Event Type");
//		}

		RiskMonitor rM = new RiskMonitor(c1);

		if (wireDetailsEvent.getPayeeiswells().equals("Y") && wireDetailsEvent.getPayoriswells().equals("N")) {
			rM.addCredit(wireDetailsEvent.getAmt());
		} else {
			rM.addDebit(wireDetailsEvent.getAmt());
		}
		rM.calculate();

		save(wireDetailsEvent);

//		RiskMonitor c2 = new RiskMonitor(c1);
		save(rM);
		
		c1.update(rM);

	}

//	private void validate(WireDetailsEvent wireDetailsEvent) {
//		if (wireDetailsEvent.getWireDetailsEventId() == null) {
//			throw new IllegalArgumentException("Library Event Id is missing");
//		}
//
//		Optional<WireDetailsEvent> wireDetailsEventOptional = wireDetailsEventsRepository
//				.findById(wireDetailsEvent.getWireDetailsEventId());
//		if (!wireDetailsEventOptional.isPresent()) {
//			throw new IllegalArgumentException("Not a valid library Event");
//		}
//		log.info("Validation is successful for the library Event : {} ", wireDetailsEventOptional.get());
//	}

	private void save(RiskMonitor rM) {
		riskMonitorRepository.save(rM);
		log.info("Successfully Persisted the RiskMonitor {} ", rM);
	}

	private void save(WireDetailsEvent wireDetailsEvent) {
//		wireDetailsEvent.getBook().setWireDetailsEvent(wireDetailsEvent);
		wireDetailsEventsRepository.save(wireDetailsEvent);
//		calculatorRepository.save(c);
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

}

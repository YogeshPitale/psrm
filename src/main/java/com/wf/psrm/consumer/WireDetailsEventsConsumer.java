package com.wf.psrm.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
//import com.wf.psrm.service.LibraryEventsService;
import com.wf.psrm.service.WireDetailsEventsService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WireDetailsEventsConsumer {
	@Autowired
	private WireDetailsEventsService wireDetailsEventsService;

	@KafkaListener(topics = { "events" })
	public void onMessage(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {

		log.info("ConsumerRecord : {} ", consumerRecord);
		wireDetailsEventsService.processWireDetailsEvent(consumerRecord);

	}
}

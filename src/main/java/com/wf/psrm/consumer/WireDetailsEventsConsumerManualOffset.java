package com.wf.psrm.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WireDetailsEventsConsumerManualOffset implements AcknowledgingMessageListener<String, String> {
	@Override
	@KafkaListener(topics = { "events" })
	public void onMessage(ConsumerRecord<String, String> consumerRecord, Acknowledgment acknowledgment) {
		log.info("ConsumerRecord : {} ", consumerRecord);
		acknowledgment.acknowledge();
	}
}

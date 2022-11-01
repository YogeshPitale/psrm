package com.wf.psrm.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import com.wf.psrm.domain.WireDetailsEvent;
import com.wf.psrm.service.SequenceGeneratorService;

@Component
public class UserModelListener extends AbstractMongoEventListener<WireDetailsEvent> {

	private SequenceGeneratorService sequenceGenerator;

	@Autowired
	public UserModelListener(SequenceGeneratorService sequenceGenerator) {
		this.sequenceGenerator = sequenceGenerator;
	}

	@Override
	public void onBeforeConvert(BeforeConvertEvent<WireDetailsEvent> event) {
		if (event.getSource().getId() < 1) {
			event.getSource().setId(sequenceGenerator.generateSequence(WireDetailsEvent.SEQUENCE_NAME));
		}
	}

}

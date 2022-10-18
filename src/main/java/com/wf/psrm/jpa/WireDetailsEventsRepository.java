package com.wf.psrm.jpa;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.wf.psrm.domain.WireDetailsEvent;

public interface WireDetailsEventsRepository extends MongoRepository<WireDetailsEvent, Long> {
}

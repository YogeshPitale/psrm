package com.wf.psrm.jpa;

import org.springframework.data.repository.CrudRepository;

import com.wf.psrm.domain.WireDetailsEvent;

public interface WireDetailsEventsRepository extends CrudRepository<WireDetailsEvent, String> {
}

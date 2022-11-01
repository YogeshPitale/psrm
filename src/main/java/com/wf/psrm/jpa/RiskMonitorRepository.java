package com.wf.psrm.jpa;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.wf.psrm.domain.RiskMonitor;

public interface RiskMonitorRepository extends MongoRepository<RiskMonitor, Long> {
}

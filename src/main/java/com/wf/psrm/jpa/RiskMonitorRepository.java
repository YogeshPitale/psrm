package com.wf.psrm.jpa;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.wf.psrm.domain.RiskMonitor;

public interface RiskMonitorRepository extends MongoRepository<RiskMonitor, Long> {
}

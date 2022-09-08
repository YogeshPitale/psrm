package com.wf.psrm.jpa;

import org.springframework.data.repository.CrudRepository;
import com.wf.psrm.domain.RiskMonitor;

public interface RiskMonitorRepository extends CrudRepository<RiskMonitor, String> {
}

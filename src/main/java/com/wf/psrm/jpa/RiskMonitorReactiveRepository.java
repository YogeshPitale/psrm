package com.wf.psrm.jpa;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import org.springframework.stereotype.Repository;

import com.wf.psrm.domain.ReactiveRiskMonitor;

import reactor.core.publisher.Flux;

@Repository
public interface RiskMonitorReactiveRepository extends ReactiveMongoRepository<ReactiveRiskMonitor, Long> {

	@Tailable
	Flux<ReactiveRiskMonitor> findByIdNotNull();
}

package com.wf.psrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableFeignClients
@EnableMongoRepositories
@EnableReactiveMongoRepositories
public class PaymentSystemRiskMonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentSystemRiskMonitorApplication.class, args);
	}

}

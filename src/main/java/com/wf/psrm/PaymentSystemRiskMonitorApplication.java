package com.wf.psrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PaymentSystemRiskMonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentSystemRiskMonitorApplication.class, args);
	}

}

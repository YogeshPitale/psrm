package com.wf.psrm.controller;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.wf.psrm.domain.RiskMonitorCalculator;

@RestController
public class PsrmController {

	@Autowired
	RiskMonitorCalculator c1;

	@PostMapping("/v1/psrm/account")
	public ResponseEntity<RiskMonitorCalculator> postEvent(@RequestParam HashMap<String, Double> req)
			throws JsonProcessingException, ExecutionException, InterruptedException {

		c1.setInitialBalance(Double.parseDouble(String.valueOf(req.get("balance"))));
		c1.setCap(Double.parseDouble(String.valueOf(req.get("cap"))));

		return ResponseEntity.status(HttpStatus.CREATED).body(c1);
	}

}

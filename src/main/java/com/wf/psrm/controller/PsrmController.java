package com.wf.psrm.controller;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.wf.psrm.service.WireDetailsEventsService;
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
	private WireDetailsEventsService wireDetailsEventsService;

	@PostMapping("/v1/psrm/account")
	public ResponseEntity<Optional<?>> postEvent(@RequestParam HashMap<String, Double> req)
			throws JsonProcessingException, ExecutionException, InterruptedException {
		double initialBalance=Double.parseDouble(String.valueOf(req.get("balance")));
		double cap=Double.parseDouble(String.valueOf(req.get("cap")));
		wireDetailsEventsService.kickOffTheDay(initialBalance,cap);
		return ResponseEntity.status(HttpStatus.CREATED).body(Optional.empty());
	}

}

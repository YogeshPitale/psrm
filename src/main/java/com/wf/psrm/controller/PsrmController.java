package com.wf.psrm.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.wf.psrm.service.WireDetailsEventsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.wf.psrm.domain.RiskMonitorCalculator;
import com.wf.psrm.domain.RiskMonitor;

@RestController
public class PsrmController {

	@Autowired
	private WireDetailsEventsService wireDetailsEventsService;

	@PostMapping("/v1/psrm/account")
	public ResponseEntity<Optional<?>> postEvent(@RequestParam HashMap<String, Double> req)
			throws JsonProcessingException, ExecutionException, InterruptedException {
		double initialBalance = Double.parseDouble(String.valueOf(req.get("balance")));
		double cap = Double.parseDouble(String.valueOf(req.get("cap")));
		wireDetailsEventsService.kickOffTheDay(initialBalance, cap);
		return ResponseEntity.status(HttpStatus.CREATED).body(Optional.of("success"));
	}

	@CrossOrigin
	@GetMapping("/v1/psrm/risk-monitor")
	public List<RiskMonitor> getEvent() {
		return wireDetailsEventsService.getAllRiskMonitor();
	}

	@CrossOrigin
	@GetMapping("/v1/psrm/count")
	public int getCount() {
		return wireDetailsEventsService.getCount();
	}

	@CrossOrigin
	@PostMapping("/v1/psrm/throttle")
	public ResponseEntity<Optional<?>> postThrottle(@RequestParam Boolean throttleValue) {
		Boolean tempValue = wireDetailsEventsService.setThrottle(throttleValue);
		return ResponseEntity.status(HttpStatus.OK).body(Optional.of(tempValue));
	}

	@CrossOrigin
	@PostMapping("/v1/psrm/amount")
	public ResponseEntity<Optional<?>> postAmount(@RequestParam Integer amount) {
		Integer tempAmount = wireDetailsEventsService.setAmount(amount);
		return ResponseEntity.status(HttpStatus.OK).body(Optional.of(tempAmount));
	}

}

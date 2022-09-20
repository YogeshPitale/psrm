package com.wf.psrm.domain;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Component
public class RiskMonitorCalculator {

	private double initialBalance;
	private double fedwireCredits;
	private double fedwireDebits;
	private double cap;
	private double safetyfactor;
	private double maxAvailable;
	private double netFedWirePosition;
	private double currentPosition;
	private String timeStamp;

	public void update(RiskMonitor calculator) {
		this.initialBalance = calculator.getInitialBalance();
		this.fedwireCredits = calculator.getFedwireCredits();
		this.fedwireDebits = calculator.getFedwireDebits();
		this.cap = calculator.getCap();
		this.safetyfactor = calculator.getSafetyfactor();
		this.maxAvailable = calculator.getMaxAvailable();
		this.netFedWirePosition = calculator.getNetFedWirePosition();
		this.currentPosition = calculator.getCurrentPosition();
	}

//	public void update(RiskMonitorMoney riskMonitorMoney) {
//		this.initialBalance = riskMonitorMoney.getInitialBalance().getAmount().doubleValue();
//		this.fedwireCredits = riskMonitorMoney.getFedwireCredits().getAmount().doubleValue();
//		this.fedwireDebits = riskMonitorMoney.getFedwireDebits().getAmount().doubleValue();
//		this.cap = riskMonitorMoney.getCap().getAmount().doubleValue();
//		this.safetyfactor = riskMonitorMoney.getSafetyfactor().getAmount().doubleValue();
//		this.maxAvailable = riskMonitorMoney.getMaxAvailable().getAmount().doubleValue();
//		this.netFedWirePosition = riskMonitorMoney.getNetFedWirePosition().getAmount().doubleValue();
//		this.currentPosition = riskMonitorMoney.getCurrentPosition().getAmount().doubleValue();
//	}

}

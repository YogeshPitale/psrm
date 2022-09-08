package com.wf.psrm.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class RiskMonitor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private double initialBalance;
	private double fedwireCredits;
	private double fedwireDebits;
	private double cap;
	private double safetyfactor;
	private double maxAvailable;
	private double netFedWirePosition;
	private double currentPosition;

	public RiskMonitor(RiskMonitorCalculator calculator) {
		this.initialBalance = calculator.getInitialBalance();
		this.fedwireCredits = calculator.getFedwireCredits();
		this.fedwireDebits = calculator.getFedwireDebits();
		this.cap = calculator.getCap();
		this.currentPosition = calculator.getCurrentPosition();
		this.safetyfactor = cap * 0.1;
	}

	public RiskMonitor calculate() {
		netFedWirePosition = fedwireCredits - fedwireDebits;
		currentPosition = initialBalance + netFedWirePosition;
		maxAvailable = currentPosition + cap - safetyfactor;
		return this;
	}

	public void addCredit(double amt) {
		fedwireCredits = fedwireCredits + amt;
	}

	public void addDebit(double amt) {
		fedwireDebits = fedwireDebits + amt;
	}

}

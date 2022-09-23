package com.wf.psrm.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.math.RoundingMode;

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
	private String timeStamp;
	private String nm;
	private String status;
	private double creditAmt;
	private double DebitAmt;
	private int onHoldCount;
	private String pmtRail;

	public RiskMonitor(RiskMonitorCalculator calculator) {

		this.initialBalance = calculator.getInitialBalance();
		this.fedwireCredits = 0;
		this.fedwireDebits = 0;
		this.cap = calculator.getCap();
		// this.currentPosition = calculator.getCurrentPosition();
		this.safetyfactor = cap * 0.1;
		this.onHoldCount = 0;
	}

	public RiskMonitor calculate() {
//		Money money = Money.parse("USD 23.87");
		netFedWirePosition = round(fedwireCredits - fedwireDebits, 2);
		currentPosition = round(initialBalance + netFedWirePosition, 2);
		maxAvailable = round(currentPosition + cap - safetyfactor, 2);
		return this;
	}

	public void addCredit(double amt) {
		fedwireCredits = round(fedwireCredits + amt, 2);
	}

	public void addDebit(double amt) {
		fedwireDebits = round(fedwireDebits + amt, 2);
	}

	private static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public RiskMonitor(RiskMonitor rM) {
		super();
		this.initialBalance = rM.initialBalance;
		this.fedwireCredits = rM.fedwireCredits;
		this.fedwireDebits = rM.fedwireDebits;
		this.cap = rM.cap;
		this.safetyfactor = rM.safetyfactor;
		this.maxAvailable = rM.maxAvailable;
		this.netFedWirePosition = rM.netFedWirePosition;
		this.currentPosition = rM.currentPosition;
		this.onHoldCount = rM.onHoldCount;
	}

	public void setOnHoldCount() {
		this.onHoldCount += 1;
	}
}

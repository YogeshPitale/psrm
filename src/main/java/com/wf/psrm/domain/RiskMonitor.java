package com.wf.psrm.domain;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;

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
@Document(collection="riskmonitor")
public class RiskMonitor {

	@Id
	private Long id;
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
	private String reasonForHold;

	public RiskMonitor(RiskMonitorCalculator calculator) {

		this.initialBalance = calculator.getInitialBalance();
		this.fedwireCredits = 0;
		this.fedwireDebits = 0;
		this.cap = calculator.getCap();
		// this.currentPosition = calculator.getCurrentPosition();
		this.safetyfactor = cap * 0.1;
		this.onHoldCount = 0;
	}

	public synchronized RiskMonitor calculate() {
		netFedWirePosition = round(fedwireCredits - fedwireDebits, 2);
		currentPosition = round(initialBalance + netFedWirePosition, 2);
		maxAvailable = round(currentPosition + cap - safetyfactor, 2);
		return this;
	}

	public synchronized void addCredit(double amt) {
		fedwireCredits = round(fedwireCredits + amt, 2);
	}

	public synchronized void addDebit(double amt) {
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

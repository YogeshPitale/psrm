package com.wf.psrm.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.math.RoundingMode;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class RiskMonitorMoney {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private Money initialBalance;
	private Money fedwireCredits;
	private Money fedwireDebits;
	private Money cap;
	private Money safetyfactor;
	private Money maxAvailable;
	private Money netFedWirePosition;
	private Money currentPosition;
	final CurrencyUnit usd = CurrencyUnit.of("USD");

	public RiskMonitorMoney(RiskMonitorCalculator calculator) {

		this.initialBalance = Money.of(usd,calculator.getInitialBalance());
		this.fedwireCredits = Money.parse("USD 0");
		this.fedwireDebits = Money.parse("USD 0");
		netFedWirePosition = Money.parse("USD 0");
		this.cap = Money.of(usd,calculator.getCap());
		this.currentPosition = initialBalance;
		this.safetyfactor = cap.multipliedBy(0.1,RoundingMode.HALF_UP);
		this.maxAvailable = cap.minus(safetyfactor);
	}

	public synchronized RiskMonitorMoney calculate() {
		netFedWirePosition = fedwireCredits.minus(fedwireDebits);
		currentPosition = initialBalance.plus(netFedWirePosition);
		maxAvailable = (currentPosition.plus(cap)).minus(safetyfactor);
		return this;
	}

	public synchronized void addCredit(double amt) {
		fedwireCredits = fedwireCredits.plus(amt);
	}

	public synchronized void addDebit(double amt) {
		fedwireDebits =fedwireDebits.plus(amt);
	}
}

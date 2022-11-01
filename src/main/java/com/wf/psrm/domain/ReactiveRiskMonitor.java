package com.wf.psrm.domain;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Document
@ToString
@NoArgsConstructor
public class ReactiveRiskMonitor {

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

//	public ReactiveRiskMonitor(RiskMonitor riskMonitor) {
//		this.id = riskMonitor.getId();
//		this.initialBalance = riskMonitor.getInitialBalance();
//		this.fedwireCredits = riskMonitor.getFedwireCredits();
//		this.fedwireDebits = riskMonitor.getFedwireDebits();
//		this.cap = riskMonitor.getCap();
//		this.safetyfactor = riskMonitor.getSafetyfactor();
//		this.maxAvailable = riskMonitor.getMaxAvailable();
//		this.netFedWirePosition = riskMonitor.getNetFedWirePosition();
//		this.currentPosition = riskMonitor.getCurrentPosition();
//		this.timeStamp = riskMonitor.getTimeStamp();
//		this.nm = riskMonitor.getNm();
//		this.status = riskMonitor.getStatus();
//		this.creditAmt = riskMonitor.getCreditAmt();
//		this.DebitAmt = riskMonitor.getDebitAmt();
//		this.onHoldCount = riskMonitor.getOnHoldCount();
//		this.pmtRail = riskMonitor.getPmtRail();
//		this.reasonForHold = riskMonitor.getReasonForHold();
//	}

}

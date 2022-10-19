package com.wf.psrm.domain;

import java.util.ArrayList;

import lombok.Data;

@Data
public class TransactionStatus {
	private Boolean statusOnHold;
	private ArrayList<String> onHoldReasonsList;
}

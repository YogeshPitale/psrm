package com.wf.psrm.domain;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RulesList {
	private Boolean statusOnHold;
	private ArrayList<String> onHoldReasonsList;
}

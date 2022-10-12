package com.wf.psrm.domain;

import java.util.ArrayList;

import lombok.Data;

@Data
public class RulesList {
	private Boolean statusOnHold;
	private ArrayList<String> list;
}

package com.wf.psrm.domain;

import java.util.ArrayList;

import lombok.Data;

@Data
public class RulesList {

	private Boolean statusOnHold;
	private ArrayList<String> list;

	public RulesList() {
		super();
		this.list = new ArrayList<>();
		this.statusOnHold = false;
	}

	public void setList(String listString) {
		this.list.add(listString);
	}
}

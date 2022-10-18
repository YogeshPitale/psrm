package com.wf.psrm.domain;

import javax.persistence.Id;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Document(collection = "wiredetails")
public class WireDetailsEvent {
	
	@Transient
	public static final String SEQUENCE_NAME = "users_sequence";

	@Id
	private Long id;

	private String appId;

	private String pmtRail;

	private String payoriswells;

	private String payeeiswells;

	private Double amt;

	private String ccy;

	private String nm;

	private String evtDtTm;

//	@DecimalMin(value = "0.1", inclusive = true)
//	private Double initialBalance;

}

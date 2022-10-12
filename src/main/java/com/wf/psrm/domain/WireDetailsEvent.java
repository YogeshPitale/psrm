package com.wf.psrm.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

//import javax.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class WireDetailsEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

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

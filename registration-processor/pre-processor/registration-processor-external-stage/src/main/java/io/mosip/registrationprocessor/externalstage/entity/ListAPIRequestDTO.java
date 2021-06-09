package io.mosip.registrationprocessor.externalstage.entity;


import lombok.Data;

import java.io.Serializable;

@Data
public class ListAPIRequestDTO implements Serializable{

	/** API Name*/
	private String apiName;

	/** The Request Center ID */
	private String reqCenterId;

	/** The Request Operator ID */
	private String reqOperatorId;

	/** The Max Limit */
	private Integer reqMaxLimit;

}
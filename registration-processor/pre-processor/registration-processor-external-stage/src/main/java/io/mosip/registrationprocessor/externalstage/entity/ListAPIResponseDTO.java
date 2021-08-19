package io.mosip.registrationprocessor.externalstage.entity;


import lombok.Data;

import java.io.Serializable;

@Data
public class ListAPIResponseDTO implements Serializable{

	/** Reference ID*/
	private String refId;

	/** RID*/
	private String rid;

	/** The Request Center ID */
	private String centerId;

	/** The Request Operator ID */
	private String operatorId;

	/** The Request Operator Flag */
	private Integer operatorFlag;

	/** The Request Operator Flag */
	private Integer stageFlag;

}
package io.mosip.registrationprocessor.externalstage.entity;


import io.mosip.registration.processor.core.abstractverticle.MessageDTO;
import lombok.Data;

import java.io.Serializable;

@Data
public class MessageDRPrequestDTO extends MessageDTO {

	/** API Name*/
	private String apiName;

	/** The Request Center ID */
	private String centerId;

	/** The Request Operator ID */
	private String operatorId;

	/** The Request operator Flag */
	private String operatorFlag;

	/** The Request stage Flag */
	private String stageFlag;

	/** The Max Limit */
	private Integer maxLimit;

}
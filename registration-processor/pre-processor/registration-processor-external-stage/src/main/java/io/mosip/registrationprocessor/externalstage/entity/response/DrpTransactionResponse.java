package io.mosip.registrationprocessor.externalstage.entity.response;

import io.mosip.registrationprocessor.externalstage.utils.enums.OperatorFlag;
import io.mosip.registrationprocessor.externalstage.utils.enums.StageFlag;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
public class DrpTransactionResponse {


private String refId;
private String rid;
private String centerId;
private String drpOperator;
private String dataShareUrl;

@Enumerated(EnumType.STRING)
private OperatorFlag operatorFlag;

@Enumerated(EnumType.STRING)
private StageFlag stageFlag;
}

package io.mosip.registrationprocessor.externalstage.entity.request;


import io.mosip.registrationprocessor.externalstage.utils.enums.OperatorFlag;
import io.mosip.registrationprocessor.externalstage.utils.enums.StageFlag;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
public class FilterParameterReg {
    private String operatorId;
    private String centerId;
    private Integer maxLimit;

    @Enumerated(EnumType.STRING)
    private OperatorFlag operatorFlag;

    @Enumerated(EnumType.STRING)
    private StageFlag stageFlag;
}

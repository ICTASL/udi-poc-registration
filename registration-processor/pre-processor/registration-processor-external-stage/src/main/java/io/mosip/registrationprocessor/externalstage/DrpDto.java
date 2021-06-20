package io.mosip.registrationprocessor.externalstage;

import javax.persistence.Column;
import java.io.Serializable;

public class DrpDto implements Serializable {
    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 8907302024886152312L;

    /** The transaction id. */
    private String drpId;

    /** The registration id. */
    private String registrationId;

    /** The stage flag. */
    private String stageFlag;

    /** The operator_flag. */
    private String operatorFlag;

    /** The status comment. */
    private String statusComment;

    /** The operator id */
    private String operatorId;

    /** The center id */
    private String centerId;

    /** The is active. */
    private Boolean isActive;

    public DrpDto() {
    }

    public DrpDto(String drpId, String registrationId, String stageFlag, String operatorFlag, String statusComment, String operatorId, String centerId, Boolean isActive) {
        this.drpId = drpId;
        this.registrationId = registrationId;
        this.stageFlag = stageFlag;
        this.operatorFlag = operatorFlag;
        this.statusComment = statusComment;
        this.operatorId = operatorId;
        this.centerId = centerId;
        this.isActive = isActive;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getDrpId() {
        return drpId;
    }

    public void setDrpId(String drpId) {
        this.drpId = drpId;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getStageFlag() {
        return stageFlag;
    }

    public void setStageFlag(String stageFlag) {
        this.stageFlag = stageFlag;
    }

    public String getOperatorFlag() {
        return operatorFlag;
    }

    public void setOperatorFlag(String operatorFlag) {
        this.operatorFlag = operatorFlag;
    }

    public String getStatusComment() {
        return statusComment;
    }

    public void setStatusComment(String statusComment) {
        this.statusComment = statusComment;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getCenterId() {
        return centerId;
    }

    public void setCenterId(String centerId) {
        this.centerId = centerId;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}

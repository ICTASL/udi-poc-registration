package io.mosip.registrationprocessor.externalstage.entity;

import io.mosip.registration.processor.status.entity.BaseRegistrationEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "drp", schema = "regprc")
public class DrpEntity extends BaseRegistrationEntity {

    /** The id. */
    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue
    protected String id;

    /** The registration id. */
    @Column(name = "reg_id")
    private String registrationId;

    /** The stage flag. */
    @Column(name = "stage_flag")
    private String stageFlag;

    /** The operator flag. */
    @Column(name = "operator_flag")
    private String operatorFlag;

    /** The status comment. */
    @Column(name = "status_comment")
    private String statusComment;

    /** The operator id */
    @Column(name = "operator_id")
    private String operatorId;

    /** The center id */
    @Column(name = "center_id")
    private String centerId;

    /**  is active?. */
    @Column(name = "is_active", length = 32)
    private Boolean isActive ;

    /** The created by. */
    @Column(name = "cr_by")
    private String createdBy = "MOSIP_SYSTEM";

    /** The create date time. */
    @Column(name = "cr_dtimes")
    private LocalDateTime createDateTime;

    /** The updated by. */
    @Column(name = "upd_by", length = 32)
    private String updatedBy = "MOSIP_SYSTEM";

    /** The update date time. */
    @Column(name = "upd_dtimes")
    private LocalDateTime updateDateTime;

    /**  is deleted?. */
    @Column(name = "is_deleted", length = 32)
    private Boolean isDeleted ;

    /** The update date time. */
    @Column(name = "del_dtimes")
    private LocalDateTime deleteDateTime;

    public DrpEntity() {
        super();
    }

    public DrpEntity(String drpId, String registrationId, String stageFlag, String operatorFlag, String statusComment, String operatorId, String centerId) {
        this.id = drpId;
        this.registrationId = registrationId;
        this.stageFlag = stageFlag;
        this.operatorFlag = operatorFlag;
        this.statusComment = statusComment;
        this.operatorId = operatorId;
        this.centerId = centerId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(LocalDateTime createDateTime) {
        this.createDateTime = createDateTime;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(LocalDateTime updateDateTime) {
        this.updateDateTime = updateDateTime;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public LocalDateTime getDeleteDateTime() {
        return deleteDateTime;
    }

    public void setDeleteDateTime(LocalDateTime deleteDateTime) {
        this.deleteDateTime = deleteDateTime;
    }
}

//package io.mosip.registrationprocessor.externalstage.model;
//
//
//import io.mosip.registrationprocessor.externalstage.utils.enums.OperatorFlag;
//import io.mosip.registrationprocessor.externalstage.utils.enums.StageFlag;
//import lombok.Data;
//
//import javax.persistence.*;
//
//@Entity
//@Data
//@Table(name = "drp_transaction",schema = "regprc")
//public class DrpTransaction extends BaseEntity {
//
//    @Column(name = "ref_id")
//    private String refId;
//
//    @Column(name = "rid")
//    private String rid;
//
//    @Column(name = "center_id")
//    private String centerId;
//
//    @Column(name = "drp_operator")
//    private String drpOperator;
//
//    @Column(name = "data_share_url")
//    private String dataShareUrl;
//
//    @Column(name = "operator_flag")
//    @Enumerated(EnumType.STRING)
//    private OperatorFlag operatorFlag;
//
//    @Column(name = "stage_flag")
//    @Enumerated(EnumType.STRING)
//    private StageFlag stageFlag;
//
//}

package io.mosip.registrationprocessor.externalstage.utils.enums;

import java.util.HashMap;
import java.util.Map;

public enum OperatorFlag {

    OPERATOR_UNPICK("OPERATOR_UNPICK"), /** 0 */
    OPERATOR_PICK("OPERATOR_PICK"), /** 1 */
    OPERATOR_PROCESS("OPERATOR_PROCESS"); /** 2 */

    private static final Map<String, OperatorFlag> operatorFlagByValue = new HashMap<String, OperatorFlag>();

    static {
        for (OperatorFlag type : OperatorFlag.values()) {
            operatorFlagByValue.put(type.operatorFlag, type);
        }
    }

    private final String operatorFlag;

    OperatorFlag(String operatorFlag) {
        this.operatorFlag = operatorFlag;
    }
}

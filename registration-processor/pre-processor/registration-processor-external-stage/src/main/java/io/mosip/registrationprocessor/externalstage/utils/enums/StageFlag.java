package io.mosip.registrationprocessor.externalstage.utils.enums;

import java.util.HashMap;
import java.util.Map;

public enum StageFlag {

    STAGE_INITIATE("STAGE_INITIATE"), /** 0 */
    STAGE_READY_PROCESS("STAGE_READY_PROCESS"), /** 1 */
    STAGE_PROCESS("STAGE_PROCESS"); /** 2 */

    private static final Map<String, StageFlag> stageFlagByValue = new HashMap<String, StageFlag>();

    static {
        for (StageFlag type : StageFlag.values()) {
            stageFlagByValue.put(type.stageFlag, type);
        }
    }

    private final String stageFlag;

    StageFlag(String stageFlag) {
        this.stageFlag = stageFlag;
    }
}

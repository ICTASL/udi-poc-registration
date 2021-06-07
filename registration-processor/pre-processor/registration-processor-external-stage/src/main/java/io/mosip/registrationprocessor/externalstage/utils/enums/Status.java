package io.mosip.registrationprocessor.externalstage.utils.enums;

import java.util.HashMap;
import java.util.Map;

public enum Status {

    PENDING("PENDING"),
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE"),
    DELETE("DELETE"),
    VERIFIED("VERIFIED"),
    APPROVE("APPROVE"),
    REJECTED("REJECTED");

    private static final Map<String, Status> statusByValue = new HashMap<String, Status>();

    static {
        for (Status type : Status.values()) {
            statusByValue.put(type.status, type);
        }
    }

    private final String status;

    Status(String status) {
        this.status = status;
    }
}

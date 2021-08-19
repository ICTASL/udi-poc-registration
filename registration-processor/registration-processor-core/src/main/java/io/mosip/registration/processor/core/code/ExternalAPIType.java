package io.mosip.registration.processor.core.code;

/**
 * The Enum EventType.
 *
 * @author VirajRK
 */
public enum ExternalAPIType {

    /**
     * Get Pending Requests.
     */
    LIST,

    /**
     * Get RID demographic Data.
     */
    GETDATA,

    /**
     * Submit Success Requests
     */
    SUCCESS,

    /**
     * Submit REJECTED Requests.
     */
    REJECT,

    /**
     * Get Submit Operator picked RID.
     */
    PICK,

    /**
     * Get Submit Operator unpicked RID.
     */
    UNPICK,

}
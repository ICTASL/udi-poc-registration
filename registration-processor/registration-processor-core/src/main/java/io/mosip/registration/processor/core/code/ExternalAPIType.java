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
     * Get RID Geographic Data.
     */
    RIDDETAILS,

    /**
     * Submit Success Requests
     */
    SUCCESS,

    /**
     * Submit REJECTED Requests.
     */
    REJECT,

}
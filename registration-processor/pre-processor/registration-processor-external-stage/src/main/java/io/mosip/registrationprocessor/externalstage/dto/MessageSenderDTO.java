package io.mosip.registrationprocessor.externalstage.dto;

import io.mosip.registrationprocessor.externalstage.utils.NotificationSubjectCode;
import io.mosip.registrationprocessor.externalstage.utils.NotificationTemplateTypeCode;
import lombok.Data;

@Data
public class MessageSenderDTO {

    /**
     * The sms template code.
     */
    private NotificationTemplateTypeCode smsTemplateCode = null;

    /**
     * The email template code.
     */
    private NotificationTemplateTypeCode emailTemplateCode = null;

    /**
     * The subject.
     */
    private NotificationSubjectCode subjectTemplateCode = null;

}

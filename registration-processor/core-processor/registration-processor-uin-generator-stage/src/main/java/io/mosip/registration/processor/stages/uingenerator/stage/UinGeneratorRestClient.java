package io.mosip.registration.processor.stages.uingenerator.stage;

import com.google.gson.Gson;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.processor.core.abstractverticle.MosipVerticleAPIManager;
import io.mosip.registration.processor.core.code.ApiName;
import io.mosip.registration.processor.core.code.RegistrationExceptionTypeCode;
import io.mosip.registration.processor.core.constant.LoggerFileConstant;
import io.mosip.registration.processor.core.exception.ApisResourceAccessException;
import io.mosip.registration.processor.core.exception.util.PlatformErrorMessages;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.mosip.registration.processor.core.spi.restclient.RegistrationProcessorRestClientService;
import io.mosip.registration.processor.core.status.util.StatusUtil;
import io.mosip.registration.processor.stages.uingenerator.dto.UinGenResponseDto;
import io.mosip.registration.processor.stages.uingenerator.dto.UinResponseDto;
import io.mosip.registration.processor.status.code.RegistrationStatusCode;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class UinGeneratorRestClient {

    private static Logger regProcLogger = RegProcessorLogger.getLogger(UinGeneratorRestClient.class);
    private static final String RECORD_ALREADY_EXISTS_ERROR = "IDR-IDC-012";

    @Autowired
    private Environment env;

    @Autowired
    RegistrationProcessorRestClientService<Object> registrationProcessorRestClientService;

    public String generateUin() {
        String uinField;
        UinGenResponseDto uinResponseDto = null;
        try {
            regProcLogger.debug("uid generation ---","","","UinGeneratorRestClient::entry");
            String test = (String) registrationProcessorRestClientService.getApi(ApiName.UINGENERATOR, null, "",
                    "", String.class);

            Gson gsonObj = new Gson();
            uinResponseDto = gsonObj.fromJson(test, UinGenResponseDto.class);

            uinField = uinResponseDto.getResponse().getUin();
            return uinField;

        } catch (ApisResourceAccessException ex) {
            regProcLogger.error("Error in uid generation","","",ex.toString());
            ex.printStackTrace();

        }
        return null;

    }
}




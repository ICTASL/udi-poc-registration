package io.mosip.registration.processor.stages.uingenerator.controller;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.mosip.registration.processor.core.spi.restclient.RegistrationProcessorRestClientService;
import io.mosip.registration.processor.stages.uingenerator.stage.UinGeneratorRestClient;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(basePath = "/uingenerator", value = "Mosip ", description = "mosip uingenrator", produces = "application/json")
@RestController
@RequestMapping("/uin")
@CrossOrigin
public class UinGeneratorController {

    /** The reg proc logger. */
    private static Logger uinGeneratorLogger = RegProcessorLogger.getLogger(UinGeneratorController.class);

    @Autowired
    private UinGeneratorRestClient uinGeneratorRestClient;

    @GetMapping("")
    public ResponseEntity<String> getUid() {
        uinGeneratorLogger.info("---get uin rest ---","","","");
        String uid = uinGeneratorRestClient.generateUin();
        return new ResponseEntity<>(uid, new HttpHeaders(), HttpStatus.OK);
    }

}

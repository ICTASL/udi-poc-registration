package io.mosip.registrationprocessor.externalstage.utils;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.registration.processor.core.constant.LoggerFileConstant;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.mosip.registrationprocessor.externalstage.stage.ExternalStage;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Component
public class JP2ImageConverter {

    private static Logger regProcLogger = RegProcessorLogger.getLogger(ExternalStage.class);

    public String convert(byte[] photoBytes) {
        String output = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(photoBytes);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            BufferedImage image = ImageIO.read(bis);
            ImageIO.write(image, "jpg", bos);
            byte[] data = bos.toByteArray();
            output = CryptoUtil.encodeBase64String(data);
        } catch (Exception e) {
            regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                    "", org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
        }
        return output;
    }
}

package io.mosip.registrationprocessor.externalstage.config;

import io.mosip.kernel.cbeffutil.impl.CbeffImpl;
import io.mosip.kernel.core.cbeffutil.spi.CbeffUtil;
import io.mosip.registration.processor.core.packet.dto.Identity;
import io.mosip.registration.processor.core.spi.packetmanager.PacketInfoManager;
import io.mosip.registration.processor.message.sender.template.TemplateGenerator;
import io.mosip.registration.processor.packet.storage.dao.PacketInfoDao;
import io.mosip.registration.processor.packet.storage.dto.ApplicantInfoDto;
import io.mosip.registration.processor.packet.storage.helper.PacketManagerHelper;
import io.mosip.registration.processor.packet.storage.service.impl.PacketInfoManagerImpl;
import io.mosip.registration.processor.packet.storage.utils.IdSchemaUtil;
import io.mosip.registration.processor.packet.storage.utils.PacketManagerService;
import io.mosip.registration.processor.packet.storage.utils.PriorityBasedPacketManagerService;
import io.mosip.registration.processor.packet.storage.utils.Utilities;
import io.mosip.registrationprocessor.externalstage.DrpDto;
import io.mosip.registrationprocessor.externalstage.service.DrpService;
import io.mosip.registrationprocessor.externalstage.service.impl.DrpServiceImpl;
import io.mosip.registrationprocessor.externalstage.utils.ExtractFaceImageData;
import io.mosip.registrationprocessor.externalstage.utils.JP2ImageConverter;
import io.mosip.registrationprocessor.externalstage.utils.NotificationUtility;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import io.mosip.registrationprocessor.externalstage.stage.ExternalStage;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * external stage beans configuration class
 */
@Configuration
@EnableAspectJAutoProxy
@EnableJpaRepositories(basePackages = {
        "io.mosip.registrationprocessor.externalstage.repositary"
})
public class Externalconfig {

    @Bean
    @ConfigurationProperties(prefix = "provider.packetreader")
    public Map<String, String> readerConfiguration() {
        return new HashMap<>();
    }

    @Bean
    @ConfigurationProperties(prefix = "packetmanager.provider")
    public Map<String, String> providerConfiguration() {
        return new HashMap<>();
    }

    @Bean
    @ConfigurationProperties(prefix = "provider.packetwriter")
    public Map<String, String> writerConfiguration() {
        return new HashMap<>();
    }

    @PostConstruct
    public void initialize() {
        Utilities.initialize(readerConfiguration(), writerConfiguration());
        PriorityBasedPacketManagerService.initialize(providerConfiguration());
    }

    @Bean
    public PacketManagerService packetManagerService() {
        return new PacketManagerService();
    }


    /**
     * ExternalStage bean
     */
    @Bean
    public ExternalStage externalStage() {
        return new ExternalStage();
    }

    @Bean
    public DrpService<DrpDto> drpService() {
        return new DrpServiceImpl();
    }

    @Bean
    public Utilities utility() {
        return new Utilities();
    }

    @Bean
    public NotificationUtility notificationUtility() {
        return new NotificationUtility();
    }

    @Bean
    public IdSchemaUtil idSchemaUtil() { return new IdSchemaUtil(); }

    @Bean
    public CbeffUtil cbeffUtil() {
        return new CbeffImpl();
    }

    @Bean
    public JP2ImageConverter jP2ImageConverter() { return new JP2ImageConverter(); }

    @Bean
    public ExtractFaceImageData extractFaceImageData() { return new ExtractFaceImageData(); }

    /** Required for packetManagerService*/
    @Bean
    public PacketInfoDao packetInfoDao() { return new PacketInfoDao(); }

    /** Required for packetManagerService*/
    @Bean
    public PacketInfoManager<Identity, ApplicantInfoDto> packetInfoManager() { return new PacketInfoManagerImpl(); }

    /** Required for packetManagerService*/
    @Bean
    public PacketManagerHelper packetManagerHelper() { return new PacketManagerHelper(); }

    /** Required for notificationUtility*/
    @Bean
    public TemplateGenerator templateGenerator() { return new TemplateGenerator(); }


}

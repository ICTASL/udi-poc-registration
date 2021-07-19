package io.mosip.registrationprocessor.externalstage.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.mosip.kernel.cbeffutil.impl.CbeffImpl;
import io.mosip.kernel.core.cbeffutil.spi.CbeffUtil;
import io.mosip.registration.processor.core.packet.dto.Identity;
import io.mosip.registration.processor.core.spi.packetmanager.PacketInfoManager;
import io.mosip.registration.processor.message.sender.template.TemplateGenerator;
import io.mosip.registration.processor.packet.manager.decryptor.Decryptor;
import io.mosip.registration.processor.packet.manager.decryptor.DecryptorImpl;
import io.mosip.registration.processor.packet.manager.idreposervice.IdRepoService;
import io.mosip.registration.processor.packet.manager.idreposervice.impl.IdRepoServiceImpl;
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
import io.mosip.registrationprocessor.externalstage.utils.NotificationUtility;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import io.mosip.registrationprocessor.externalstage.stage.ExternalStage;
import org.springframework.context.annotation.Primary;
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
    public IdRepoService getIdRepoService() {
        return new IdRepoServiceImpl();
    }

    @Bean
    public Utilities getUtilities() {
        return new Utilities();
    }

    @Bean
    public PacketInfoManager<Identity, ApplicantInfoDto> getPacketInfoManager() {
        return new PacketInfoManagerImpl();
    }

    @Bean
    public PacketInfoDao getPacketInfoDao() {
        return new PacketInfoDao();
    }

    @Bean
    public PacketManagerHelper packetManagerHelper() {
        return new PacketManagerHelper();
    }

    @Bean
    public NotificationUtility notificationUtility() {
        return new NotificationUtility();
    }

    @Bean
    public TemplateGenerator getTemplateGenerator() {
        return new TemplateGenerator();
    }

    @Bean
    public Decryptor getDecryptor() {
        return new DecryptorImpl();
    }

    @Bean
    public IdSchemaUtil idSchemaUtil() { return new IdSchemaUtil(); }

    @Bean
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Bean
    public CbeffUtil getCbeffUtil() {
        return new CbeffImpl();
    }

}

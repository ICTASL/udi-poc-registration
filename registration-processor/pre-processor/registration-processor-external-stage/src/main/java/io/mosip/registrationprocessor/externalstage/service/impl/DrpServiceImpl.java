package io.mosip.registrationprocessor.externalstage.service.impl;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.processor.core.constant.LoggerFileConstant;
import io.mosip.registration.processor.core.exception.util.PlatformErrorMessages;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.mosip.registration.processor.status.dto.TransactionDto;
import io.mosip.registration.processor.status.entity.TransactionEntity;
import io.mosip.registration.processor.status.exception.TransactionTableNotAccessibleException;
import io.mosip.registration.processor.status.repositary.RegistrationRepositary;
import io.mosip.registration.processor.status.service.impl.TransactionServiceImpl;
import io.mosip.registrationprocessor.externalstage.DrpDto;
import io.mosip.registrationprocessor.externalstage.entity.DrpEntity;
import io.mosip.registrationprocessor.externalstage.repositary.DrpRepositary;
import io.mosip.registrationprocessor.externalstage.service.DrpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DrpServiceImpl implements DrpService<DrpDto> {

    /**
     * The reg proc logger.
     */
    private static Logger regProcLogger = RegProcessorLogger.getLogger(DrpServiceImpl.class);

    /**
     * The drp repositary.
     */
    @Autowired
    DrpRepositary<DrpEntity, String> drpRepositary;

    @Override
    public DrpEntity addDrpTransaction(DrpDto drpDto) {
        try {
            regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
                    drpDto.getRegistrationId(),
                    "DrpServiceImpl::addDrpTransaction()::entry");
            DrpEntity entity = convertDtoToEntity(drpDto);
            return drpRepositary.save(entity);
        } catch (DataAccessLayerException e) {
            throw new TransactionTableNotAccessibleException(
                    PlatformErrorMessages.RPR_RGS_TRANSACTION_TABLE_NOT_ACCESSIBLE.getMessage(), e);
        }
    }

    @Override
    public List<DrpDto> getRIDList(DrpDto drpDto) {
        List<DrpDto> drpDtoList = new ArrayList<>();
        try {
            regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
                    drpDto.getRegistrationId(),
                    "DrpServiceImpl::addDrpTransaction()::entry");
            DrpEntity entity = convertDtoToEntity(drpDto);
            List<DrpEntity> drpEntityList = drpRepositary.getRIDList();
            if (!CollectionUtils.isEmpty(drpEntityList)) {
                for (DrpEntity drpEntity : drpEntityList) {
                    drpDto = convertEntityToDto(drpEntity);
                    drpDtoList.add(drpDto);
                }
            }
        } catch (DataAccessLayerException e) {
            throw new TransactionTableNotAccessibleException(
                    PlatformErrorMessages.RPR_RGS_TRANSACTION_TABLE_NOT_ACCESSIBLE.getMessage(), e);
        }
        return drpDtoList;
    }

    private DrpEntity convertDtoToEntity(DrpDto dto) {
        DrpEntity drpEntity = new DrpEntity(dto.getDrpId(), dto.getRegistrationId(),
                dto.getStageFlag(), dto.getOperatorFlag(), dto.getStatusComment(), dto.getOperatorId(),
                dto.getCenterId());
        drpEntity.setActive(Boolean.TRUE);
        drpEntity.setCreatedBy("MOSIP_SYSTEM");
        drpEntity.setCreateDateTime(LocalDateTime.now(ZoneId.of("UTC")));
        drpEntity.setUpdateDateTime(LocalDateTime.now(ZoneId.of("UTC")));
        return drpEntity;
    }

    private DrpDto convertEntityToDto(DrpEntity drpEntity) {
        DrpDto dto = new DrpDto(drpEntity.getId(), drpEntity.getRegistrationId(), drpEntity.getStageFlag(), drpEntity.getOperatorFlag()
                , drpEntity.getStatusComment(), drpEntity.getOperatorId(), drpEntity.getCenterId(), drpEntity.getActive());
        return dto;
    }
}

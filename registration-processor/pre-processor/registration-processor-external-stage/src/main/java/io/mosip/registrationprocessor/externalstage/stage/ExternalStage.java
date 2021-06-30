package io.mosip.registrationprocessor.externalstage.stage;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.processor.core.abstractverticle.*;
import io.mosip.registration.processor.core.code.*;
import io.mosip.registration.processor.core.constant.LoggerFileConstant;
import io.mosip.registration.processor.core.constant.RegistrationType;
import io.mosip.registration.processor.core.exception.util.PlatformErrorMessages;
import io.mosip.registration.processor.core.exception.util.PlatformSuccessMessages;
import io.mosip.registration.processor.core.logger.LogDescription;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.mosip.registration.processor.core.spi.restclient.RegistrationProcessorRestClientService;
import io.mosip.registration.processor.core.status.util.StatusUtil;
import io.mosip.registration.processor.core.status.util.TrimExceptionMessage;
import io.mosip.registration.processor.core.util.RegistrationExceptionMapperUtil;
import io.mosip.registration.processor.packet.manager.idreposervice.IdRepoService;
import io.mosip.registration.processor.rest.client.audit.builder.AuditLogRequestBuilder;
import io.mosip.registration.processor.status.code.RegistrationStatusCode;
import io.mosip.registration.processor.status.dto.InternalRegistrationStatusDto;
import io.mosip.registration.processor.status.dto.RegistrationStatusDto;
import io.mosip.registration.processor.status.exception.TablenotAccessibleException;
import io.mosip.registration.processor.status.service.RegistrationStatusService;
import io.mosip.registrationprocessor.externalstage.DrpDto;
import io.mosip.registrationprocessor.externalstage.entity.ListAPIResponseDTO;
import io.mosip.registrationprocessor.externalstage.entity.MessageDRPrequestDTO;
import io.mosip.registrationprocessor.externalstage.service.DrpService;
import io.mosip.registrationprocessor.externalstage.utils.DrpOperatorStageCode;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.mosip.registration.processor.packet.storage.utils.Utilities;

import java.util.*;

/**
 * External stage verticle class
 */
@Service
public class ExternalStage extends MosipVerticleAPIManager {
    /**
     * The reg proc logger.
     */
    private static Logger regProcLogger = RegProcessorLogger.getLogger(ExternalStage.class);
    /**
     * request id
     */
    private static final String ID = "io.mosip.registrationprocessor";
    /**
     * request version
     */
    private static final String VERSION = "1.0";
    /**
     * mosipEventBus
     */
    private MosipEventBus mosipEventBus;
    /**
     * vertx Cluster Manager Url.
     */
    @Value("${vertx.cluster.configuration}")
    private String clusterManagerUrl;

    /**
     * server port number.
     */
    @Value("${server.port}")
    private String port;

    /**
     * worker pool size.
     */
    @Value("${worker.pool.size}")
    private Integer workerPoolSize;

    /**
     * After this time intervel, message should be considered as expired (In seconds).
     */
    @Value("${mosip.regproc.external.message.expiry-time-limit}")
    private Long messageExpiryTimeLimit;

    @Autowired
    private AuditLogRequestBuilder auditLogRequestBuilder;

    /**
     * The registration status service.
     */
    @Autowired
    private RegistrationStatusService<String, InternalRegistrationStatusDto, RegistrationStatusDto> registrationStatusService;

    @Autowired
    private DrpService<DrpDto> drpService;

    /**
     * rest client to send requests.
     */
    @Autowired
    private RegistrationProcessorRestClientService<Object> registrationProcessorRestService;

    /**
     * Mosip router for APIs
     */
    @Autowired
    MosipRouter router;

    /**
     * The description.
     */
    @Autowired
    LogDescription description;

    /**
     * The context path.
     */
    @Value("${server.servlet.path}")
    private String contextPath;

    /**
     * The Constant USER.
     */
    private static final String USER = "MOSIP_SYSTEM";

    @Autowired
    RegistrationExceptionMapperUtil registrationStatusMapperUtil;

    @Autowired
    private IdRepoService idRepoService;

    /**
     * The utilities.
     */
    @Autowired
    Utilities utilities;

    /**
     * method to deploy external stage verticle
     */
    public void deployVerticle() {
        this.mosipEventBus = this.getEventBus(this, clusterManagerUrl, workerPoolSize);
        this.consumeAndSend(mosipEventBus, MessageBusAddress.EXTERNAL_STAGE_BUS_IN,
                MessageBusAddress.EXTERNAL_STAGE_BUS_OUT, messageExpiryTimeLimit);
    }

    /*
     * (non-Javadoc)
     *
     * @see io.vertx.core.AbstractVerticle#start()
     */
    @Override
    public void start() {
        router.setRoute(this.postUrl(vertx, MessageBusAddress.EXTERNAL_STAGE_BUS_IN, MessageBusAddress.EXTERNAL_STAGE_BUS_OUT));
        this.routes(router);
        this.createServer(router.getRouter(), Integer.parseInt(port));
    }

    private void routes(MosipRouter router) {
        router.post(contextPath + "/drpstage");
        router.handler(this::processURL, this::failure);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * io.mosip.registration.processor.core.spi.eventbus.EventBusManager#process(
     * java.lang.Object)
     */

    public void processURL(RoutingContext ctx) {
        regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
                "ExternalStage::processURL()::entry");

        InternalRegistrationStatusDto registrationStatusDto = new InternalRegistrationStatusDto();
        DrpDto drpDto = new DrpDto();
        MessageDRPrequestDTO messageDTO = new MessageDRPrequestDTO();

        String apiName = "";
        TrimExceptionMessage trimMessage = new TrimExceptionMessage();
        LogDescription description = new LogDescription();
        boolean isTransactionSuccessful = false;

        try {
            JsonObject obj = ctx.getBodyAsJson();
            apiName = obj.getString("apiName");
            JsonObject requestJson = obj.getJsonObject("request");

            messageDTO.setApiName(obj.getString("apiName"));
            messageDTO.setRid(obj.getString("rid"));
            messageDTO.setIsValid(obj.getBoolean("isValid"));
            messageDTO.setMessageBusAddress(MessageBusAddress.EXTERNAL_STAGE_BUS_IN);
            messageDTO.setInternalError(Boolean.FALSE);
            messageDTO.setReg_type(RegistrationType.valueOf(obj.getString("reg_type")));
            if (requestJson != null) {
                messageDTO.setOperatorId(requestJson.getString("operatorId"));
                messageDTO.setCenterId(requestJson.getString("centerId"));
                messageDTO.setMaxLimit(requestJson.getInteger("maxLimit"));
                messageDTO.setOperatorFlag(requestJson.getString("operatorFlag"));
                messageDTO.setStageFlag(requestJson.getString("stageFlag"));
                messageDTO.setStatusComment(requestJson.getString("statusComment"));
            }
            if (apiName != null && apiName != "" && !apiName.equals(ExternalAPIType.LIST.toString())) {
                registrationStatusDto = registrationStatusService.getRegistrationStatus(messageDTO.getRid());
                List<DrpDto> drpDtoList = drpService.getDrpEntryByRegId(messageDTO.getRid());
                if (drpDtoList != null && !drpDtoList.isEmpty() && drpDtoList.get(0) != null) {
                    drpDto = drpDtoList.get(0);
                }
            }

            if (apiName != null && apiName != "" && apiName.equals(ExternalAPIType.LIST.toString())) {
                List<DrpDto> drpDtoList = drpService.getRIDList(drpDto);
                setResponse(ctx, drpDtoList);
                isTransactionSuccessful = false;
                messageDTO.setIsValid(Boolean.TRUE);
            } else if (apiName != null && apiName != "" && apiName.equals(ExternalAPIType.GETDATA.toString())) {
                if (registrationStatusDto != null && messageDTO.getRid().equalsIgnoreCase(registrationStatusDto.getRegistrationId())
                        && drpDto != null && messageDTO.getRid().equalsIgnoreCase(drpDto.getRegistrationId())) {
                    JSONObject matchedDemographicIdentity = idRepoService.getIdJsonFromIDRepo(messageDTO.getRid(),
                            utilities.getGetRegProcessorDemographicIdentity());
                    Map convertedObject = convertGetDataObject(matchedDemographicIdentity);
                    convertedObject.put("rid", messageDTO.getRid());
                    setResponse(ctx, convertedObject);
                    isTransactionSuccessful = false;
                    messageDTO.setIsValid(Boolean.TRUE);
                } else {
                    isTransactionSuccessful = false;
                    messageDTO.setIsValid(Boolean.FALSE);
                    regProcLogger.error(LoggerFileConstant.SESSIONID.toString(),
                            LoggerFileConstant.REGISTRATIONID.toString(), messageDTO.getRid(),
                            "Transaction failed. RID not found in registration table.");
                }
            } else if (apiName != null && apiName != "" && apiName.equals(ExternalAPIType.PICK.toString())) {
                if (registrationStatusDto != null && messageDTO.getRid().equalsIgnoreCase(registrationStatusDto.getRegistrationId())
                        && drpDto != null && messageDTO.getRid().equalsIgnoreCase(drpDto.getRegistrationId())
                        && drpDto.getOperatorFlag() != null && drpDto.getOperatorFlag().equals(DrpOperatorStageCode.DEFAULT.toString())) {
                    isTransactionSuccessful = false;
                    messageDTO.setIsValid(Boolean.TRUE);

                    drpDto.setOperatorFlag(DrpOperatorStageCode.PICK.toString());
                    drpDto.setActive(Boolean.TRUE);
                    drpDto.setCenterId(messageDTO.getCenterId());
                    drpDto.setOperatorId(messageDTO.getOperatorId());
                } else {
                    isTransactionSuccessful = false;
                    messageDTO.setIsValid(Boolean.FALSE);
                    regProcLogger.error(LoggerFileConstant.SESSIONID.toString(),
                            LoggerFileConstant.REGISTRATIONID.toString(), messageDTO.getRid(),
                            "Transaction failed. RID not found in registration table.");
                }
            } else if (apiName != null && apiName != "" && apiName.equals(ExternalAPIType.UNPICK.toString())) {
                if (registrationStatusDto != null && messageDTO.getRid().equalsIgnoreCase(registrationStatusDto.getRegistrationId())
                        && drpDto != null && messageDTO.getRid().equalsIgnoreCase(drpDto.getRegistrationId())
                        && drpDto.getOperatorFlag() != null && drpDto.getOperatorFlag().equals(DrpOperatorStageCode.PICK.toString())) {
                    isTransactionSuccessful = false;
                    messageDTO.setIsValid(Boolean.TRUE);

                    drpDto.setOperatorFlag(DrpOperatorStageCode.DEFAULT.toString());
                    drpDto.setActive(Boolean.TRUE);
                    drpDto.setCenterId(messageDTO.getCenterId());
                    drpDto.setOperatorId(messageDTO.getOperatorId());
                } else {
                    isTransactionSuccessful = false;
                    messageDTO.setIsValid(Boolean.FALSE);
                    regProcLogger.error(LoggerFileConstant.SESSIONID.toString(),
                            LoggerFileConstant.REGISTRATIONID.toString(), messageDTO.getRid(),
                            "Transaction failed. RID not found in registration table.");
                }
            } else if (apiName != null && apiName != "" && apiName.equals(ExternalAPIType.SUCCESS.toString())) {
                if (registrationStatusDto != null && messageDTO.getRid().equalsIgnoreCase(registrationStatusDto.getRegistrationId())
                        && drpDto != null && messageDTO.getRid().equalsIgnoreCase(drpDto.getRegistrationId())) {
                    registrationStatusDto.setLatestTransactionTypeCode(RegistrationTransactionTypeCode.EXTERNAL_INTEGRATION.toString());
                    registrationStatusDto.setRegistrationStageName(this.getClass().getSimpleName());

                    registrationStatusDto.setLatestTransactionStatusCode(RegistrationTransactionStatusCode.SUCCESS.toString());
                    registrationStatusDto.setStatusComment(StatusUtil.DRP_STAGE_SUCCESS.getMessage());
                    registrationStatusDto.setSubStatusCode(StatusUtil.DRP_STAGE_SUCCESS.getCode());
                    registrationStatusDto.setStatusCode(RegistrationStatusCode.PROCESSED.toString());

                    drpDto.setStageFlag(RegistrationStatusCode.PROCESSED.toString());
                    drpDto.setOperatorFlag(RegistrationStatusCode.PROCESSED.toString());
                    drpDto.setStatusComment(messageDTO.getStatusComment());
                    drpDto.setActive(Boolean.TRUE);
                    drpDto.setCenterId(messageDTO.getCenterId());
                    drpDto.setOperatorId(messageDTO.getOperatorId());

                    messageDTO.setIsValid(Boolean.TRUE);
                    isTransactionSuccessful = true;
                    description.setMessage(PlatformSuccessMessages.RPR_DRP_STAGE_SUCCESS.getMessage() + " -- " + messageDTO.getRid());
                    description.setCode(PlatformSuccessMessages.RPR_DRP_STAGE_SUCCESS.getCode());

                    regProcLogger.info(LoggerFileConstant.SESSIONID.toString(),
                            LoggerFileConstant.REGISTRATIONID.toString(), messageDTO.getRid(),
                            description.getCode() + description.getMessage());
                } else {
                    isTransactionSuccessful = false;
                    messageDTO.setIsValid(Boolean.FALSE);
                    regProcLogger.error(LoggerFileConstant.SESSIONID.toString(),
                            LoggerFileConstant.REGISTRATIONID.toString(), messageDTO.getRid(),
                            "Transaction failed. RID not found in registration table.");
                }

            } else if (apiName != null && apiName != "" && apiName.equals(ExternalAPIType.REJECT.toString())) {

                if (registrationStatusDto != null && messageDTO.getRid().equalsIgnoreCase(registrationStatusDto.getRegistrationId())
                        && drpDto != null && messageDTO.getRid().equalsIgnoreCase(drpDto.getRegistrationId())) {
                    registrationStatusDto.setLatestTransactionTypeCode(RegistrationTransactionTypeCode.EXTERNAL_INTEGRATION.toString());
                    registrationStatusDto.setRegistrationStageName(this.getClass().getSimpleName());

                    registrationStatusDto.setLatestTransactionStatusCode(RegistrationTransactionStatusCode.REJECTED.toString());
                    registrationStatusDto.setStatusComment(StatusUtil.DRP_STAGE_REJECTED.getMessage());
                    registrationStatusDto.setSubStatusCode(StatusUtil.DRP_STAGE_REJECTED.getCode());
                    registrationStatusDto.setStatusCode(RegistrationStatusCode.REJECTED.toString());

                    drpDto.setStageFlag(RegistrationStatusCode.REJECTED.toString());
                    drpDto.setOperatorFlag(RegistrationStatusCode.REJECTED.toString());
                    drpDto.setStatusComment(messageDTO.getStatusComment());
                    drpDto.setActive(Boolean.TRUE);
                    drpDto.setCenterId(messageDTO.getCenterId());
                    drpDto.setOperatorId(messageDTO.getOperatorId());

                    description.setMessage(PlatformErrorMessages.DRP_STAGE_REJECTED.getMessage() + " -- " + messageDTO.getRid());
                    description.setCode(PlatformErrorMessages.DRP_STAGE_REJECTED.getCode());

                    regProcLogger.info(LoggerFileConstant.SESSIONID.toString(),
                            LoggerFileConstant.REGISTRATIONID.toString(), messageDTO.getRid(),
                            description.getCode() + description.getMessage());

                    messageDTO.setIsValid(Boolean.TRUE);
                    messageDTO.setInternalError(Boolean.FALSE);
                    messageDTO.setRid(registrationStatusDto.getRegistrationId());


                } else {
                    isTransactionSuccessful = false;
                    messageDTO.setIsValid(Boolean.FALSE);
                    regProcLogger.error(LoggerFileConstant.SESSIONID.toString(),
                            LoggerFileConstant.REGISTRATIONID.toString(), messageDTO.getRid(),
                            "Transaction failed. RID not found in registration table.");
                }
            } else {
                isTransactionSuccessful = false;
                messageDTO.setIsValid(Boolean.FALSE);
                regProcLogger.error(LoggerFileConstant.SESSIONID.toString(),
                        LoggerFileConstant.REGISTRATIONID.toString(), messageDTO.getRid(),
                        "Invalid API Name " + apiName);
            }

            if (messageDTO.getIsValid()) {
                if (apiName.equals(ExternalAPIType.SUCCESS.toString())) {
                    sendMessage(messageDTO);
                    this.setResponse(ctx, "Packet with registrationId '" + messageDTO.getRid() + "' has been forwarded to next stage");
                    regProcLogger.info(obj.getString("rid"),
                            "Packet with registrationId '" + messageDTO.getRid() + "' has been forwarded to next stage", null,
                            null);
                } else if (apiName.equals(ExternalAPIType.REJECT.toString())) {
                    this.setResponse(ctx, "Packet with registrationId '" + obj.getString("rid") + "' has been rejected by DRP user");
                    regProcLogger.info(obj.getString("rid"),
                            "Packet with registrationId '" + messageDTO.getRid() + "' has been rejected by DRP user",
                            null, null);
                } else if (apiName.equals(ExternalAPIType.PICK.toString())) {
                    this.setResponse(ctx, "Packet with registrationId '" + obj.getString("rid") + "' has been marked as PiCK by DRP user");
                    regProcLogger.info(obj.getString("rid"),
                            "Packet with registrationId '" + messageDTO.getRid() + "' has been marked as PiCK by DRP user",
                            null, null);
                } else if (apiName.equals(ExternalAPIType.UNPICK.toString())) {
                    this.setResponse(ctx, "Packet with registrationId '" + obj.getString("rid") + "' has been marked as UNPiCK by DRP user");
                    regProcLogger.info(obj.getString("rid"),
                            "Packet with registrationId '" + messageDTO.getRid() + "' has been marked as UNPiCK by DRP user",
                            null, null);
                } else if (apiName.equals(ExternalAPIType.GETDATA.toString())) {
                } else if (apiName.equals(ExternalAPIType.LIST.toString())) {
                } else {
                    setErrorResponse(ctx, "Packet with registrationId '" + obj.getString("rid") + "' Invalid API Name");
                    regProcLogger.error(LoggerFileConstant.SESSIONID.toString(),
                            LoggerFileConstant.REGISTRATIONID.toString(), messageDTO.getRid(),
                            "Invalid API Name " + apiName);
                }
            } else {
                setErrorResponse(ctx, "Packet with registrationId '" + obj.getString("rid") + "' has not been forwarded to next stage");
                regProcLogger.info(obj.getString("rid"),
                        "Packet with registrationId '" + messageDTO.getRid() + "' has not been forwarded to next stage",
                        null, null);
            }

        } catch (TablenotAccessibleException e) {
            registrationStatusDto.setStatusCode(RegistrationStatusCode.PROCESSING.toString());
            registrationStatusDto.setStatusComment(
                    trimMessage.trimExceptionMessage(StatusUtil.DB_NOT_ACCESSIBLE.getMessage() + e.getMessage()));
            registrationStatusDto.setSubStatusCode(StatusUtil.DB_NOT_ACCESSIBLE.getCode());
            registrationStatusDto.setLatestTransactionStatusCode(
                    registrationStatusMapperUtil.getStatusCode(RegistrationExceptionTypeCode.DATA_ACCESS_EXCEPTION));
            isTransactionSuccessful = false;
            description.setMessage(PlatformErrorMessages.RPR_RGS_REGISTRATION_TABLE_NOT_ACCESSIBLE.getMessage());
            description.setCode(PlatformErrorMessages.RPR_RGS_REGISTRATION_TABLE_NOT_ACCESSIBLE.getCode());
            regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
                    description.getCode() + " -- " + messageDTO.getRid(),
                    PlatformErrorMessages.RPR_RGS_REGISTRATION_TABLE_NOT_ACCESSIBLE.getMessage() + e.getMessage()
                            + org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
            messageDTO.setIsValid(Boolean.FALSE);
            messageDTO.setInternalError(Boolean.TRUE);
            messageDTO.setRid(registrationStatusDto.getRegistrationId());
            ctx.fail(e);
        } catch (Exception e) {
            regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                    ctx.getBodyAsString(), org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
            messageDTO.setIsValid(Boolean.FALSE);
            isTransactionSuccessful = false;
            description.setCode(PlatformErrorMessages.DRP_STAGE_FAILED.getCode());
            description.setMessage(PlatformErrorMessages.DRP_STAGE_FAILED.getMessage());
            ctx.fail(e);
        } finally {
            if (apiName.equals(ExternalAPIType.SUCCESS.toString()) || apiName.equals(ExternalAPIType.REJECT.toString())) {
                /** Module-Id can be Both Success/Error code */
                String moduleId = isTransactionSuccessful
                        ? PlatformSuccessMessages.RPR_DRP_STAGE_SUCCESS.getCode()
                        : description.getCode();
                String moduleName = ModuleName.DRP.toString();
                registrationStatusService.updateRegistrationStatus(registrationStatusDto, moduleId, moduleName);
                drpService.updateDrpTransaction(drpDto);
                if (isTransactionSuccessful)
                    description.setMessage(PlatformSuccessMessages.RPR_DRP_STAGE_SUCCESS.getMessage());
                String eventId = isTransactionSuccessful ? EventId.RPR_401.toString()
                        : EventId.RPR_405.toString();
                String eventName = isTransactionSuccessful ? EventName.UPDATE.toString()
                        : EventName.EXCEPTION.toString();
                String eventType = isTransactionSuccessful ? EventType.BUSINESS.toString()
                        : EventType.SYSTEM.toString();

                auditLogRequestBuilder.createAuditRequestBuilder(description.getMessage(), eventId, eventName, eventType,
                        moduleId, moduleName, messageDTO.getRid());
            } else if (apiName.equals(ExternalAPIType.PICK.toString()) || apiName.equals(ExternalAPIType.UNPICK.toString())) {
                drpService.updateDrpTransaction(drpDto);
            }
        }
    }

    private Map convertGetDataObject(JSONObject matchedDemographicIdentity) {
        Map dataMap = new HashMap<String, String>();
        try {
            dataMap.put("profession", (String) ((Map<String, String>) ((List) matchedDemographicIdentity.get("profession")).get(0)).get("value"));
        } catch (Exception e) {
            dataMap.put("profession", "Error When Fetching Data");
        }
        try {
            dataMap.put("gender", (String) ((Map<String, String>) ((List) matchedDemographicIdentity.get("gender")).get(0)).get("value"));
        } catch (Exception e) {
            dataMap.put("gender", "Error When Fetching Data");
        }
        try {
            dataMap.put("fullName", (String) ((Map<String, String>) ((List) matchedDemographicIdentity.get("fullName")).get(0)).get("value"));
        } catch (Exception e) {
            dataMap.put("fullName", "Error When Fetching Data");
        }
        try {
            dataMap.put("postalCode", (String) ((Map<String, String>) ((List) matchedDemographicIdentity.get("postalCode")).get(0)).get("value"));
        } catch (Exception e) {
            dataMap.put("postalCode", "Error When Fetching Data");
        }
        try {
            dataMap.put("province", (String) ((Map<String, String>) ((List) matchedDemographicIdentity.get("province")).get(0)).get("value"));
        } catch (Exception e) {
            dataMap.put("province", "Error When Fetching Data");
        }
        try {
            dataMap.put("district", (String) ((Map<String, String>) ((List) matchedDemographicIdentity.get("district")).get(0)).get("value"));
        } catch (Exception e) {
            dataMap.put("district", "Error When Fetching Data");
        }
        try {
            dataMap.put("city", (String) ((Map<String, String>) ((List) matchedDemographicIdentity.get("city")).get(0)).get("value"));
        } catch (Exception e) {
            dataMap.put("city", "Error When Fetching Data");
        }
        try {
            dataMap.put("addressLine1", (String) ((Map<String, String>) ((List) matchedDemographicIdentity.get("addressLine1")).get(0)).get("value"));
        } catch (Exception e) {
            dataMap.put("addressLine1", "Error When Fetching Data");
        }
        try {
            dataMap.put("addressLine2", (String) ((Map<String, String>) ((List) matchedDemographicIdentity.get("addressLine2")).get(0)).get("value"));
        } catch (Exception e) {
            dataMap.put("addressLine2", "Error When Fetching Data");
        }
        try {
            dataMap.put("residenceStatus", (String) ((Map<String, String>) ((List) matchedDemographicIdentity.get("residenceStatus")).get(0)).get("value"));
        } catch (Exception e) {
            dataMap.put("residenceStatus", "Error When Fetching Data");
        }
        try {
            dataMap.put("maritalStatus", (String) ((Map<String, String>) ((List) matchedDemographicIdentity.get("maritalStatus")).get(0)).get("value"));
        } catch (Exception e) {
            dataMap.put("maritalStatus", "Error When Fetching Data");
        }
        try {
            dataMap.put("dateOfBirth", (String) matchedDemographicIdentity.get("dateOfBirth").toString());
        } catch (Exception e) {
            dataMap.put("dateOfBirth", "Error When Fetching Data");
        }
        try {
            dataMap.put("phone", (String) matchedDemographicIdentity.get("phone").toString());
        } catch (Exception e) {
            dataMap.put("phone", "Error When Fetching Data");
        }
        try {
            dataMap.put("nationalIdentityNumber", (String) matchedDemographicIdentity.get("nationalIdentityNumber").toString());
        } catch (Exception e) {
            dataMap.put("nationalIdentityNumber", "Error When Fetching Data");
        }
        try {
            dataMap.put("email", (String) matchedDemographicIdentity.get("email").toString());
        } catch (Exception e) {
            dataMap.put("email", "Error When Fetching Data");
        }
        return dataMap;

    }

    /**
     * This is for failure handler
     *
     * @param routingContext
     */
    private void failure(RoutingContext routingContext) {
        this.setResponse(routingContext, routingContext.failure().getMessage());
    }

    /**
     * sends messageDTO to camel bridge.
     *
     * @param messageDTO the message DTO
     */
    public void sendMessage(MessageDTO messageDTO) {
        this.send(this.mosipEventBus, MessageBusAddress.EXTERNAL_STAGE_BUS_OUT, messageDTO);
    }

    private List populateListApiResponseMock() {
        List arrayList = new ArrayList();

        for (int refId = 1; refId < 11; refId++) {
            ListAPIResponseDTO listAPIResponseDTO = new ListAPIResponseDTO();
            listAPIResponseDTO.setRefId(String.valueOf(refId));
            listAPIResponseDTO.setRid("110011001100" + refId);
            listAPIResponseDTO.setOperatorId("1001");
            listAPIResponseDTO.setCenterId("0001");
            listAPIResponseDTO.setOperatorFlag(0);
            listAPIResponseDTO.setStageFlag(0);
            arrayList.add(listAPIResponseDTO);
        }
        return arrayList;
    }

    /**
     * This should implement with default flow.
     * which means initial data saving part should done from this block
     */
    @Override
    public MessageDTO process(MessageDTO object) {

        TrimExceptionMessage trimExceptionMsg = new TrimExceptionMessage();

        boolean isTransactionSuccessful = false;
        String registrationId = object.getRid();
        object.setMessageBusAddress(MessageBusAddress.EXTERNAL_STAGE_BUS_IN);
        regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
                registrationId, "ExternalStage::process()::entry");
        InternalRegistrationStatusDto registrationStatusDto = registrationStatusService
                .getRegistrationStatus(registrationId);
        DrpDto drpDto = null;
        try {
            registrationStatusDto
                    .setLatestTransactionTypeCode(RegistrationTransactionTypeCode.EXTERNAL_INTEGRATION.toString());
            registrationStatusDto.setRegistrationStageName(this.getClass().getSimpleName());

            Boolean temp = false;
            if (registrationStatusDto != null && registrationId.equals(registrationStatusDto.getRegistrationId())) {
                List<DrpDto> drpDtoList = drpService.getDrpEntryByRegId(registrationId);
                if (drpDtoList != null && !drpDtoList.isEmpty() && drpDtoList.get(0) != null) {
                    drpDto = drpDtoList.get(0);
                    regProcLogger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
                            "",
                            "ExternalStage::process():: Rid Already exist in DRP table : " + drpDto.toString());
                }
            }
            if (drpDto == null) {
                drpDto = new DrpDto();
                drpDto.setDrpId(generateId());
                drpDto.setRegistrationId(registrationStatusDto.getRegistrationId());
                drpDto.setStageFlag(RegistrationStatusCode.PROCESSING.toString());
                drpDto.setOperatorFlag(DrpOperatorStageCode.DEFAULT.toString());
                drpDto.setActive(Boolean.TRUE);
                drpDto.setCenterId("CENTER1");
                drpDto.setOperatorId("OPERATOR1");
                drpService.addDrpTransaction(drpDto);
                temp = true;
            }
            regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
                    "",
                    "ExternalStage::process():: EIS service Api call  ended with response data : " + temp.toString());

            if (temp) {
                registrationStatusDto
                        .setLatestTransactionStatusCode(RegistrationTransactionStatusCode.SUCCESS.toString());
                registrationStatusDto.setStatusComment(StatusUtil.EXTERNAL_STAGE_SUCCESS.getMessage());
                registrationStatusDto.setSubStatusCode(StatusUtil.EXTERNAL_STAGE_SUCCESS.getCode());
                registrationStatusDto.setStatusCode(RegistrationStatusCode.PROCESSING.toString());
                object.setIsValid(true);
                object.setInternalError(false);
                isTransactionSuccessful = true;
                description.setMessage(
                        PlatformSuccessMessages.RPR_EXTERNAL_STAGE_SUCCESS.getMessage() + " -- " + registrationId);
                description.setCode(PlatformSuccessMessages.RPR_EXTERNAL_STAGE_SUCCESS.getCode());

            } else {
                registrationStatusDto.setLatestTransactionStatusCode(registrationStatusMapperUtil
                        .getStatusCode(RegistrationExceptionTypeCode.EXTERNAL_INTEGRATION_FAILED));
                registrationStatusDto.setStatusComment(StatusUtil.EXTERNAL_STAGE_FAILED.getMessage());
                registrationStatusDto.setSubStatusCode(StatusUtil.EXTERNAL_STAGE_FAILED.getCode());
                registrationStatusDto.setStatusCode(RegistrationStatusCode.FAILED.toString());
                object.setIsValid(false);
                object.setInternalError(false);
                description
                        .setMessage(PlatformErrorMessages.EXTERNAL_STAGE_FAILED.getMessage() + " -- " + registrationId);
                description.setCode(PlatformErrorMessages.EXTERNAL_STAGE_FAILED.getCode());
            }
            regProcLogger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
                    registrationId, description.getMessage());
        } catch (Exception e) {
            registrationStatusDto.setStatusComment(
                    trimExceptionMsg.trimExceptionMessage(StatusUtil.UNKNOWN_EXCEPTION_OCCURED + e.getMessage()));
            registrationStatusDto.setSubStatusCode(StatusUtil.UNKNOWN_EXCEPTION_OCCURED.getCode());
            registrationStatusDto.setStatusCode(RegistrationStatusCode.PROCESSING.toString());
            registrationStatusDto.setLatestTransactionStatusCode(registrationStatusMapperUtil
                    .getStatusCode(RegistrationExceptionTypeCode.UNEXCEPTED_ERROR));
            description.setCode(PlatformErrorMessages.RPR_BDD_UNKNOWN_EXCEPTION.getCode());
            description.setMessage(PlatformErrorMessages.RPR_BDD_UNKNOWN_EXCEPTION.getMessage());
            regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), description.getCode(), registrationId,
                    description.getMessage() + e.getMessage() + ExceptionUtils.getStackTrace(e));
            object.setInternalError(true);
            object.setIsValid(false);
        } finally {

            if (object.getInternalError()) {
                registrationStatusDto.setUpdatedBy(USER);
                int retryCount = registrationStatusDto.getRetryCount() != null
                        ? registrationStatusDto.getRetryCount() + 1
                        : 1;

                registrationStatusDto.setRetryCount(retryCount);
            }
            /** Module-Id can be Both Succes/Error code */
            String moduleId = isTransactionSuccessful ? PlatformSuccessMessages.RPR_EXTERNAL_STAGE_SUCCESS.getCode()
                    : description.getCode();
            String moduleName = ModuleName.EXTERNAL.toString();
            registrationStatusService.updateRegistrationStatus(registrationStatusDto, moduleId, moduleName);

            if (isTransactionSuccessful) {
                description.setMessage(PlatformSuccessMessages.RPR_PKR_PACKET_VALIDATE.getMessage());
                description.setCode(PlatformSuccessMessages.RPR_PKR_PACKET_VALIDATE.getCode());
            }
            String eventId = isTransactionSuccessful ? EventId.RPR_402.toString() : EventId.RPR_405.toString();
            String eventName = isTransactionSuccessful ? EventName.UPDATE.toString() : EventName.EXCEPTION.toString();
            String eventType = isTransactionSuccessful ? EventType.BUSINESS.toString() : EventType.SYSTEM.toString();

            auditLogRequestBuilder.createAuditRequestBuilder(description.getMessage(), eventId, eventName, eventType,
                    moduleId, moduleName, registrationId);
        }

        return object;
    }

    public String generateId() {
        return UUID.randomUUID().toString();
    }

    public void setResponse(RoutingContext ctx, Object object) {
        ctx.response().putHeader("content-type", "application/json").putHeader("Access-Control-Allow-Origin", "*")
                .putHeader("Access-Control-Allow-Methods", "GET, POST").setStatusCode(200)
                .end(Json.encodePrettily(object));
    }

    public void setErrorResponse(RoutingContext ctx, Object object) {
        ctx.response().putHeader("content-type", "application/json").putHeader("Access-Control-Allow-Origin", "*")
                .putHeader("Access-Control-Allow-Methods", "GET, POST").setStatusCode(400)
                .end(Json.encodePrettily(object));
    }
}

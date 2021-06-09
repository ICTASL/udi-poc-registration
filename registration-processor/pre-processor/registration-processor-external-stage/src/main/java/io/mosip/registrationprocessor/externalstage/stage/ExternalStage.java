package io.mosip.registrationprocessor.externalstage.stage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.mosip.registration.processor.core.code.*;
import io.mosip.registration.processor.core.constant.RegistrationType;
import io.mosip.registration.processor.status.exception.TablenotAccessibleException;
import io.mosip.registrationprocessor.externalstage.entity.ListAPIResponseDTO;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.processor.core.abstractverticle.MessageBusAddress;
import io.mosip.registration.processor.core.abstractverticle.MessageDTO;
import io.mosip.registration.processor.core.abstractverticle.MosipEventBus;
import io.mosip.registration.processor.core.abstractverticle.MosipRouter;
import io.mosip.registration.processor.core.abstractverticle.MosipVerticleAPIManager;
import io.mosip.registration.processor.core.constant.LoggerFileConstant;
import io.mosip.registration.processor.core.exception.ApisResourceAccessException;
import io.mosip.registration.processor.core.exception.util.PlatformErrorMessages;
import io.mosip.registration.processor.core.exception.util.PlatformSuccessMessages;
import io.mosip.registration.processor.core.logger.LogDescription;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.mosip.registration.processor.core.spi.restclient.RegistrationProcessorRestClientService;
import io.mosip.registration.processor.core.status.util.StatusUtil;
import io.mosip.registration.processor.core.status.util.TrimExceptionMessage;
import io.mosip.registration.processor.core.util.RegistrationExceptionMapperUtil;
import io.mosip.registration.processor.rest.client.audit.builder.AuditLogRequestBuilder;
import io.mosip.registration.processor.status.code.RegistrationStatusCode;
import io.mosip.registration.processor.status.dto.InternalRegistrationStatusDto;
import io.mosip.registration.processor.status.dto.RegistrationStatusDto;
import io.mosip.registration.processor.status.service.RegistrationStatusService;
import io.mosip.registrationprocessor.externalstage.entity.MessageRequestDTO;

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

	/** After this time intervel, message should be considered as expired (In seconds). */
	@Value("${mosip.regproc.external.message.expiry-time-limit}")
	private Long messageExpiryTimeLimit;

	@Autowired
	private AuditLogRequestBuilder auditLogRequestBuilder;

	/**
	 * The registration status service.
	 */
	@Autowired
	private RegistrationStatusService<String, InternalRegistrationStatusDto, RegistrationStatusDto> registrationStatusService;

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
		MessageDTO messageDTO = new MessageDTO();
		String type = "";
		TrimExceptionMessage trimMessage = new TrimExceptionMessage();
		LogDescription description = new LogDescription();
		boolean isTransactionSuccessful = false;

		try {
			JsonObject obj = ctx.getBodyAsJson();
			type = obj.getString("apiName");
			JsonObject requestJson = obj.getJsonObject("request");

			messageDTO.setMessageBusAddress(MessageBusAddress.EXTERNAL_STAGE_BUS_IN);
			messageDTO.setInternalError(Boolean.FALSE);
			messageDTO.setRid(requestJson.getString("rid"));
			messageDTO.setReg_type(RegistrationType.valueOf(requestJson.getString("reg_type")));
			messageDTO.setIsValid(requestJson.getBoolean("isValid"));

			if (type != null && type != "" && (type.equals(ExternalAPIType.SUCCESS) || type.equals(ExternalAPIType.REJECT))) {
				registrationStatusDto = registrationStatusService.getRegistrationStatus(messageDTO.getRid());
			}


			if (type != null && type != "" && type.equals(ExternalAPIType.LIST)) {
				ListAPIResponseDTO listAPIResponseDTO = new ListAPIResponseDTO();
				List<ListAPIResponseDTO> list = populateListApiResponseMock();
				this.setResponse(ctx, list);
			} else if (type != null && type != "" && type.equals(ExternalAPIType.SUCCESS)) {
				if (registrationStatusDto != null && messageDTO.getRid().equalsIgnoreCase(registrationStatusDto.getRegistrationId())) {
					registrationStatusDto.setLatestTransactionTypeCode(RegistrationTransactionTypeCode.EXTERNAL_INTEGRATION.toString());
					registrationStatusDto.setRegistrationStageName(this.getClass().getSimpleName());

					registrationStatusDto.setLatestTransactionStatusCode(RegistrationTransactionStatusCode.SUCCESS.toString());
					messageDTO.setIsValid(Boolean.TRUE);
					registrationStatusDto.setStatusComment(StatusUtil.EXTERNAL_STAGE_SUCCESS.getMessage());
					registrationStatusDto.setSubStatusCode(StatusUtil.EXTERNAL_STAGE_SUCCESS.getCode());
					registrationStatusDto.setStatusCode(RegistrationStatusCode.PROCESSING.toString());

					isTransactionSuccessful = true;
					description.setMessage(PlatformSuccessMessages.RPR_EXTERNAL_STAGE_SUCCESS.getMessage() + " -- " + messageDTO.getRid());
					description.setCode(PlatformSuccessMessages.RPR_EXTERNAL_STAGE_SUCCESS.getCode());

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

				if (messageDTO.getIsValid()) {
					sendMessage(messageDTO);
					this.setResponse(ctx, "Packet with registrationId '" + messageDTO.getRid() + "' has been forwarded to next stage");
					regProcLogger.info(obj.getString("rid"),
							"Packet with registrationId '" + messageDTO.getRid() + "' has been forwarded to next stage", null,
							null);
				} else {
					this.setResponse(ctx, "Packet with registrationId '" + obj.getString("rid") + "' has not been uploaded to file System");
					regProcLogger.info(obj.getString("rid"),
							"Packet with registrationId '" + messageDTO.getRid() + "' has not been uploaded to file System",
							null, null);
				}

			} else if (type != null && type != "" && type.equals(ExternalAPIType.REJECT)) {

				if (registrationStatusDto != null && messageDTO.getRid().equalsIgnoreCase(registrationStatusDto.getRegistrationId())) {
					registrationStatusDto.setLatestTransactionTypeCode(RegistrationTransactionTypeCode.EXTERNAL_INTEGRATION.toString());
					registrationStatusDto.setRegistrationStageName(this.getClass().getSimpleName());

					registrationStatusDto.setLatestTransactionStatusCode(RegistrationTransactionStatusCode.SUCCESS.toString());
					messageDTO.setIsValid(Boolean.TRUE);
					registrationStatusDto.setStatusComment(StatusUtil.EXTERNAL_STAGE_SUCCESS.getMessage());
					registrationStatusDto.setSubStatusCode(StatusUtil.EXTERNAL_STAGE_SUCCESS.getCode());
					registrationStatusDto.setStatusCode(RegistrationStatusCode.PROCESSING.toString());

					isTransactionSuccessful = true;
					description.setMessage(PlatformSuccessMessages.RPR_EXTERNAL_STAGE_SUCCESS.getMessage() + " -- " + messageDTO.getRid());
					description.setCode(PlatformSuccessMessages.RPR_EXTERNAL_STAGE_SUCCESS.getCode());

					regProcLogger.info(LoggerFileConstant.SESSIONID.toString(),
							LoggerFileConstant.REGISTRATIONID.toString(), messageDTO.getRid(),
							description.getCode() + description.getMessage());

					registrationStatusDto.setStatusCode(RegistrationStatusCode.REJECTED.toString());
					registrationStatusDto.setStatusComment(
							trimMessage.trimExceptionMessage(StatusUtil.PACKET_REJECTED.getMessage()));
					registrationStatusDto.setSubStatusCode(StatusUtil.PACKET_REJECTED.getCode());
					registrationStatusDto.setLatestTransactionStatusCode(RegistrationTransactionStatusCode.REJECTED.toString());
					isTransactionSuccessful = false;
					description.setMessage(PlatformErrorMessages.RPR_PVM_PACKET_REJECTED.getMessage());
					description.setCode(PlatformErrorMessages.RPR_PVM_PACKET_REJECTED.getCode());

					regProcLogger.info(LoggerFileConstant.SESSIONID.toString(),
							LoggerFileConstant.REGISTRATIONID.toString(), messageDTO.getRid(),
							description.getCode() + description.getMessage());

					messageDTO.setIsValid(Boolean.FALSE);
					messageDTO.setInternalError(Boolean.TRUE);
					messageDTO.setRid(registrationStatusDto.getRegistrationId());
					ctx.fail(0);
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
						"API Name Invalid");
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
			description.setCode(PlatformErrorMessages.EXTERNAL_STAGE_FAILED.getCode());
			description.setMessage(PlatformErrorMessages.EXTERNAL_STAGE_FAILED.getMessage());
			ctx.fail(e);
		} finally {
			if (messageDTO.getInternalError()) {
				registrationStatusDto.setUpdatedBy(USER);
				int retryCount = registrationStatusDto.getRetryCount() != null
						? registrationStatusDto.getRetryCount() + 1
						: 1;
				registrationStatusDto.setRetryCount(retryCount);
			}

			/** Module-Id can be Both Success/Error code */
			String moduleId = isTransactionSuccessful
					? PlatformSuccessMessages.RPR_EXTERNAL_STAGE_SUCCESS.getCode()
					: description.getCode();
			String moduleName = ModuleName.EXTERNAL.toString();
			registrationStatusService.updateRegistrationStatus(registrationStatusDto, moduleId, moduleName);
			if (isTransactionSuccessful)
				description.setMessage(PlatformSuccessMessages.RPR_EXTERNAL_STAGE_SUCCESS.getMessage());
			String eventId = isTransactionSuccessful ? EventId.RPR_401.toString()
					: EventId.RPR_405.toString();
			String eventName = isTransactionSuccessful ? EventName.GET.toString()
					: EventName.EXCEPTION.toString();
			String eventType = isTransactionSuccessful ? EventType.BUSINESS.toString()
					: EventType.SYSTEM.toString();

			auditLogRequestBuilder.createAuditRequestBuilder(description.getMessage(), eventId, eventName, eventType,
					moduleId, moduleName, messageDTO.getRid());
		}
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
		this.send(this.mosipEventBus, MessageBusAddress.SECUREZONE_NOTIFICATION_OUT, messageDTO);
	}


	/**
	 * This should implement with default flow.
	 * which means initial data saving part should done from this block
	 */
	@Override
	public MessageDTO process(MessageDTO object) {
		return null;
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
}

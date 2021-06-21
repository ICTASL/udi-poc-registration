package io.mosip.registrationprocessor.externalstage.service;

import io.mosip.registrationprocessor.externalstage.DrpDto;
import io.mosip.registrationprocessor.externalstage.entity.DrpEntity;

import java.util.List;

public interface DrpService<U> {
    public DrpEntity addDrpTransaction(U DrpDto);

    public DrpEntity updateDrpTransaction(U DrpDto);

    public List<DrpDto> getRIDList(U DrpDto);

    public List<DrpDto> getDrpEntryByRegId(String registrationId);
}

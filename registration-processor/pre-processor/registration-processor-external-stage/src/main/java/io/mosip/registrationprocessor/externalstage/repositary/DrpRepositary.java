package io.mosip.registrationprocessor.externalstage.repositary;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.registrationprocessor.externalstage.entity.DrpEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrpRepositary<T extends DrpEntity, E> extends BaseRepository<T, E> {
    @Query("SELECT d FROM DrpEntity d WHERE d.stageFlag=:stageFlag  and (d.operatorFlag=:operatorFlag or d.operatorId=:operatorId) and d.isDeleted IS NULL and d.isActive=TRUE order by d.operatorFlag DESC, d.registrationId ASC")
    public List<DrpEntity> getRIDList(@Param("stageFlag") String stageFlag, @Param("operatorFlag") String operatorFlag, @Param("operatorId") String operatorId);

    @Query("SELECT d FROM DrpEntity d WHERE d.registrationId=:registrationId and d.isDeleted IS NULL and d.isActive=TRUE ")
    public List<DrpEntity> getDrpEntryByRegId(@Param("registrationId") String registrationId);
}

package com.green.jobdone.business;

import com.green.jobdone.business.model.BusinessContentsPostReq;
import com.green.jobdone.entity.Business;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {

    @Query("select b.user.userId from Business b where b.businessId=:businessId")
    Long findUserIdByBusinessId(@Param("businessId") Long businessId);

    @Query("select b.businessId from Business b where b.user.userId=:UserId")
    Long findBusinessIdByUserId(@Param("UserId") Long UserId);


    @Modifying
    @Query("update Business b set b.title=:#{#p.title} ,b.contents=:#{#p.contents} where b.businessId=:#{#p.businessId}")
    void updateBusinessContents(@Param("p") BusinessContentsPostReq p);


    @Query("select count(s.serviceId) from Service s " +
            "join s.product p " +
            "join p.business b " +
            "where b.businessId = :businessId")
    Integer countBusinessServices(@Param("businessId") Long businessId);
}

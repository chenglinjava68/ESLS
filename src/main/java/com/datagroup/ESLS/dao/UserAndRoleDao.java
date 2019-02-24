package com.datagroup.ESLS.dao;

import com.datagroup.ESLS.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserAndRoleDao extends JpaRepository<UserRole,Long> {
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO user_role(roleId,userId) SELECT :roleId , :userId FROM DUAL WHERE NOT EXISTS(SELECT * FROM user_role b WHERE b.roleId=:roleId AND b.userId=:userId)",nativeQuery = true)
    Integer insertByCondition(@Param("roleId") Long roleId,@Param("userId") Long userId);
    @Transactional
    @Modifying
    Integer deleteByUserIdAndRoleId(Long userId,Long roleId);
    UserRole findByUserIdAndRoleId(Long userId,Long roleId);
}

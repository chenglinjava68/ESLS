package com.datagroup.ESLS.dao;

import com.datagroup.ESLS.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface RoleAndPermissionDao extends JpaRepository<RolePermission,Long> {
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO role_permission(role_id,permission_id) SELECT :role_id , :permission_id FROM DUAL WHERE NOT EXISTS(SELECT * FROM role_permission b WHERE b.role_id=:role_id AND b.permission_id=:permission_id)",nativeQuery = true)
    Integer insertByCondition(@Param("role_id") Long role_id, @Param("permission_id") Long permission_id);
}

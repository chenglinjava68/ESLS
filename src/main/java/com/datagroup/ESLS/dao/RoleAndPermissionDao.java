package com.datagroup.ESLS.dao;

import com.datagroup.ESLS.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface RoleAndPermissionDao extends JpaRepository<RolePermission,Long> {
    RolePermission findByRoleIdAndPermissionId(Long roleId, Long permissionId);
}

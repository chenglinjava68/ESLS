package com.datagroup.ESLS.dao;

import com.datagroup.ESLS.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionDao  extends JpaRepository<Permission,Long> {
}

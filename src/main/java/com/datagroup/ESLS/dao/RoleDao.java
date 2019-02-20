package com.datagroup.ESLS.dao;

import com.datagroup.ESLS.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleDao  extends JpaRepository<Role,Long> {
}

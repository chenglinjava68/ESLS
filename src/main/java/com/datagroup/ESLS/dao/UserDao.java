package com.datagroup.ESLS.dao;

import com.datagroup.ESLS.entity.Permission;
import com.datagroup.ESLS.entity.Role;
import com.datagroup.ESLS.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserDao extends JpaRepository<User,Long> {
    @Query(value = "SELECT t.* FROM user_role AS u,role_permission AS r ,permission t WHERE u.user_id =?1 AND u.role_id = r.role_id AND r.permission_id = t.id",nativeQuery = true)
    List<Permission> findPermissionByUserId(Long userId);
    @Query(value = "SELECT p.* FROM permission AS p INNER JOIN role_permission AS rp ON p.id = rp.permission_id INNER JOIN user_role AS ur ON ur.role_id = rp.role_id WHERE ur.user_id = ?1",nativeQuery = true)
    List<Role> findRolesByUserId(Long userId);
    User findByName(String name);
}

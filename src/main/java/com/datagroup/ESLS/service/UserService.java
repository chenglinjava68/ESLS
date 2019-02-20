package com.datagroup.ESLS.service;

import com.datagroup.ESLS.dto.UserVo;
import com.datagroup.ESLS.entity.Permission;
import com.datagroup.ESLS.entity.Role;
import com.datagroup.ESLS.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService extends Service{
    List<Permission> findPermissionByUserId(Long userId);
    List<Role> findRolesByUserId(Long userId);
    User findByName(String name);
    Optional<User> findById(Long id);
    boolean registerUser(UserVo userVo);
}

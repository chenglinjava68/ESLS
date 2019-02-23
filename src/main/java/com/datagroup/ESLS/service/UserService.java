package com.datagroup.ESLS.service;

import com.datagroup.ESLS.dto.UserVo;
import com.datagroup.ESLS.entity.Permission;
import com.datagroup.ESLS.entity.Role;
import com.datagroup.ESLS.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService extends Service{
    List<User> findAll();
    List<User> findAll(Integer page, Integer count);
    boolean deleteById(Long id);
    List<Permission> findPermissionByUserId(Long userId);
    List<Role> findRolesByUserId(Long userId);
    User findByName(String name);
    User findById(Long id);
    User registerUser(UserVo userVo);
}

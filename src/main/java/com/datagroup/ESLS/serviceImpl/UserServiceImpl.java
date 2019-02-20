package com.datagroup.ESLS.serviceImpl;

import com.datagroup.ESLS.dao.UserDao;
import com.datagroup.ESLS.dto.UserVo;
import com.datagroup.ESLS.entity.Permission;
import com.datagroup.ESLS.entity.Role;
import com.datagroup.ESLS.entity.User;
import com.datagroup.ESLS.service.UserService;
import com.datagroup.ESLS.utils.MD5Util;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service("UserService")
public class UserServiceImpl extends BaseServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;
    @Override
    public List<Permission> findPermissionByUserId(Long userId) {
        return userDao.findPermissionByUserId(userId);
    }

    @Override
    public List<Role> findRolesByUserId(Long userId) {
        return userDao.findRolesByUserId(userId);
    }

    @Override
    public User findByName(String name) {
        return userDao.findByName(name);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userDao.findById(id);
    }

    @Override
    public boolean registerUser(UserVo userVo) {
        User u = findByName(userVo.getName());
        if(u!=null)
            return false;
        else{
            User user = new User();
            BeanUtils.copyProperties(userVo, user);
            ByteSource credentialsSalt = ByteSource.Util.bytes(userVo.getName());
            // 加盐加密
            Object obj = new SimpleHash("MD5", userVo.getPasswd(), credentialsSalt, MD5Util.HASHITERATIONS);
            user.setPasswd(((SimpleHash) obj).toHex());
            // 非禁用
            user.setStatus((byte) 1);
            user.setCreateTime(new Timestamp(System.currentTimeMillis()));
            userDao.save(user);
            return true;
        }
    }
}

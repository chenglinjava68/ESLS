package com.datagroup.ESLS.serviceImpl;

import com.datagroup.ESLS.dao.PermissionDao;
import com.datagroup.ESLS.entity.Permission;
import com.datagroup.ESLS.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PermissionServiceImpl extends BaseServiceImpl implements PermissionService {
    @Autowired
    private PermissionDao permissionDao;

    @Override
    public List<Permission> findAll() {
        return permissionDao.findAll();
    }
    @Override
    public List<Permission> findAll(Integer page, Integer count) {
        List<Permission> content = permissionDao.findAll(PageRequest.of(page, count, Sort.Direction.DESC, "id")).getContent();
        return content;
    }
    @Override
    public Permission saveOne(Permission permission) {
        return permissionDao.save(permission);
    }

    @Override
    public Optional<Permission> findById(Long id) {
        return permissionDao.findById(id);
    }

    @Override
    public boolean deleteById(Long id) {
        try{
            permissionDao.deleteById(id);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }
}

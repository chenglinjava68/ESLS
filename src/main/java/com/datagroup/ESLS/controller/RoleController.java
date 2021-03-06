package com.datagroup.ESLS.controller;

import com.datagroup.ESLS.aop.Log;
import com.datagroup.ESLS.common.constant.TableConstant;
import com.datagroup.ESLS.common.response.ResponseBean;
import com.datagroup.ESLS.common.response.ResultBean;
import com.datagroup.ESLS.dao.RoleAndPermissionDao;
import com.datagroup.ESLS.dao.UserAndRoleDao;
import com.datagroup.ESLS.entity.*;
import com.datagroup.ESLS.service.PermissionService;
import com.datagroup.ESLS.service.RoleService;
import com.datagroup.ESLS.service.UserService;
//import com.datagroup.ESLS.shiro.FilterChainDefinitionsService;
import com.datagroup.ESLS.shiro.ShiroService;
import com.datagroup.ESLS.utils.ConditionUtil;
import io.swagger.annotations.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Api(description ="角色管理")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class RoleController {

    @Autowired
    private RoleService roleService;
    @Autowired
    private UserAndRoleDao userAndRoleDao;
    @Autowired
    private RoleAndPermissionDao roleAndPermissionDao;
    @Autowired
    private UserService userService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private ShiroService shiroService;
    @ApiOperation(value = "根据条件获取角色信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "query", value = " 查询条件 可为所有字段", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "queryString", value = "查询条件的字符串", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "页码", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "count", value = "数量", dataType = "int", paramType = "query")
    })
    @GetMapping("/roles")
    @Log("获取角色数据")
    @RequiresPermissions("系统菜单")
    public ResponseEntity<ResultBean> getRoles(@RequestParam(required = false) String query, @RequestParam(required = false) String queryString, @Min(message = "data.page.min", value = 0)@RequestParam(required = false) Integer page, @Min(message = "data.count.min", value = 0) @RequestParam(required = false) Integer count) {
        String result = ConditionUtil.judgeArgument(query, queryString, page, count);
        if(result==null)
            return new ResponseEntity<>(ResultBean.error("参数组合有误 [query和queryString必须同时提供] [page和count必须同时提供]"), HttpStatus.OK);
        // 查询全部
        if(result.equals(ConditionUtil.QUERY_ALL)) {
            List<Role> list = roleService.findAll();
            return new ResponseEntity<>(new ResultBean(list, list.size()), HttpStatus.OK);
        }
        // 查询全部分页
        if(result.equals(ConditionUtil.QUERY_ALL_PAGE)){
            List<Role> list = roleService.findAll();
            return new ResponseEntity<>(new ResultBean(list, list.size()), HttpStatus.OK);
        }
        // 带条件查询全部
        if(result.equals(ConditionUtil.QUERY_ATTRIBUTE_ALL)) {
            List content = roleService.findAllBySql(TableConstant.TABLE_ROLES, query, queryString,Role.class);
            return new ResponseEntity<>(new ResultBean(content, content.size()), HttpStatus.OK);
        }
        // 带条件查询分页
        if(result.equals(ConditionUtil.QUERY_ATTRIBUTE_PAGE)) {
            List<Role> list = roleService.findAll();
            List content = roleService.findAllBySql(TableConstant.TABLE_ROLES, query, queryString, page, count,Role.class);
            return new ResponseEntity<>(new ResultBean(content, list.size()), HttpStatus.OK);
        }
        return new ResponseEntity<>(ResultBean.error("查询组合出错 函数未执行！"), HttpStatus.OK);
    }

    @ApiOperation(value = "获取指定ID的角色信息")
    @GetMapping("/role/{id}")
    @Log("获取指定ID的角色信息")
    @RequiresPermissions("获取指定ID的信息")
    public ResponseEntity<ResultBean> getRoleById(@PathVariable Long id) {
        Optional<Role> result = roleService.findById(id);
        if (result.isPresent()) {
            return new ResponseEntity<>(new ResultBean(result), HttpStatus.OK);
        }
        return new ResponseEntity<>(ResultBean.error("此ID角色不存在"), HttpStatus.BAD_REQUEST);
    }

    @ApiOperation(value = "添加或修改角色信息")
    @PostMapping("/role")
    @Log("添加或修改角色信息")
    @RequiresPermissions("添加或修改信息")
    public ResponseEntity<ResultBean> saveRole(@ApiParam("角色描述") @RequestParam String name,@ApiParam("角色类型")  @RequestParam String type) {
        Role role = new Role(name,type);
        return new ResponseEntity<>(new ResultBean(roleService.saveOne(role)),HttpStatus.OK);
    }

    @ApiOperation(value = "根据ID删除角色信息")
    @DeleteMapping("/role/{id}")
    @Log("根据ID删除角色信息")
    @RequiresPermissions("删除指定ID的信息")
    public ResponseEntity<ResultBean> deleteRoleById(@PathVariable Long id) {
        // 删除角色表
        boolean flag = roleService.deleteById(id);
        // 可批量删除
        List<Long> idList = new ArrayList<>();
        idList.add(id);
        // 删除user_role 和role_permission表中数据
        roleService.deleteByIdList(TableConstant.TABLE_USER_ROLE,"role_id",idList);
        roleService.deleteByIdList(TableConstant.TABLE_ROLE_PERMISSION,"role_id",idList);
        if (flag)
            return new ResponseEntity<>(ResultBean.success("删除成功"), HttpStatus.OK);
        return new ResponseEntity<>(ResultBean.success("删除失败！没有指定ID的角色"), HttpStatus.BAD_REQUEST);
    }
    @ApiOperation("为指定ID的角色添加权限")
    @PostMapping("/role/addPerm/{id}")
    @RequiresPermissions("为指定ID的角色添加权限")
    public ResponseEntity<ResultBean> addRoleAndPermission(@PathVariable Long id,@RequestBody @ApiParam("权限信息ID集合") List<Long> longList) {
        int sum = 0;
        Optional<Role> role = roleService.findById(id);
        if(!role.isPresent())
            return new ResponseEntity<>(ResultBean.error("角色不存在"), HttpStatus.BAD_REQUEST);
        for(Long permissonId:longList) {
            Optional<Permission> permission = permissionService.findById(permissonId);
            if(permission.isPresent()) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRoleId(id);
                rolePermission.setPermissionId(permissonId);
                if(roleAndPermissionDao.findByRoleIdAndPermissionId(id,permissonId)==null){
                    roleAndPermissionDao.save(rolePermission);
                    sum++;
                }
            }

        }
        shiroService.updatePermission();
        return new ResponseEntity<>(ResultBean.success(new ResponseBean(longList.size(),sum)), HttpStatus.OK);
    }
    @ApiOperation("为指定ID的用户添加角色")
    @PostMapping("/role/addRole/{id}")
    @RequiresPermissions("为指定ID的用户添加角色")
    public ResponseEntity<ResultBean> addUserAndRole(@PathVariable Long id,@RequestBody @ApiParam("角色信息ID集合") List<Long> longList) {
        int sum = 0;
        User user = userService.findById(id);
        if(user==null) {
            return new ResponseEntity<>(ResultBean.error("用户不存在"), HttpStatus.BAD_REQUEST);
        }
        for(Long roleId:longList) {
            // 判断角色是否存在
            Optional<Role> role = roleService.findById(roleId);
            if(role.isPresent()) {
                if(userAndRoleDao.insertByCondition(roleId,id)>0)
                    sum++;
            }
        }
        return new ResponseEntity<>(ResultBean.success(new ResponseBean(longList.size(),sum)), HttpStatus.OK);
    }
}

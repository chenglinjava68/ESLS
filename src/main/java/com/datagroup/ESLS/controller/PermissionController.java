package com.datagroup.ESLS.controller;

import com.datagroup.ESLS.aop.Log;
import com.datagroup.ESLS.common.constant.TableConstant;
import com.datagroup.ESLS.common.response.ResultBean;
import com.datagroup.ESLS.dao.RoleAndPermissionDao;
import com.datagroup.ESLS.entity.Permission;
import com.datagroup.ESLS.entity.Role;
import com.datagroup.ESLS.entity.Tag;
import com.datagroup.ESLS.service.PermissionService;
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

@Api(description ="权限管理")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class PermissionController {

    @Autowired
    private PermissionService permissionService;
    @Autowired
  private ShiroService shiroService;
    @ApiOperation(value = "根据条件获取权限信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "query", value = " 查询条件 可为所有字段", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "queryString", value = "查询条件的字符串", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "页码", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "count", value = "数量", dataType = "int", paramType = "query")
    })
    @GetMapping("/permissions")
    @Log("获取权限数据")
    @RequiresPermissions("系统菜单")
    public ResponseEntity<ResultBean> getPermissions(@RequestParam(required = false) String query, @RequestParam(required = false) String queryString, @Min(message = "data.page.min", value = 0)@RequestParam(required = false) Integer page, @Min(message = "data.count.min", value = 0) @RequestParam(required = false) Integer count) {
        String result = ConditionUtil.judgeArgument(query, queryString, page, count);
        if(result==null)
            return new ResponseEntity<>(ResultBean.error("参数组合有误 [query和queryString必须同时提供] [page和count必须同时提供]"), HttpStatus.OK);
        // 查询全部
        if(result.equals(ConditionUtil.QUERY_ALL)) {
            List<Permission> list = permissionService.findAll();
            return new ResponseEntity<>(new ResultBean(list, list.size()), HttpStatus.OK);
        }
        // 查询全部分页
        if(result.equals(ConditionUtil.QUERY_ALL_PAGE)){
            List<Permission> list = permissionService.findAll();
            return new ResponseEntity<>(new ResultBean(list, list.size()), HttpStatus.OK);
        }
        // 带条件查询全部
        if(result.equals(ConditionUtil.QUERY_ATTRIBUTE_ALL)) {
            List content = permissionService.findAllBySql(TableConstant.TABLE_PERMISSION, query, queryString,Permission.class);
            return new ResponseEntity<>(new ResultBean(content, content.size()), HttpStatus.OK);
        }
        // 带条件查询分页
        if(result.equals(ConditionUtil.QUERY_ATTRIBUTE_PAGE)) {
            List<Permission> list = permissionService.findAll();
            List content = permissionService.findAllBySql(TableConstant.TABLE_PERMISSION, query, queryString, page, count,Permission.class);
            return new ResponseEntity<>(new ResultBean(content, list.size()), HttpStatus.OK);
        }
        return new ResponseEntity<>(ResultBean.error("查询组合出错 函数未执行！"), HttpStatus.OK);
    }

    @ApiOperation(value = "获取指定ID的权限信息")
    @GetMapping("/permission/{id}")
    @Log("获取指定ID的权限信息")
    @RequiresPermissions("获取指定ID的信息")
    public ResponseEntity<ResultBean> getPermissionById(@PathVariable Long id) {
        Optional<Permission> result = permissionService.findById(id);
        if (result.isPresent()) {
            return new ResponseEntity<>(new ResultBean(result), HttpStatus.OK);
        }
        return new ResponseEntity<>(ResultBean.error("此ID权限不存在"), HttpStatus.BAD_REQUEST);
    }

    @ApiOperation(value = "添加或修改权限信息")
    @PostMapping("/permission")
    @Log("添加或修改权限信息")
    @RequiresPermissions("添加或修改信息")
    public ResponseEntity<ResultBean> savePermission(@ApiParam(value = "权限信息描述") @RequestParam String name,@ApiParam(value = "权限信息url") @RequestParam String url) {
        Permission permission = new Permission(name,url);
        Permission result = permissionService.saveOne(permission);
       shiroService.updatePermission();
        return new ResponseEntity<>(new ResultBean(result),HttpStatus.OK);
    }

    @ApiOperation(value = "根据ID删除权限信息")
    @DeleteMapping("/permission/{id}")
    @Log("根据ID删除权限信息")
    @RequiresPermissions("删除指定ID的信息")
    public ResponseEntity<ResultBean> deletePermissionById(@PathVariable Long id) {
        List<Long> idList = new ArrayList<>();
        idList.add(id);
        permissionService.deleteByIdList(TableConstant.TABLE_ROLE_PERMISSION,"permission_id",idList);
        boolean flag = permissionService.deleteById(id);
        if (flag)
            return new ResponseEntity<>(ResultBean.success("删除成功"), HttpStatus.OK);
        return new ResponseEntity<>(ResultBean.success("删除失败！没有指定ID的权限"), HttpStatus.BAD_REQUEST);
    }
}

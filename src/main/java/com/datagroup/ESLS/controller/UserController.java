package com.datagroup.ESLS.controller;

import com.datagroup.ESLS.aop.Log;
import com.datagroup.ESLS.common.constant.TableConstant;
import com.datagroup.ESLS.common.request.RequestBean;
import com.datagroup.ESLS.common.request.RequestItem;
import com.datagroup.ESLS.common.response.ResponseBean;
import com.datagroup.ESLS.common.response.ResultBean;
import com.datagroup.ESLS.dao.RoleDao;
import com.datagroup.ESLS.dao.UserAndRoleDao;
import com.datagroup.ESLS.dto.UserVo;
import com.datagroup.ESLS.entity.*;
import com.datagroup.ESLS.redis.RedisUtil;
import com.datagroup.ESLS.service.UserService;
import com.datagroup.ESLS.utils.ConditionUtil;
import com.datagroup.ESLS.utils.CopyUtil;
import com.datagroup.ESLS.utils.SpringContextUtil;
import com.datagroup.ESLS.utils.ValidatorUtil;
import io.swagger.annotations.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@RestController
@Api(description = "用户管理API")
@CrossOrigin(origins = "*", maxAge = 3600)
@Validated
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private UserAndRoleDao userAndRoleDao;
    @ApiOperation(value = "根据条件获取用户信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "query", value = "查询条件 可为所有字段 分隔符为单个空格 ", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "queryString", value = "查询条件的字符串", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "页码", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "count", value = "数量", dataType = "int", paramType = "query")
    })
    @GetMapping("/users")
    @Log("获取用户信息")
    public ResponseEntity<ResultBean> getGoods(@RequestParam(required = false) String query, @RequestParam(required = false) String queryString, @Min(message = "data.page.min", value = 0)@RequestParam(required = false) Integer page, @RequestParam(required = false) @Min(message = "data.count.min", value = 0)Integer count) {
        String result = ConditionUtil.judgeArgument(query, queryString, page, count);
        if(result==null)
            return new ResponseEntity<>(ResultBean.error("参数组合有误 [query和queryString必须同时提供] [page和count必须同时提供]"), HttpStatus.BAD_REQUEST);
        // 带条件或查询
        if(query!=null && query.contains(" ")){
            List content = userService.findAllBySql(TableConstant.TABLE_USER, "like", query, queryString, page, count, User.class);
            return new ResponseEntity<>(new ResultBean(content, content.size()), HttpStatus.OK);
        }
        // 查询全部
        if(result.equals(ConditionUtil.QUERY_ALL)) {
            List list = userService.findAll();
            List content = CopyUtil.copyUser(list);
            return new ResponseEntity<>(new ResultBean(content, list.size()), HttpStatus.OK);
        }
        // 查询全部分页
        if(result.equals(ConditionUtil.QUERY_ALL_PAGE)){
            List list = userService.findAll();
            List content = userService.findAll(page, count);
            return new ResponseEntity<>(new ResultBean( CopyUtil.copyUser(content), list.size()), HttpStatus.OK);
        }
        // 带条件查询全部
        if(result.equals(ConditionUtil.QUERY_ATTRIBUTE_ALL)) {
            List content = userService.findAllBySql(TableConstant.TABLE_USER, query, queryString,User.class);
            return new ResponseEntity<>(new ResultBean( CopyUtil.copyUser(content), content.size()), HttpStatus.OK);
        }
        // 带条件查询分页
        if(result.equals(ConditionUtil.QUERY_ATTRIBUTE_PAGE)) {
            List list = userService.findAll();
            List content = userService.findAllBySql(TableConstant.TABLE_USER, query, queryString, page, count,User.class);
            return new ResponseEntity<>(new ResultBean( CopyUtil.copyUser(content), list.size()), HttpStatus.OK);
        }
        return new ResponseEntity<>(ResultBean.error("查询组合出错 函数未执行！"), HttpStatus.OK);
    }
    @ApiOperation(value = "获取指定ID的用户信息信息")
    @GetMapping("/users/{id}")
    @Log("获取指定ID的用户信息")
    @Transactional
    @RequiresPermissions("获取指定ID的信息")
    public ResponseEntity<ResultBean> getGoodById(@PathVariable Long id) {
        User user = userService.findById(id);
        if(user==null)
            return new ResponseEntity<>(ResultBean.error("此ID用户不存在"),HttpStatus.BAD_REQUEST);
        List users = new ArrayList<>();
        users.add(user);
        return new ResponseEntity<>(new ResultBean(users),HttpStatus.OK);
    }

    @ApiOperation(value = "根据ID删除用户信息")
    @DeleteMapping("/user/{id}")
    @Log("根据ID删除用户信息")
    @RequiresPermissions("删除指定ID的信息")
    public ResponseEntity<ResultBean> deleteGoodById(@PathVariable Long id) {
        boolean flag = userService.deleteById(id);
        if(flag)
            return new ResponseEntity<>(ResultBean.success("删除成功"),HttpStatus.OK);
        return new ResponseEntity<>(ResultBean.success("删除失败！没有指定ID的用户"),HttpStatus.BAD_REQUEST);
    }
    @ApiOperation(value = "根据用户ID获得用户角色")
    @GetMapping("/user/role/{id}")
    @Log("根据用户ID获得用户角色")
    public ResponseEntity<ResultBean> getRolesByUserId(@PathVariable Long id) {
        User user = userService.findById(id);
        if(user==null)
            return new ResponseEntity<>(ResultBean.error("获取失败！没有指定ID的用户"),HttpStatus.BAD_REQUEST);
        else
            return new ResponseEntity<>(ResultBean.success(user.getRoleList()),HttpStatus.OK);

    }
    @ApiOperation(value = "根据用户ID删除用户角色")
    @PostMapping("/user/delete/{id}")
    @Log("根据用户ID删除用户角色")
    public ResponseEntity<ResultBean> getRolesByUserId(@PathVariable Long id,@RequestBody @ApiParam("用户ID集合")List<Long> roleIds) {
        User user = userService.findById(id);
        int successNumber = 0;
        if(user==null)
            return new ResponseEntity<>(ResultBean.error("获取失败！没有指定ID的用户"),HttpStatus.BAD_REQUEST);
        else {
            for( Long roleId:roleIds){
                if(!roleDao.findById(roleId).isPresent())
                    continue;
                Integer result = userAndRoleDao.deleteByUserIdAndRoleId(id, roleId);
                if(result!=null  && result>0)
                    successNumber++;
            }
            return new ResponseEntity<>(ResultBean.success(new ResponseBean(roleIds.size(),successNumber)), HttpStatus.OK);
        }

    }
    @ApiOperation(value = "user登录")
    @PostMapping("/user/login")
    public ResponseEntity<ResultBean> login(@Valid @RequestBody @ApiParam(value = "用户信息json格式") Admin adminEntity, BindingResult error) {
        if(error.hasErrors())
            return new ResponseEntity<>(ResultBean.error(ValidatorUtil.getError(error)), HttpStatus.BAD_REQUEST);
        User admin = userService.findByName(adminEntity.getUsername());
        if (admin != null) {
            UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(adminEntity.getUsername(), adminEntity.getPassword());
            SecurityUtils.getSubject().login(usernamePasswordToken);
            HttpHeaders responseHeaders = new HttpHeaders();
            Session session = SecurityUtils.getSubject().getSession();
            Serializable id = session.getId();
            session.setTimeout(Long.valueOf(SystemVersionArgs.tokenAliveTime));
            redisUtil.sentinelSet((String) id, admin, Long.valueOf(SystemVersionArgs.tokenAliveTime));
            responseHeaders.set("ESLS", (String) id);
            responseHeaders.set("Access-Control-Expose-Headers", "ESLS");
            List<User> usrs = new ArrayList<>();
            usrs.add(admin);
            List<UserVo> userVos = CopyUtil.copyUser(usrs);
            System.out.println(userVos);
            return new ResponseEntity<>(ResultBean.success(userVos.get(0)),responseHeaders, HttpStatus.OK);
        } else {
            //登陆失败
            return new ResponseEntity<>(ResultBean.error("用户名或密码错误"), HttpStatus.BAD_REQUEST);
        }
    }
    @ApiOperation(value = "用户注册")
    @PostMapping("/user/registry")
    @RequiresPermissions("添加或修改信息")
    public ResponseEntity<ResultBean> registryUser(@RequestBody @ApiParam("用户信息集合") UserVo userVo){
        User user = userService.registerUser(userVo);
        if(user!=null)
            return new ResponseEntity<>(ResultBean.success(user), HttpStatus.OK);
        else
            return  new ResponseEntity<>(ResultBean.error("失败 [用户名已经存在] "), HttpStatus.BAD_REQUEST);
    }
    @ApiOperation("切换指定ID的用户的状态（0禁用1启用）")
    @PutMapping("/user/status/{id}")
    @RequiresPermissions("切换状态")
    public ResponseEntity<ResultBean> forbidUserById(@PathVariable Long id){
        User user = userService.findById(id);
        if(user!=null) {
            RequestBean source = new RequestBean();
            ArrayList<RequestItem> sourceItems = new ArrayList<>();
            sourceItems.add(new RequestItem("id", String.valueOf(id)));
            source.setItems(sourceItems);
            RequestBean target = new RequestBean();
            ArrayList<RequestItem> targetItems = new ArrayList<>();
            // 0为禁用
            Byte status = user.getStatus();
            String str = String.valueOf(status!=null && status==1?0:1);
            targetItems.add(new RequestItem("status",str));
            target.setItems(targetItems);
            Integer result = userService.updateByArrtribute(TableConstant.TABLE_USER, source, target);
            if (result != null && result > 0)
                return new ResponseEntity<>(ResultBean.success("切换状态成功 当前状态为："+str), HttpStatus.OK);
            return new ResponseEntity<>(ResultBean.error("切换状态失败 当前状态为："+status), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(ResultBean.error("不存在此用户"), HttpStatus.BAD_REQUEST);
    }
}

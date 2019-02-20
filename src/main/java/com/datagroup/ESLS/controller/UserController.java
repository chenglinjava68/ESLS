package com.datagroup.ESLS.controller;

import com.datagroup.ESLS.aop.Log;
import com.datagroup.ESLS.common.constant.TableConstant;
import com.datagroup.ESLS.common.exception.ResultEnum;
import com.datagroup.ESLS.common.exception.TagServiceException;
import com.datagroup.ESLS.common.request.RequestBean;
import com.datagroup.ESLS.common.request.RequestItem;
import com.datagroup.ESLS.common.response.ResultBean;
import com.datagroup.ESLS.dao.AdminDao;
import com.datagroup.ESLS.dto.UserVo;
import com.datagroup.ESLS.entity.Admin;
import com.datagroup.ESLS.entity.User;
import com.datagroup.ESLS.redis.RedisUtil;
import com.datagroup.ESLS.service.UserService;
import com.datagroup.ESLS.utils.SpringContextUtil;
import com.datagroup.ESLS.utils.ValidatorUtil;
import io.swagger.annotations.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@RestController
@Api(description = "用户管理API")
//实现跨域注解
//origin="*"代表所有域名都可访问
//maxAge飞行前响应的缓存持续时间的最大年龄，简单来说就是Cookie的有效期 单位为秒
//若maxAge是负数,则代表为临时Cookie,不会被持久化,Cookie信息保存在浏览器内存中,浏览器关闭Cookie就消失
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisUtil redisUtil;

    @ApiOperation(value = "user登录")
    @PostMapping("/user/login")
    public ResponseEntity<ResultBean> login(@Valid @RequestBody @ApiParam(value = "用户信息json格式") Admin adminEntity, BindingResult error) {
        if(error.hasErrors())
            return new ResponseEntity<>(ResultBean.error(ValidatorUtil.getError(error)), HttpStatus.BAD_REQUEST);
        User admin = userService.findByName(adminEntity.getUsername());
        if (admin != null) {
//            try {
//                UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(adminEntity.getUsername(), adminEntity.getPassword());
//                SecurityUtils.getSubject().login(usernamePasswordToken);
//            }
//            catch (Exception e){
//                e.printStackTrace();
//                throw new TagServiceException(ResultEnum.USER_LOGIN_ERROR);
//            }
//            HttpHeaders responseHeaders = new HttpHeaders();
//            String token = TokenUtil.getToken(admin);
//            redisUtil.sentinelSet(token, admin, SpringContextUtil.getAliveTime());
//            responseHeaders.set("X-ESLS-TOKEN", token);
            UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(adminEntity.getUsername(), adminEntity.getPassword());
            SecurityUtils.getSubject().login(usernamePasswordToken);
//            String JWTtoken = JWTUtil.sign(admin.getName(), admin.getPasswd());
            HttpHeaders responseHeaders = new HttpHeaders();
            Session session = SecurityUtils.getSubject().getSession();
            Serializable id = session.getId();
            session.setTimeout(SpringContextUtil.getAliveTime());
            System.out.println(id);
            responseHeaders.set("X-ESLS-TOKEN", (String) id);
            return new ResponseEntity<>(ResultBean.success(admin),responseHeaders, HttpStatus.OK);
        } else {
            //登陆失败
            return new ResponseEntity<>(ResultBean.error("用户名或密码错误"), HttpStatus.NOT_ACCEPTABLE);
        }
    }
    @ApiOperation(value = "用户注册")
    @PostMapping("/user/registry")
    public ResponseEntity<ResultBean> registryUser(@RequestBody @ApiParam("用户信息集合") UserVo userVo){
        boolean flag = userService.registerUser(userVo);
        if(flag)
            return new ResponseEntity<>(ResultBean.success("注册成功"), HttpStatus.OK);
        return  new ResponseEntity<>(ResultBean.error("注册失败 [用户名已经存在] "), HttpStatus.BAD_REQUEST);
    }
    @ApiOperation("切换指定ID的用户的状态（0禁用1启用）")
    @PutMapping("/user/status/{id}")
    public ResponseEntity<ResultBean> forbidUserById(@PathVariable Long id){
        Optional<User> user = userService.findById(id);
        if(user.isPresent()) {
            RequestBean source = new RequestBean();
            ArrayList<RequestItem> sourceItems = new ArrayList<>();
            sourceItems.add(new RequestItem("id", String.valueOf(id)));
            source.setItems(sourceItems);
            RequestBean target = new RequestBean();
            ArrayList<RequestItem> targetItems = new ArrayList<>();
            // 0为禁用
            Byte status = user.get().getStatus();
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

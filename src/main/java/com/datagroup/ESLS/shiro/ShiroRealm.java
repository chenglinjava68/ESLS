package com.datagroup.ESLS.shiro;

import com.datagroup.ESLS.entity.Permission;
import com.datagroup.ESLS.entity.Role;
import com.datagroup.ESLS.entity.User;
import com.datagroup.ESLS.service.UserService;
import com.datagroup.ESLS.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Slf4j
public class ShiroRealm extends AuthorizingRealm {
	@Override
	public boolean supports(AuthenticationToken token) {
		return token instanceof UsernamePasswordToken;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		UsernamePasswordToken userpasswordToken = (UsernamePasswordToken)token;
		String username = userpasswordToken.getUsername();
		User user = ((UserService)SpringContextUtil.getBean("UserService")).findByName(username);
		System.out.println("登录用户: "+user);
		if(user == null)
			throw new AuthenticationException("用户名或者密码错误");
		SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(user, user.getPasswd(),
				//得到加密密码的盐值
				ByteSource.Util.bytes(user.getName()), getName());
		return authenticationInfo;
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		System.out.println("获取用户角色");
		System.out.println(principals);
		User user = (User) principals.getPrimaryPrincipal();
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		// 获取用户角色
		// List<Role> roles = userService.findRolesByUserId(user.getId());
		List<Role> roles = user.getRoleList();
		System.out.println("用户角色："+roles.toString());
		Set<String> roleNames = roles.stream().map(Role::getName).collect(Collectors.toSet());
		info.setRoles(roleNames);
		// 获取用户权限
		//List<Permission> permissions = userService.findPermissionByUserId(user.getId());
		List<Permission> permissions = new ArrayList<>();
		for (Role role: roles)
			permissions.addAll(role.getPermissions());
		Set<String> permissionNames = permissions.stream().filter(p -> !StringUtils.isEmpty(p.getName()))
				.map(Permission::getName).collect(Collectors.toSet());
		info.addStringPermissions(permissionNames);
		System.out.println("用户权限："+permissionNames.toString());
		return info;
	}
}

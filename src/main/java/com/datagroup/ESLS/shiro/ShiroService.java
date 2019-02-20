package com.datagroup.ESLS.shiro;

import com.datagroup.ESLS.entity.Permission;
import com.datagroup.ESLS.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ShiroService {
    @Autowired
    private PermissionService permissionService;
    /**
     * 初始化权限
     */
    public Map<String, String> loadFilterChainDefinitions() {
        // 权限控制map.从数据库获取
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();
        // 所有请求通过我们自己的JWT Filter
        filterChainDefinitionMap.put("/user/login", "anon");
        filterChainDefinitionMap.put("/css/**", "anon");
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/img/**", "anon");
        filterChainDefinitionMap.put("/font-awesome/**", "anon");
        filterChainDefinitionMap.put("/swagger-ui.html/**", "anon");
        filterChainDefinitionMap.put("/swagger-resources/**", "anon");
        filterChainDefinitionMap.put("/webjars/**", "anon");
        filterChainDefinitionMap.put("/v2/**", "anon");
        List<Permission> permissions =permissionService.findAll();
        for (Permission item : permissions) {
            String permission = "perms[" + item.getName() + "]";
            log.info(permission);
            filterChainDefinitionMap.put(item.getUrl(), permission);
        }
        filterChainDefinitionMap.put("/**", "authc");
        return filterChainDefinitionMap;
    }
}

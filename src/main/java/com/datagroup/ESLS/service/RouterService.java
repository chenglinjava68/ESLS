package com.datagroup.ESLS.service;

import com.datagroup.ESLS.common.request.RequestBean;
import com.datagroup.ESLS.common.response.ResponseBean;
import com.datagroup.ESLS.entity.Router;
import com.datagroup.ESLS.entity.Tag;

import java.util.List;
import java.util.Optional;

public interface RouterService extends Service{
    List<Router> findAll();
    List<Router> findAll(Integer page, Integer count);
    Router saveOne(Router router);
    Optional<Router> findById(Long id);
    boolean deleteById(Long id);
    Router findByIp(String ip);
    // 更换路由器
    ResponseBean changeRouter(String sourceQuery,String sourceQueryString,String targetQuery, String targetQueryString);
    // 对路由器进行巡检
    ResponseBean routerScan(RequestBean requestBean);
    ResponseBean settingRouter(RequestBean requestBean);
}

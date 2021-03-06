package com.datagroup.ESLS.controller;

import com.datagroup.ESLS.aop.Log;
import com.datagroup.ESLS.common.constant.TableConstant;
import com.datagroup.ESLS.common.request.RequestBean;
import com.datagroup.ESLS.common.request.RequestItem;
import com.datagroup.ESLS.common.response.ResponseBean;
import com.datagroup.ESLS.common.response.ResultBean;
import com.datagroup.ESLS.dto.RouterVo;
import com.datagroup.ESLS.entity.Router;
import com.datagroup.ESLS.entity.Shop;
import com.datagroup.ESLS.service.RouterService;
import com.datagroup.ESLS.service.ShopService;
import com.datagroup.ESLS.utils.ConditionUtil;
import com.datagroup.ESLS.utils.CopyUtil;
import com.datagroup.ESLS.utils.SendCommandUtil;
import io.swagger.annotations.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@Api(description = "路由器管理API")
@CrossOrigin(origins = "*", maxAge = 3600)
@Validated
public class RouterController {

    @Autowired
    private RouterService routerService;
    private ShopService shopService;
    @ApiOperation(value = "根据条件获取路由器信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "query", value = "查询条件 可为所有字段", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "queryString", value = "查询条件的字符串", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "页码", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "count", value = "数量", dataType = "int", paramType = "query")
    })
    @GetMapping("/routers")
    @Log("获取路由器信息")
    @RequiresPermissions("系统菜单")
    public ResponseEntity<ResultBean> getRouter(@RequestParam(required = false) String query, @RequestParam(required = false) String queryString, @Min(message = "data.page.min", value = 0)@RequestParam(required = false) Integer page, @Min(message = "data.count.min", value = 0)@RequestParam(required = false)  Integer count) {
        String result = ConditionUtil.judgeArgument(query, queryString, page, count);
        if(result==null)
            return new ResponseEntity<>(ResultBean.error("参数组合有误 [query和queryString必须同时提供] [page和count必须同时提供]"), HttpStatus.BAD_REQUEST);
        // 带条件或查询
        if(query!=null && query.contains(" ")){
            List content = routerService.findAllBySql(TableConstant.TABLE_ROUTERS, "like", query, queryString, page, count, Router.class);
            return new ResponseEntity<>(new ResultBean(content, content.size()), HttpStatus.OK);
        }
        // 查询全部
        if(result.equals(ConditionUtil.QUERY_ALL)) {
            List list = routerService.findAll();
            return new ResponseEntity<>(new ResultBean(list, list.size()), HttpStatus.OK);
        }
        // 查询全部分页
        if(result.equals(ConditionUtil.QUERY_ALL_PAGE)){
            List list = routerService.findAll();
            List content = routerService.findAll(page, count);
            return new ResponseEntity<>(new ResultBean(content, list.size()), HttpStatus.OK);
        }
        // 带条件查询全部
        if(result.equals(ConditionUtil.QUERY_ATTRIBUTE_ALL)) {
            List content = routerService.findAllBySql(TableConstant.TABLE_ROUTERS, query, queryString,Router.class);
            return new ResponseEntity<>(new ResultBean(content, content.size()), HttpStatus.OK);
        }
        // 带条件查询分页
        if(result.equals(ConditionUtil.QUERY_ATTRIBUTE_PAGE)) {
            List list = routerService.findAll();
            List content = routerService.findAllBySql(TableConstant.TABLE_ROUTERS, query, queryString, page, count,Router.class);
            return new ResponseEntity<>(new ResultBean(content, list.size()), HttpStatus.OK);
        }
        return new ResponseEntity<>(ResultBean.error("查询组合出错 函数未执行！"), HttpStatus.BAD_REQUEST);
    }

    @ApiOperation(value = "获取指定ID的路由器信息")
    @GetMapping("/router/{id}")
    @Log("获取指定ID的路由器信息")
    @RequiresPermissions("获取指定ID的信息")
    public ResponseEntity<ResultBean> getRouterById(@PathVariable Long id) {
        Optional<Router> result = routerService.findById(id);
        if(result.isPresent()) {
            List list = new ArrayList();
            list.add(result.get());
            return new ResponseEntity<>(new ResultBean(CopyUtil.copyRouter(list)), HttpStatus.OK);
        }
        return new ResponseEntity<>(ResultBean.error("此ID路由器不存在"),HttpStatus.BAD_REQUEST);
    }

    @ApiOperation(value = "添加或修改路由器信息(路由器设置)")
    @PostMapping("/router")
    @Log("添加或修改路由器信息")
    @RequiresPermissions("添加或修改信息")
    public ResponseEntity<ResultBean> saveRouter(@RequestBody @ApiParam(value = "路由器信息json格式") RouterVo routerVo) {
        Router router = new Router();
        BeanUtils.copyProperties(routerVo,router);
        // 绑定商店
        if (routerVo.getShopId() != 0) {
            Shop shop = new Shop();
            if(!shopService.findById(routerVo.getShopId()).isPresent())
                return new ResponseEntity<>(ResultBean.error("商店不存在"),HttpStatus.BAD_REQUEST);
            shop.setId(routerVo.getShopId());
            router.setShop(shop);
        }
        return new ResponseEntity<>(new ResultBean(routerService.saveOne(router)),HttpStatus.OK);
    }

    @ApiOperation(value = "根据ID删除路由器信息")
    @DeleteMapping("/router/{id}")
    @Log("根据ID删除路由器信息")
    @RequiresPermissions("删除指定ID的信息")
    public ResponseEntity<ResultBean> deleteRouterById(@PathVariable Long id) {
        boolean flag = routerService.deleteById(id);
        if(flag)
            return new ResponseEntity<>(ResultBean.success("删除成功"),HttpStatus.OK);
        return new ResponseEntity<>(ResultBean.success("删除失败！没有指定ID的路由器或者此ID下的路由器仍有绑定的标签"),HttpStatus.BAD_REQUEST);
    }
    @ApiOperation("根据多个字段搜索数据")
    @PostMapping("/routers/search")
    @Log("根据多个字段搜索数据")
    @RequiresPermissions("查询和搜索功能")
    public ResponseEntity<ResultBean> searchRoutersByConditon(@RequestParam String connection,@Min(message = "data.page.min", value = 0)@RequestParam Integer page,@RequestParam @Min(message = "data.count.min", value = 0)Integer count,@RequestBody @ApiParam(value = "查询条件json格式") RequestBean requestBean){
        List<Router> routerList = routerService.findAllBySql(TableConstant.TABLE_ROUTERS, connection, requestBean, page, count, Router.class);
        return new ResponseEntity<>(new ResultBean(CopyUtil.copyRouter(routerList)), HttpStatus.OK);
    }
    // 更换路由器
    @ApiOperation("根据指定属性更换路由器")
    @PutMapping("/router/change")
    @Log("更换路由器")
    @RequiresPermissions("更换路由器")
    public ResponseEntity<ResultBean> changeRouter(@RequestParam @ApiParam("源字段名") String sourceQuery,@RequestParam @ApiParam("源字段值") String sourceQueryString,@RequestParam @ApiParam("目的字段名") String targetQuery,@RequestParam  @ApiParam("目的字段值") String targetQueryString ){
        ResponseBean response = routerService.changeRouter(sourceQuery, sourceQueryString, targetQuery, targetQueryString);
        if(!response.isError())
            return new ResponseEntity<>(ResultBean.success(response),HttpStatus.OK);
        else
            return new ResponseEntity<>(ResultBean.error("执行出错"),HttpStatus.BAD_REQUEST);
    }
    // 路由器巡检（查询路由器信息）
    @ApiOperation(value = "路由器巡检",notes = "定期巡检才需加cron表达式")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mode", value = " 0为指定路由器巡检 1定期巡检", dataType = "int", paramType = "query")
    })
    @PutMapping("/router/scan")
    @RequiresPermissions("路由器巡检")
    public ResponseEntity<ResultBean> routerScan(@RequestBody @ApiParam("路由器信息集合") RequestBean requestBean,@RequestParam Integer mode) {
        ResponseBean responseBean;
        if(mode == 0)
            responseBean = routerService.routerScan(requestBean);
        else
            responseBean = routerService.routerScanByCycle(requestBean);
        return new ResponseEntity<>(ResultBean.success(responseBean),HttpStatus.OK);
    }
    @ApiOperation("对所有路由器发起巡检")
    @PutMapping("/routers/scan")
    @RequiresPermissions("对所有路由器发起巡检")
    public ResponseEntity<ResultBean> routersScan() {
        ResponseBean responseBean = routerService.routersScan();
        return new ResponseEntity<>(ResultBean.success(responseBean),HttpStatus.OK);
    }
    // 路由器设置
    @ApiOperation("发送路由器设置命令")
    @PutMapping("/router/setting")
    @RequiresPermissions("发送路由器设置命令")
    public ResponseEntity<ResultBean> routerSetting(@RequestBody @ApiParam("路由器信息集合") RequestBean requestBean) {
        ResponseBean responseBean = routerService.settingRouter(requestBean);
        return new ResponseEntity<>(ResultBean.success(responseBean),HttpStatus.OK);
    }

    // AP信息写入
    @ApiOperation("AP测试命令")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mode", value = " 0AP信息写入(3者) 1AP信息读取 2AP发送无线帧（channelId） 3AP停止发送无线帧 4AP接收无线帧（channelId） 5AP停止接收无线帧", dataType = "int", paramType = "query")
    })
    @PutMapping("/router/test")
    @RequiresPermissions("AP测试")
    public ResponseEntity<ResultBean> routerTest(@RequestBody @ApiParam("路由器信息集合") RequestBean requestBean,@RequestParam(required = false) String barCode,@RequestParam(required = false) String channelId,@RequestParam(required = false) String hardVersion,@RequestParam Integer mode) {
        List<Router> routerList = new ArrayList<>();
        for (RequestItem items : requestBean.getItems()) {
            routerList.addAll(routerService.findByArrtribute(TableConstant.TABLE_ROUTERS, items.getQuery(), items.getQueryString(), Router.class));
        }
        ResponseBean responseBean = new ResponseBean(0,0);
        // 0AP信息写入
        if(mode==0){
            responseBean = SendCommandUtil.sendAPWrite(routerList, barCode, channelId, hardVersion);
        }
        // 1AP信息读取
        else if(mode==1){
            responseBean = SendCommandUtil.sendAPRead(routerList);
        }
        // 2AP发送无线帧
        else if(mode==2){
            responseBean = SendCommandUtil.sendAPByChannelId(routerList, channelId);
        }
        // 3AP停止发送无线帧
        else if(mode==3){
            responseBean = SendCommandUtil.sendAPByChannelIdStop(routerList);
        }
        // 4AP接收无线帧
        else if(mode==4){
            responseBean = SendCommandUtil.sendAPReceiveByChannelId(routerList, channelId);
        }
        // 5AP停止接收无线帧
        else if(mode==5){
            responseBean = SendCommandUtil.sendAPReceiveByChannelIdStop(routerList);
        }
        return new ResponseEntity<>(ResultBean.success(responseBean),HttpStatus.OK);
    }
}

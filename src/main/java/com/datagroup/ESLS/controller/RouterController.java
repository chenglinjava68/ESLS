package com.datagroup.ESLS.controller;

        import com.datagroup.ESLS.aop.Log;
        import com.datagroup.ESLS.common.constant.TableConstant;
        import com.datagroup.ESLS.common.request.RequestBean;
        import com.datagroup.ESLS.common.response.ResponseBean;
        import com.datagroup.ESLS.common.response.ResultBean;
        import com.datagroup.ESLS.dto.RouterVo;
        import com.datagroup.ESLS.entity.Router;
        import com.datagroup.ESLS.entity.Shop;
        import com.datagroup.ESLS.service.RouterService;
        import com.datagroup.ESLS.service.TagService;
        import com.datagroup.ESLS.utils.CopyUtil;
        import io.swagger.annotations.*;
        import org.springframework.beans.BeanUtils;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.http.HttpStatus;
        import org.springframework.http.ResponseEntity;
        import org.springframework.web.bind.annotation.*;

        import javax.validation.constraints.Min;
        import java.util.ArrayList;
        import java.util.List;
        import java.util.Optional;

@RestController
@Api(description = "路由器管理API")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RouterController {

    @Autowired
    private RouterService routerService;
    @Autowired
    private TagService tagService;

    @ApiOperation(value = "根据条件获取路由器信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "query", value = "查询条件 可为所有字段", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "queryString", value = "查询条件的字符串", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "页码", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "count", value = "数量", required = true, dataType = "int", paramType = "query")
    })
    @GetMapping("/routers")
    @Log("获取路由器信息")
    public ResponseEntity<ResultBean> getRouter(@RequestParam(required = false) String query, @RequestParam(required = false) String queryString, @Min(message = "data.page.min", value = 0)@RequestParam Integer page, @Min(message = "data.count.min", value = 0)@RequestParam Integer count) {
        if(query!=null && queryString!=null) {
            List<Router> list = routerService.findAllBySql(TableConstant.TABLE_ROUTERS, query, queryString, page, count,Router.class);
            return new ResponseEntity<>(new ResultBean(CopyUtil.copyRouter(list),list.size()), HttpStatus.OK);
        }
        List<Router> list = routerService.findAll();
        List<Router> content = routerService.findAll(page,count);
        return new ResponseEntity<>(new ResultBean(CopyUtil.copyRouter(content),list.size()),HttpStatus.OK);
    }

    @ApiOperation(value = "获取指定ID的路由器信息")
    @GetMapping("/router/{id}")
    @Log("获取指定ID的路由器信息")
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
    public ResponseEntity<ResultBean> saveRouter(@RequestBody @ApiParam(value = "路由器信息json格式") RouterVo routerVo) {
        Router router = new Router();
        BeanUtils.copyProperties(routerVo,router);
        // 绑定商店
        if (routerVo.getShopId() != 0) {
            Shop shop = new Shop();
            shop.setId(routerVo.getShopId());
            router.setShop(shop);
        }
        return new ResponseEntity<>(new ResultBean(routerService.saveOne(router)),HttpStatus.OK);
    }

    @ApiOperation(value = "根据ID删除路由器信息")
    @DeleteMapping("/router/{id}")
    @Log("根据ID删除路由器信息")
    public ResponseEntity<ResultBean> deleteRouterById(@PathVariable Long id) {
        boolean flag = routerService.deleteById(id);
        if(flag)
            return new ResponseEntity<>(ResultBean.success("删除成功"),HttpStatus.OK);
        return new ResponseEntity<>(ResultBean.success("删除失败！没有指定ID的路由器或者此ID下的路由器仍有绑定的标签"),HttpStatus.BAD_REQUEST);
    }
    @ApiOperation("根据多个字段搜索数据")
    @PostMapping("/routers/search")
    @Log("根据多个字段搜索数据")
    public ResponseEntity<ResultBean> searchRoutersByConditon(@RequestParam String connection,@Min(message = "data.page.min", value = 0)@RequestParam Integer page,@RequestParam @Min(message = "data.count.min", value = 0)Integer count,@RequestBody @ApiParam(value = "查询条件json格式") RequestBean requestBean){
        List<Router> routerList = routerService.findAllBySql(TableConstant.TABLE_ROUTERS, connection, requestBean, page, count, Router.class);
        return new ResponseEntity<>(new ResultBean(CopyUtil.copyRouter(routerList)), HttpStatus.OK);
    }
    // 更换路由器
    @ApiOperation("根据指定属性更换路由器")
    @PutMapping("/router/change")
    @Log("更换路由器")
    public ResponseEntity<ResultBean> changeRouter(@RequestParam @ApiParam("源字段名") String sourceQuery,@RequestParam @ApiParam("源字段值") String sourceQueryString,@RequestParam @ApiParam("目的字段名") String targetQuery,@RequestParam  @ApiParam("目的字段值") String targetQueryString ){
        ResponseBean response = routerService.changeRouter(sourceQuery, sourceQueryString, targetQuery, targetQueryString);
        if(!response.isError())
            return new ResponseEntity<>(ResultBean.success(response),HttpStatus.OK);
        else
            return new ResponseEntity<>(ResultBean.error("执行出错"),HttpStatus.BAD_REQUEST);
    }
    // 路由器巡检（查询路由器信息）
    @ApiOperation("路由器巡检")
    @PutMapping("/router/scan")
    public ResponseEntity<ResultBean> routerScan(@RequestBody @ApiParam("路由器信息集合") RequestBean requestBean) {
        ResponseBean responseBean = routerService.routerScan(requestBean);
        return new ResponseEntity<>(ResultBean.success(responseBean),HttpStatus.OK);
    }
    // 路由器设置
    @ApiOperation("发送路由器设置命令")
    @PutMapping("/router/setting")
    public ResponseEntity<ResultBean> routerSetting(@RequestBody @ApiParam("路由器信息集合") RequestBean requestBean) {
        ResponseBean responseBean = routerService.settingRouter(requestBean);
        return new ResponseEntity<>(ResultBean.success(responseBean),HttpStatus.OK);
    }
}

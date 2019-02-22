package com.datagroup.ESLS.controller;

import com.datagroup.ESLS.aop.Log;
import com.datagroup.ESLS.common.constant.TableConstant;
import com.datagroup.ESLS.common.response.ResultBean;
import com.datagroup.ESLS.entity.Shop;
import com.datagroup.ESLS.service.ShopService;
import com.datagroup.ESLS.utils.ConditionUtil;
import com.datagroup.ESLS.utils.CopyUtil;
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

@RestController
@Api(description = "店铺管理API")
@CrossOrigin(origins = "*",maxAge = 3600)
public class ShopController {
    @Autowired
    private ShopService shopService;
    @ApiOperation(value = "根据条件获取店铺信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "query", value = "查询条件 可为所有字段 ", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "queryString", value = "查询条件的字符串", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "页码", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "count", value = "数量", dataType = "int", paramType = "query")
    })
    @GetMapping("/shops")
    @Log("获取店铺信息")
    @RequiresPermissions("系统菜单")
    public ResponseEntity<ResultBean> getShops(@RequestParam(required = false) String query, @RequestParam(required = false) String queryString, @Min(message = "data.page.min", value = 0)@RequestParam(required = false) Integer page, @Min(message = "data.count.min", value = 0) @RequestParam(required = false) Integer count) {
        String result = ConditionUtil.judgeArgument(query, queryString, page, count);
        if(result==null)
            return new ResponseEntity<>(ResultBean.error("参数组合有误 [query和queryString必须同时提供] [page和count必须同时提供]"), HttpStatus.BAD_REQUEST);
        // 带条件或查询
        if(query!=null && query.contains(" ")){
            List content = shopService.findAllBySql(TableConstant.TABLE_SHOPS, "like", query, queryString, page, count, Shop.class);
            return new ResponseEntity<>(new ResultBean(content, content.size()), HttpStatus.OK);
        }
        // 查询全部
        if(result.equals(ConditionUtil.QUERY_ALL)) {
            List list = shopService.findAll();
            return new ResponseEntity<>(new ResultBean(list, list.size()), HttpStatus.OK);
        }
        // 查询全部分页
        if(result.equals(ConditionUtil.QUERY_ALL_PAGE)){
            List list = shopService.findAll();
            List content = shopService.findAll(page, count);
            return new ResponseEntity<>(new ResultBean(content, list.size()), HttpStatus.OK);
        }
        // 带条件查询全部
        if(result.equals(ConditionUtil.QUERY_ATTRIBUTE_ALL)) {
            List content = shopService.findAllBySql(TableConstant.TABLE_SHOPS, query, queryString,Shop.class);
            return new ResponseEntity<>(new ResultBean(content, content.size()), HttpStatus.OK);
        }
        // 带条件查询分页
        if(result.equals(ConditionUtil.QUERY_ATTRIBUTE_PAGE)) {
            List list = shopService.findAll();
            List content = shopService.findAllBySql(TableConstant.TABLE_SHOPS, query, queryString, page, count,Shop.class);
            return new ResponseEntity<>(new ResultBean(content, list.size()), HttpStatus.OK);
        }
        return new ResponseEntity<>(ResultBean.error("查询组合出错 函数未执行！"), HttpStatus.BAD_REQUEST);
    }

    @ApiOperation(value = "获取指定ID的店铺信息")
    @GetMapping("/shop/{id}")
    @Log("获取指定ID的店铺信息")
    @RequiresPermissions("获取指定ID的信息")
    public ResponseEntity<ResultBean> getShopById(@PathVariable Long id) {
        Optional<Shop> result = shopService.findById(id);
        if(result.isPresent()) {
            ArrayList<Shop> list = new ArrayList<>();
            list.add(result.get());
            return new ResponseEntity<>(new ResultBean(list), HttpStatus.OK);
        }
        return new ResponseEntity<>(ResultBean.error("此ID店铺不存在"),HttpStatus.BAD_REQUEST);
    }

    @ApiOperation(value = "添加或修改店铺信息")
    @PostMapping("/shop")
    @Log("添加或修改店铺信息")
    @RequiresPermissions("添加或修改信息")
    public ResponseEntity<ResultBean>  saveShop(@RequestBody @ApiParam(value = "店铺信息json格式") Shop shop) {
        return new ResponseEntity<>(new ResultBean(shopService.saveOne(shop)),HttpStatus.OK);
    }

    @ApiOperation(value = "根据ID删除店铺信息")
    @DeleteMapping("/shop/{id}")
    @Log("根据ID删除店铺信息")
    @RequiresPermissions("删除指定ID的信息")
    public ResponseEntity<ResultBean> deleteShopById(@PathVariable Long id) {
        boolean flag = shopService.deleteById(id);
        if(flag)
            return new ResponseEntity<>(ResultBean.success("删除成功"),HttpStatus.OK);
        return new ResponseEntity<>(ResultBean.success("删除失败！没有指定ID的店铺"),HttpStatus.BAD_REQUEST);
    }
}

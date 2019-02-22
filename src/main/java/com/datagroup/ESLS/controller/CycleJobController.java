package com.datagroup.ESLS.controller;

import com.datagroup.ESLS.aop.Log;
import com.datagroup.ESLS.common.constant.TableConstant;
import com.datagroup.ESLS.common.response.ResultBean;
import com.datagroup.ESLS.entity.CycleJob;
import com.datagroup.ESLS.service.CycleJobService;
import com.datagroup.ESLS.utils.ConditionUtil;
import io.swagger.annotations.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;

@Api(description = "定期任务工具类")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Validated
public class CycleJobController {
    @Autowired
    private CycleJobService cycleJobService;
    @ApiOperation(value = "根据条件获取定期任务信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "query", value = "查询条件 可为所有字段 分隔符为单个空格 ", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "queryString", value = "查询条件的字符串", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "页码", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "count", value = "数量", dataType = "int", paramType = "query")
    })
    @GetMapping("/cyclejobs")
    @Log("获取定期任务信息")
    @RequiresPermissions("系统菜单")
    public ResponseEntity<ResultBean> getGoods(@RequestParam(required = false) String query, @RequestParam(required = false) String queryString, @Min(message = "data.page.min", value = 0)@RequestParam(required = false) Integer page, @RequestParam(required = false) @Min(message = "data.count.min", value = 0)Integer count) {
        String result = ConditionUtil.judgeArgument(query, queryString, page, count);
        if(result==null)
            return new ResponseEntity<>(ResultBean.error("参数组合有误 [query和queryString必须同时提供] [page和count必须同时提供]"), HttpStatus.BAD_REQUEST);
        // 带条件或查询
        if(query!=null && query.contains(" ")){
            List content = cycleJobService.findAllBySql(TableConstant.TABLE_CYCLEJOBS, "like", query, queryString, page, count, CycleJob.class);
            return new ResponseEntity<>(new ResultBean(content, content.size()), HttpStatus.OK);
        }
        // 查询全部
        if(result.equals(ConditionUtil.QUERY_ALL)) {
            List list = cycleJobService.findAll();
            return new ResponseEntity<>(new ResultBean(list, list.size()), HttpStatus.OK);
        }
        // 查询全部分页
        if(result.equals(ConditionUtil.QUERY_ALL_PAGE)){
            List list = cycleJobService.findAll();
            List content = cycleJobService.findAll(page, count);
            return new ResponseEntity<>(new ResultBean(content, list.size()), HttpStatus.OK);
        }
        // 带条件查询全部
        if(result.equals(ConditionUtil.QUERY_ATTRIBUTE_ALL)) {
            List content = cycleJobService.findAllBySql(TableConstant.TABLE_CYCLEJOBS, query, queryString,CycleJob.class);
            return new ResponseEntity<>(new ResultBean(content, content.size()), HttpStatus.OK);
        }
        // 带条件查询分页
        if(result.equals(ConditionUtil.QUERY_ATTRIBUTE_PAGE)) {
            List list = cycleJobService.findAll();
            List content = cycleJobService.findAllBySql(TableConstant.TABLE_CYCLEJOBS, query, queryString, page, count,CycleJob.class);
            return new ResponseEntity<>(new ResultBean(content, list.size()), HttpStatus.OK);
        }
        return new ResponseEntity<>(ResultBean.error("查询组合出错 函数未执行！"), HttpStatus.BAD_REQUEST);
    }
    @ApiOperation(value = "获取指定ID的定期任务信息")
    @GetMapping("/cyclejobs/{id}")
    @Log("获取指定ID的定期任务信息")
    @Transactional
    @RequiresPermissions("获取指定ID的信息")
    public ResponseEntity<ResultBean> getGoodById(@PathVariable Long id) {
        CycleJob cycleJob = cycleJobService.findById(id);
        if(cycleJob==null)
            return new ResponseEntity<>(ResultBean.error("此ID定期任务不存在"),HttpStatus.BAD_REQUEST);
        List cycleJobs = new ArrayList<CycleJob>();
        cycleJobs.add(cycleJob);
        return new ResponseEntity<>(new ResultBean(cycleJobs),HttpStatus.OK);
    }

    @ApiOperation(value = "添加或修改定期任务信息")
    @PostMapping("/cyclejob")
    @Log("添加或修改定期任务信息")
    public ResponseEntity<ResultBean>  saveGood(@RequestBody @ApiParam(value = "定期任务json格式") CycleJob cycleJob) {
        return new ResponseEntity<>(new ResultBean(cycleJobService.saveOne(cycleJob)),HttpStatus.OK);
    }

    @ApiOperation(value = "根据ID删除定期任务信息")
    @DeleteMapping("/cyclejob/{id}")
    @Log("根据ID删除定期任务信息")
    @RequiresPermissions("删除指定ID的信息")
    public ResponseEntity<ResultBean> deleteGoodById(@PathVariable Long id) {
        boolean flag = cycleJobService.deleteById(id);
        if(flag)
            return new ResponseEntity<>(ResultBean.success("删除成功"),HttpStatus.OK);
        return new ResponseEntity<>(ResultBean.success("删除失败！没有指定ID的定期任务"),HttpStatus.BAD_REQUEST);
    }
}

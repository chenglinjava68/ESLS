package com.datagroup.ESLS.controller;

import com.datagroup.ESLS.aop.Log;
import com.datagroup.ESLS.common.request.RequestBean;
import com.datagroup.ESLS.common.response.ResponseBean;
import com.datagroup.ESLS.dao.TagDao;
import com.datagroup.ESLS.dto.DispmsVo;
import com.datagroup.ESLS.dto.StyleVo;
import com.datagroup.ESLS.common.constant.ArrtributeConstant;
import com.datagroup.ESLS.common.constant.TableConstant;
import com.datagroup.ESLS.common.response.ResultBean;
import com.datagroup.ESLS.entity.Dispms;
import com.datagroup.ESLS.entity.Good;
import com.datagroup.ESLS.entity.Style;
import com.datagroup.ESLS.entity.Tag;
import com.datagroup.ESLS.netty.command.CommandConstant;
import com.datagroup.ESLS.service.DispmsService;
import com.datagroup.ESLS.service.GoodService;
import com.datagroup.ESLS.service.StyleService;
import com.datagroup.ESLS.service.TagService;
import com.datagroup.ESLS.utils.*;
import io.swagger.annotations.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@Api(description = "样式管理API")
@CrossOrigin(origins = "*", maxAge = 3600)
@Validated
public class StyleController {
    @Autowired
    private StyleService styleService;
    @Autowired
    private DispmsService dispmsService;
    @Autowired
    private GoodService goodService;
    @Autowired
    private TagDao tagDao;
    @Autowired
    private NettyUtil nettyUtil;
    @ApiOperation(value = "根据条件获取样式信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "query", value = "查询条件 可为所有字段", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "queryString", value = "查询条件的字符串", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "页码", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "count", value = "数量", dataType = "int", paramType = "query")
    })
    @GetMapping("/styles")
    @Log("获取样式数据")
    @RequiresPermissions("系统菜单")
    public ResponseEntity<ResultBean> getStyles(@RequestParam(required = false) String query, @RequestParam(required = false) String queryString, @Min(message = "data.page.min", value = 0) @RequestParam(required = false) Integer page, @Min(message = "data.count.min", value = 0) @RequestParam(required = false) Integer count) {
        String result = ConditionUtil.judgeArgument(query, queryString, page, count);
        if(result==null)
            return new ResponseEntity<>(ResultBean.error("参数组合有误 [query和queryString必须同时提供] [page和count必须同时提供]"), HttpStatus.BAD_REQUEST);
        // 查询全部
        if(result.equals(ConditionUtil.QUERY_ALL)) {
            List<Style> list = styleService.findAll();
            List<StyleVo> resultList = CopyUtil.copyStyle(list);
            return new ResponseEntity<>(new ResultBean(resultList, resultList.size()), HttpStatus.OK);
        }
        // 查询全部分页
        if(result.equals(ConditionUtil.QUERY_ALL_PAGE)){
            List<Style> list = styleService.findAll();
            List<Style> content = styleService.findAll(page, count);
            List<StyleVo> resultList = CopyUtil.copyStyle(content);
            return new ResponseEntity<>(new ResultBean(resultList, list.size()), HttpStatus.OK);
        }
        // 带条件查询全部
        if(result.equals(ConditionUtil.QUERY_ATTRIBUTE_ALL)) {
            List content = styleService.findAllBySql(TableConstant.TABLE_STYLE, query, queryString, Style.class);
            List<StyleVo> resultList = CopyUtil.copyStyle(content);
            return new ResponseEntity<>(new ResultBean(resultList, resultList.size()), HttpStatus.OK);
        }
        // 带条件查询分页
        if(result.equals(ConditionUtil.QUERY_ATTRIBUTE_PAGE)) {
            List<Style> list = styleService.findAll();
            List  content = styleService.findAllBySql(TableConstant.TABLE_STYLE, query, queryString, page, count, Style.class);
            List<StyleVo> resultList = CopyUtil.copyStyle(content);
            return new ResponseEntity<>(new ResultBean(resultList, list.size()), HttpStatus.OK);
        }
        return new ResponseEntity<>(ResultBean.error("查询组合出错 函数未执行！"), HttpStatus.BAD_REQUEST);
    }

    @ApiOperation(value = "获取指定ID的样式信息")
    @GetMapping("/style/{id}")
    @Log("获取指定ID的样式信息")
    @RequiresPermissions("获取指定ID的信息")
    public ResponseEntity<ResultBean> getStyleById(@PathVariable Long id) {
        Optional<Style> result = styleService.findById(id);
        if (result.isPresent()) {
            ArrayList<Style> styles = new ArrayList<>();
            styles.add(result.get());
            List<StyleVo> styleVo = CopyUtil.copyStyle(styles);
            return new ResponseEntity<>(new ResultBean(styleVo), HttpStatus.OK);
        }
        return new ResponseEntity<>(ResultBean.error("此ID样式不存在"), HttpStatus.BAD_REQUEST);
    }
    @ApiOperation("获得指定ID的样式的所有小样式信息")
    @GetMapping("/style/dispms/{id}")
    @Log("获得指定ID的样式的所有小样式信息")
    public ResponseEntity<ResultBean> getDispmses(@PathVariable Long id){
        List<Dispms> dispmses = dispmsService.findByArrtribute(TableConstant.TABLE_DISPMS, ArrtributeConstant.TAG_STYLEID, String.valueOf(id), Dispms.class);
        ResponseEntity<ResultBean> result ;
        if ((result = ResponseUtil.testListSize("没有对应ID的小样式", dispmses)) != null) return result;
        return new ResponseEntity<>(ResultBean.success(CopyUtil.copyDispms(dispmses)), HttpStatus.OK);
    }
    @ApiOperation(value = "添加样式信息")
    @GetMapping("/style")
    @Log("添加或修改样式信息")
    @RequiresPermissions("添加或修改信息")
    public ResponseEntity<ResultBean> saveStyleByStyleType(@RequestParam("样式名字") String styleType,@RequestParam(value = "cron表达式",required = false) String cron) {
        Style result = styleService.saveOne(styleType,cron);
        return new ResponseEntity<>(new ResultBean(result), HttpStatus.OK);
    }
    @ApiOperation(value = "添加或修改样式信息")
    @PostMapping("/style")
    @RequiresPermissions("添加或修改信息")
    public ResponseEntity<ResultBean> saveStyle(@RequestBody @ApiParam(value = "样式信息JSON格式") Style style) {
        Style result = styleService.saveOne(style);
        return new ResponseEntity<>(new ResultBean(result), HttpStatus.OK);
    }
    @ApiOperation(value = "根据ID删除样式信息")
    @DeleteMapping("/style/{id}")
    @Log("根据ID删除样式信息")
    @RequiresPermissions("删除指定ID的信息")
    public ResponseEntity<ResultBean> deleteStyleById(@PathVariable Long id) {
        boolean flag = styleService.deleteById(id);
        if (flag)
            return new ResponseEntity<>(ResultBean.success("删除成功"), HttpStatus.OK);
        return new ResponseEntity<>(ResultBean.error("删除失败！没有指定ID的样式"), HttpStatus.BAD_REQUEST);
    }

    //    @ApiOperation(value = "更改指定ID样式的小样式")
//    @PostMapping("/style/update")
//    @Log("更改指定ID样式的小样式")
//    public ResponseEntity<ResultBean> updateStyleById(@RequestParam long styleId, @RequestBody @ApiParam(value = "样式信息Id List格式") List<Long> dispmIds) {
//        List<Style> styleList = dispmsService.findByArrtribute(TableConstant.TABLE_STYLE, ArrtributeConstant.TABLE_ID, String.valueOf(styleId), Style.class);
//        if(styleList==null || styleList.size()==0)
//            return new ResponseEntity<>(ResultBean.error("没有指定的样式,请先添加样式"), HttpStatus.BAD_REQUEST);
//        ResponseEntity<ResultBean> result ;
//        if ((result = ResponseUtil.testListSize("没有对应ID的样式", styleList)) != null) return result;
//        // 返回前端提示信息
//        return new ResponseEntity<>(ResultBean.success(styleService.updateStyleById(styleId,dispmIds,styleList.get(0))), HttpStatus.OK);
//    }
    @ApiOperation(value = "新建或修改样式同时绑定小样式")
    @PostMapping("/style/new")
    @Log("新建或修改样式同时绑定小样式")
    @ApiImplicitParam(name = "mode", value = "0为添加 1为修改", dataType = "int", paramType = "query")
    @RequiresPermissions("新建或修改样式同时绑定小样式")
    public ResponseEntity<ResultBean> newStyleById(@RequestParam long styleId, @RequestBody @ApiParam(value = "样式信息JSON格式") List<Dispms> dispms,@RequestParam Integer mode) {
        List<Style> styleList = dispmsService.findByArrtribute(TableConstant.TABLE_STYLE, ArrtributeConstant.TABLE_ID, String.valueOf(styleId), Style.class);
        if(styleList==null || styleList.size()==0)
            return new ResponseEntity<>(ResultBean.error("没有指定的样式,请先添加样式"), HttpStatus.BAD_REQUEST);
        ResponseEntity<ResultBean> result ;
        if ((result = ResponseUtil.testListSize("没有对应ID的样式", styleList)) != null) return result;
        // 返回前端提示信息
        return new ResponseEntity<>(ResultBean.success(styleService.newStyleById(styleId,dispms,styleList.get(0),mode)), HttpStatus.OK);
    }
    @ApiOperation(value = "刷新选用该样式的标签或设置定期刷新",notes = "定期刷新才需加beginTime和cycleTime字段")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mode", value = " 0为刷新选用该样式的标签 1定期刷新", dataType = "int", paramType = "query")
    })
    @PutMapping("/style/flush")
    @Log("刷新选用该样式的标签或设置定期刷新")
    @RequiresPermissions("刷新选用该样式的标签或设置定期刷新")
    public ResponseEntity<ResultBean> flushTags(@RequestBody @ApiParam("样式集合") RequestBean requestBean,@RequestParam  @Min(message = "data.page.min", value = 0)  @Max(message = "data.mode.max", value = 1) Integer mode){
        return new  ResponseEntity<>(new ResultBean(styleService.flushTags(requestBean,mode)),HttpStatus.OK);
    }
    @ApiOperation("生成指定ID样式的所有小样式图片")
    @GetMapping("/style/photo/{id}")
    @Log("生成指定ID样式的所有小样式图片")
    @RequiresPermissions("生成指定ID样式的所有小样式图片")
    public ResponseEntity<ResultBean> creatStyle(@PathVariable long id,@RequestParam Long goodId){
        Style style= styleService.findById(id).get();
        Good good = goodService.findById(goodId);
        Collection<Dispms> dispmses = style.getDispmses();
        for(Dispms dispms : dispmses) {
            Dispms temp =ImageHelper.getText(dispms, good);
            ImageHelper.getRegionImage(temp, style.getStyleNumber(),good);
        }
        return new  ResponseEntity<>(ResultBean.success("成功"),HttpStatus.OK);
    }
    @ApiOperation("生成指定ID样式的所有小样式图片")
    @GetMapping("/style/dism/photo/{id}")
    @Log("生成指定ID样式的所有小样式图片")
    @RequiresPermissions("生成指定ID样式的所有小样式图片")
    public ResponseEntity<ResultBean> creatDism(@PathVariable long id,@RequestParam Long goodId,@RequestParam String styleNumber){
        Dispms dispms= dispmsService.findById(id).get();
        Good good = goodService.findById(goodId);
        Dispms temp =ImageHelper.getText(dispms, good);
        ImageHelper.getRegionImage(temp, styleNumber,good);
        return new  ResponseEntity<>(ResultBean.success("成功"),HttpStatus.OK);
    }

    @ApiOperation("向选用此样式的所有标签发送样式")
    @GetMapping("/style/sendDism/{id}")
    @Log("向选用此样式的标签发送样式")
    @RequiresPermissions("向选用此样式的所有标签发送样式")
    public ResponseEntity<ResultBean> sendDism(@PathVariable long id){
        ResponseBean responseBean;
        List<Tag> tags = tagDao.findByStyleId(id);
        if(tags.size()>1)
        {
            nettyUtil.awakeFirst(tags);
            responseBean = SendCommandUtil.updateTagStyle(tags);
            nettyUtil.awakeOverLast(tags);
        }
        else
            responseBean = SendCommandUtil.updateTagStyle(tags);
        return new  ResponseEntity<>(ResultBean.success(responseBean),HttpStatus.OK);
    }
}

package com.datagroup.ESLS.controller;

import com.datagroup.ESLS.common.constant.SqlConstant;
import com.datagroup.ESLS.common.response.ResultBean;
import com.datagroup.ESLS.dao.BaseDao;
import com.datagroup.ESLS.dao.SystemVersionDao;
import com.datagroup.ESLS.entity.SystemVersion;
import com.datagroup.ESLS.entity.SystemVersionArgs;
import com.datagroup.ESLS.utils.PoiUtil;
import com.datagroup.ESLS.utils.SpringContextUtil;
import io.swagger.annotations.*;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.*;

@Api(description = "通用工具类")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Validated
public class CommonController {
    @Autowired
    private BaseDao baseDao;
    @Autowired
    private SystemVersionDao systemVersionDao;
    @Autowired
    private SystemVersionArgs systemVersionArgs;

    @ApiOperation("获取数据库表信息（0）或获取数据表的所有字段（表名,1）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mode", value = " 0获取所有数据库表 1获取相应表的所有字段信息", dataType = "int", paramType = "query")
    })
    @GetMapping("/common/database")
    @RequiresPermissions("获取数据表信息")
    public ResponseEntity<ResultBean> getTableColumn(@RequestParam(required = false) @ApiParam("表名") String tableName, @RequestParam Integer mode) {
        String sql = SqlConstant.QUERY_ALL_TABLE;
        if (mode == 1)
            sql = SqlConstant.QUERY_TABLIE_COLUMN + "\'" + tableName + "\'";
        List results = baseDao.findBySql(sql);
        List<HashMap> mapToList = new ArrayList<>();
        for(Object str : results){
            HashMap<Object,Object> resultMap = new HashMap<>();
            resultMap.put("keyName",str);
            mapToList.add(resultMap);
        }
        return new ResponseEntity<>(ResultBean.success(mapToList), HttpStatus.OK);
    }

    @ApiOperation(value = "导出指定条件数据库excel报表(连接符可取 =  或  like)")
    @GetMapping("/common/database/exportExcelDataFile")
    @RequiresPermissions("导出数据库表")
    public ResponseEntity<ResultBean> getExcelByTableName(@RequestParam String tableName, @RequestParam(required = false) String query,@RequestParam(required = false) String connection,@RequestParam(required = false) String queryString,HttpServletRequest request, HttpServletResponse response) {
        List dataColumnList = baseDao.findBySql(SqlConstant.QUERY_TABLIE_COLUMN + "\'" + tableName + "\'");
        List dataList ;
        try {
            if(query==null || query==null  || connection==null){
                dataList = baseDao.findBySql("select * from "+ tableName , Class.forName("com.datagroup.ESLS.entity." + SqlConstant.EntityToSqlMap.get(tableName)));
            }
            else {
                if (!"=".equals(connection) && !"like".equalsIgnoreCase(connection) && !"is".equalsIgnoreCase(connection))
                    return new ResponseEntity<>(ResultBean.error("connecttion参数出错"), HttpStatus.BAD_REQUEST);
                if (connection.equalsIgnoreCase("like"))
                    queryString = "%" + queryString + "%";
                dataList = baseDao.findBySql(SqlConstant.getQuerySql(tableName, query, connection, queryString), Class.forName("com.datagroup.ESLS.entity." + SqlConstant.EntityToSqlMap.get(tableName)));
            }
        } catch (ClassNotFoundException e) {
            return new ResponseEntity<>(ResultBean.error("导出excel出错"+e.toString()), HttpStatus.BAD_REQUEST);
        }
        HSSFWorkbook hssfWorkbook = PoiUtil.exportData2Excel(dataList, dataColumnList, tableName);
        //以流输出到浏览器
        PoiUtil.writeToResponse(hssfWorkbook, request, response, tableName);
        return new ResponseEntity<>(ResultBean.success("导出excel成功"), HttpStatus.OK);
    }

    @ApiOperation("导出指定条件数据库csv文件(连接符可取 =  或  like)")
    @GetMapping("/common/database/exportCsvDataFile")
    @RequiresPermissions("导出数据库表")
    public ResponseEntity<ResultBean> getCsvByTableName(@RequestParam @ApiParam("数据库表名") String tableName,  @RequestParam(required = false) String query,@RequestParam(required = false) String connection,@RequestParam(required = false) String queryString,HttpServletResponse response) {
        List dataColumnList = baseDao.findBySql(SqlConstant.QUERY_TABLIE_COLUMN + "\'" + tableName + "\'");
        List dataList ;
        try (final OutputStream os = response.getOutputStream()) {
            if(query==null || query==null  || connection==null){
                dataList = baseDao.findBySql("select * from "+ tableName , Class.forName("com.datagroup.ESLS.entity." + SqlConstant.EntityToSqlMap.get(tableName)));
            }
            else {
                if (!"=".equals(connection) && !"like".equalsIgnoreCase(connection) && !"is".equalsIgnoreCase(connection))
                    return new ResponseEntity<>(ResultBean.error("connecttion参数出错"), HttpStatus.BAD_REQUEST);
                if (connection.equalsIgnoreCase("like"))
                    queryString = "%" + queryString + "%";
                dataList = baseDao.findBySql(SqlConstant.getQuerySql(tableName, query, connection, queryString), Class.forName("com.datagroup.ESLS.entity." + SqlConstant.EntityToSqlMap.get(tableName)));
            }
            PoiUtil.responseSetProperties(tableName, response);
            PoiUtil.exportData2Csv(dataList, dataColumnList, os);
        } catch (Exception e) {
            return new ResponseEntity<>(ResultBean.error("导出csv出错"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(ResultBean.success("导出csv成功"), HttpStatus.OK);
    }
    @ApiOperation("导入Excel数据库表")
    @PostMapping("/common/database/importExcelDataFile")
    @RequiresPermissions("导入数据库表")
    public ResponseEntity<ResultBean> importExcelDataFile(@ApiParam(value = "文件信息", required = true) @RequestParam("file") MultipartFile file, @RequestParam @ApiParam("数据库表名") String tableName) {
        if (Objects.isNull(file) || file.isEmpty()) {
            return new ResponseEntity<>(ResultBean.error("文件为空，请重新上传"), HttpStatus.NOT_ACCEPTABLE);
        }
        List dataColumnList = baseDao.findBySql(SqlConstant.QUERY_TABLIE_COLUMN + "\'" + tableName + "\'");
        PoiUtil.importExcelDataFile(file,dataColumnList,tableName);
        return new ResponseEntity<>(ResultBean.success("文件上传成功"), HttpStatus.OK);
    }
    @ApiOperation("导入Csv数据库表")
    @PostMapping("/common/database/importCsvDataFile")
    @RequiresPermissions("导入数据库表")
    public ResponseEntity<ResultBean> importCsvDataFile(@ApiParam(value = "文件信息", required = true) @RequestParam("file") MultipartFile file, @RequestParam @ApiParam("数据库表名") String tableName) {
        if (Objects.isNull(file) || file.isEmpty()) {
            return new ResponseEntity<>(ResultBean.error("文件为空，请重新上传"), HttpStatus.NOT_ACCEPTABLE);
        }
        List dataColumnList = baseDao.findBySql(SqlConstant.QUERY_TABLIE_COLUMN + "\'" + tableName + "\'");
        try {
            PoiUtil.importCsvDataFile(file.getInputStream(),dataColumnList,tableName,0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(ResultBean.success("文件上传成功"), HttpStatus.OK);
    }
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mode", value = " 0设置命令等待时间(单位为毫秒) 1设置token存活时间(单位为毫秒) 2设置命令重发次数 3设置命令包的最大长度（不得超过220字节）", dataType = "int", paramType = "query")
    })
    @ApiOperation("设置通讯命令时间参数")
    @PutMapping("/common/command/time")
        @RequiresPermissions("设置通讯命令时间参数")
    public ResponseEntity<ResultBean> setCommandTime(@ApiParam("时间") @RequestParam @Min(message = "data.time.min",value = 0) Integer time,@RequestParam Integer mode){
        SystemVersion systemVersion = systemVersionDao.findById((long) 1).get();
        SystemVersion result = null;
        if(mode == 0) {
            systemVersion.setCommandWaitingTime(String.valueOf(time));
            systemVersion.setDate(new Timestamp(System.currentTimeMillis()));
            result = systemVersionDao.save(systemVersion);
        }
        else if(mode == 1){
            systemVersion.setTokenAliveTime(String.valueOf(time));
            systemVersion.setDate(new Timestamp(System.currentTimeMillis()));
            result = systemVersionDao.save(systemVersion);
        }
        else if(mode == 2){
            systemVersion.setCommandRepeatTime(String.valueOf(time));
            systemVersion.setDate(new Timestamp(System.currentTimeMillis()));
            result = systemVersionDao.save(systemVersion);
        }
        else if(mode == 3){
            systemVersion.setPackageLength(String.valueOf(time));
            systemVersion.setDate(new Timestamp(System.currentTimeMillis()));
            result = systemVersionDao.save(systemVersion);
        }
        systemVersionArgs.init();
        return new ResponseEntity<>(ResultBean.success(result),HttpStatus.OK);
    }
    @ApiOperation("设置系统版本号和开发人员")
    @PutMapping("/common/system")
    @RequiresPermissions("设置命令参数")
    public ResponseEntity<ResultBean> setSystemArgs(@ApiParam("版本号") @RequestParam  String softVersion,@ApiParam("开发人员")String productor){
        SystemVersion systemVersion = systemVersionDao.findById((long) 1).get();
        systemVersion.setSoftVersion(softVersion);
        systemVersion.setProductor(productor);
        systemVersion.setDate(new Timestamp(System.currentTimeMillis()));
        SystemVersion result = systemVersionDao.save(systemVersion);
        systemVersionArgs.init();
        return new ResponseEntity<>(ResultBean.success(result),HttpStatus.OK);
    }
}

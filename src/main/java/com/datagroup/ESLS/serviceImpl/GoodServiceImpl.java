package com.datagroup.ESLS.serviceImpl;

import com.datagroup.ESLS.common.constant.ArrtributeConstant;
import com.datagroup.ESLS.common.constant.ModeConstant;
import com.datagroup.ESLS.common.constant.SqlConstant;
import com.datagroup.ESLS.common.constant.TableConstant;
import com.datagroup.ESLS.common.request.RequestBean;
import com.datagroup.ESLS.common.request.RequestItem;
import com.datagroup.ESLS.common.response.ResponseBean;
import com.datagroup.ESLS.controller.CommonController;
import com.datagroup.ESLS.dao.GoodDao;
import com.datagroup.ESLS.dao.TagDao;
import com.datagroup.ESLS.entity.*;
import com.datagroup.ESLS.netty.command.CommandConstant;
import com.datagroup.ESLS.redis.RedisConstant;
import com.datagroup.ESLS.service.CycleJobService;
import com.datagroup.ESLS.service.GoodService;
import com.datagroup.ESLS.service.TagService;
import com.datagroup.ESLS.utils.*;
import io.lettuce.core.protocol.Command;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Service("GoodService")
public class GoodServiceImpl extends BaseServiceImpl implements GoodService {

    @Autowired
    private TagDao tagDao;
    @Autowired
    private GoodDao goodDao;
    @Autowired
    private NettyUtil nettyUtil;
    @Autowired
    private CycleJobService cycleJobService;
    @Autowired
    private TagService tagService;
    @Override
    public List<Good> findAll() {
        return goodDao.findAll();
    }

    @Override
    public List<Good> findAll(Integer page, Integer count) {
        List<Good> content = goodDao.findAll(PageRequest.of(page, count, Sort.Direction.DESC, "id")).getContent();
        return content;
    }

    @Override
    public Good findByBarCode(String barCode) {
        return goodDao.findByBarCode(barCode);
    }

    @Override
    public Good saveOne(Good good) {
        // 添加商品
        if(good.getId()!=0){
            Good g = goodDao.findById(good.getId()).get();
            String regionNames = g.getRegionNames();
            regionNames = StringUtil.isEmpty(regionNames)?new String():regionNames;
            String sql = SqlConstant.QUERY_TABLIE_COLUMN + "\'" + TableConstant.TABLE_GOODS + "\'";
            List<String> data = baseDao.findBySql(sql);
            for(String column:data) {
                if(isNotProperty(column)) continue;
                String sourceData = SpringContextUtil.getSourceData(column, good);
                String targetData = SpringContextUtil.getSourceData(column, g);
                if(sourceData!=null && targetData!=null && !sourceData.equals(targetData)){
                    System.out.println(column+"不同");
                    if(!regionNames.contains(column)) {
                        regionNames += (column + " ");
                    }
                }
            }
            // 0为等待更新
            good.setWaitUpdate(0);
            good.setRegionNames(regionNames);
        }
        else
            // 1为不更新
            good.setWaitUpdate(1);
        return goodDao.save(good);
    }

    @Override
    public Good updateGood(Good good) {
        Good goodbyBarCode = findByBarCode(good.getBarCode());
        if(goodbyBarCode!=null){
            good.setId(goodbyBarCode.getId());
            String regionNames = goodbyBarCode.getRegionNames();
            regionNames = StringUtil.isEmpty(regionNames)?new String():regionNames;
            String sql = SqlConstant.QUERY_TABLIE_COLUMN + "\'" + TableConstant.TABLE_GOODS + "\'";
            List<String> data = baseDao.findBySql(sql);
            for(String column:data) {
                if(isNotProperty(column)) continue;
                String sourceData = SpringContextUtil.getSourceData(column, goodbyBarCode);
                String targetData = SpringContextUtil.getSourceData(column, good);
                if(sourceData!=null && targetData!=null && !sourceData.equals(targetData)){
                    System.out.println(column+"不同");
                    if(!regionNames.contains(column)) {
                        regionNames += (column + " ");
                    }
                }
            }
            // 0为等待更新
            good.setWaitUpdate(0);
            good.setRegionNames(regionNames);
        }
        // 发送更新命令
        updateGoods();
        return goodDao.save(good);
    }

    @Override
    public boolean setScheduleTask(String cron, String rootfilePath, Integer mode) {
        if(mode>-1) return false;
        CycleJob cycleJob = cycleJobService.findByMode(mode);
        cycleJob.setCron(cron);
        cycleJob.setArgs(rootfilePath);
        CycleJob result = cycleJobService.saveOne(cycleJob);
        if(result!=null)
            return true;
        else
            return false;
    }

    @Override
    public boolean uploadGoodData(MultipartFile file, Integer mode) {
        if(mode>-1)
            return false;
        List dataColumnList = findBySql(SqlConstant.QUERY_TABLIE_COLUMN + "\'" + "goods" + "\'");
        try {
            File localFile = FileUtil.multipartFileToFile(file);
            if(mode.equals(ModeConstant.DO_BY_TYPE_GOODS_SCAN)){
                // 添加
                PoiUtil.importCsvDataFile(new FileInputStream(localFile), dataColumnList, "goods",0);
            }
            else if(mode.equals(ModeConstant.DO_BY_TYPE_CHANGEGOODS_SCAN)){
                // 更新
                PoiUtil.importCsvDataFile(new FileInputStream(localFile), dataColumnList, "goods",1);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Good findById(Long id) {
        return goodDao.getOne(id);
    }

    @Override
    public boolean deleteById(Long id) {
        try{
            goodDao.deleteById(id);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }
    // 商品修改
    @Override
    public ResponseBean updateGoods(RequestBean requestBean) {
        int sum = 0;
        int successNumber = 0;
        // 商品修改标志
        List<Tag> tags = new ArrayList<>();
        try {
            for (RequestItem item : requestBean.getItems()) {
                List<Good> goods = findBySql(SqlConstant.getQuerySql(TableConstant.TABLE_GOODS, item.getQuery(), "=", item.getQueryString()), Good.class);
                for( Good good  :  goods){
                    if(good.getWaitUpdate()!=null && good.getWaitUpdate()==0) {
                        List<Tag> tagList = tagDao.findByGoodId(good.getId());
                        tags.addAll(tagList);
                    }
                }
            }
            ResponseBean responseBean ;
            if(tags.size()>1)
            {
                nettyUtil.awakeFirst(tags);
                responseBean = SendCommandUtil.updateTagStyle(tags);
                nettyUtil.awakeOverLast(tags);
            }
            else
                responseBean = SendCommandUtil.updateTagStyle(tags);
            sum +=responseBean.getSum();
            successNumber +=responseBean.getSuccessNumber();
        } catch (Exception e) { }
        return new ResponseBean(sum, successNumber);
    }
    // 商品改价
    @Override
    public ResponseBean updateGoods() {
        ResponseBean responseBean = null;
        List<Good> goods = findBySql(SqlConstant.getQuerySql(TableConstant.TABLE_GOODS, ArrtributeConstant.GOOD_WAITUPDATE, "=", "0"), Good.class);
        List<Tag> tags = new ArrayList<>();
        try {
            for( Good good  :  goods){
                // 此商品绑定的所有标签
                List<Tag> tagList = tagDao.findByGoodId(good.getId());
                tags.addAll(tagList);
            }
            if(tags.size()>1)
            {
                nettyUtil.awakeFirst(tags);
                responseBean = SendCommandUtil.updateTagStyle(tags);
                nettyUtil.awakeOverLast(tags);
            }
            else
                responseBean = SendCommandUtil.updateTagStyle(tags);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return responseBean;
    }

    @Override
    public List<Tag> getBindTags(String query,String connection, String queryString) {
        if(connection.equals("like"))
            queryString = "%"+queryString+"%";
        List<Good> goods = findBySql(SqlConstant.getQuerySql(TableConstant.TABLE_GOODS, query, connection, queryString), Good.class);
        List<Tag> resultList = new ArrayList<>();
        try {
            for (Good good : goods) {
                List<Tag> tags = tagDao.findByGoodId(good.getId());
                resultList.addAll(tags);
            }
        }
        catch (Exception e){}
        return resultList;
    }
    private boolean isNotProperty(String value){
        if(value.equals("waitUpdate") || value.equals("photo") || value.equals("importTime")|| value.equals("rfu01")|| value.equals("rfu02")
                || value.equals("rfus01")
                || value.equals("rfus02")
                || value.equals("regionNames")
                || value.equals("status"))
            return true;
        return false;
    }
}

package com.datagroup.ESLS.serviceImpl;

import com.datagroup.ESLS.common.constant.ArrtributeConstant;
import com.datagroup.ESLS.common.constant.SqlConstant;
import com.datagroup.ESLS.common.constant.TableConstant;
import com.datagroup.ESLS.common.request.RequestBean;
import com.datagroup.ESLS.common.request.RequestItem;
import com.datagroup.ESLS.common.response.ResponseBean;
import com.datagroup.ESLS.controller.CommonController;
import com.datagroup.ESLS.dao.GoodDao;
import com.datagroup.ESLS.dao.TagDao;
import com.datagroup.ESLS.entity.Dispms;
import com.datagroup.ESLS.entity.Good;
import com.datagroup.ESLS.entity.Router;
import com.datagroup.ESLS.entity.Tag;
import com.datagroup.ESLS.netty.command.CommandConstant;
import com.datagroup.ESLS.redis.RedisConstant;
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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Service("GoodService")
public class GoodServiceImpl extends BaseServiceImpl implements GoodService {

    @Autowired
    private GoodDao goodDao;
    @Autowired
    private TagDao tagDao;
    @Autowired
    private TagService tagService;
    @Autowired
    private NettyUtil nettyUtil;
    @Autowired
    private CommonController commonController;
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
    public Good saveOne(Good good) {
        System.out.println("上传good");
        System.out.println(good);
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
        int sum = 0;
        int successNumber = 0;
        List<Good> goods = findBySql(SqlConstant.getQuerySql(TableConstant.TABLE_GOODS, ArrtributeConstant.GOOD_WAITUPDATE, "=", "0"), Good.class);
        List<Tag> tags = new ArrayList<>();
        try {
            for( Good good  :  goods){
                // 此商品绑定的所有标签
                List<Tag> tagList = tagDao.findByGoodId(good.getId());
                tags.addAll(tagList);
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
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseBean(sum, successNumber);
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

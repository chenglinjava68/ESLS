package com.datagroup.ESLS.service;

import com.datagroup.ESLS.common.request.RequestBean;
import com.datagroup.ESLS.common.response.ResponseBean;
import com.datagroup.ESLS.entity.Good;
import com.datagroup.ESLS.entity.Tag;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GoodService extends Service{
    List<Good> findAll();
    List<Good> findAll(Integer page, Integer count);
    Good findByBarCode(String barCode);
    Good saveOne(Good good);
    Good updateGood(Good good);
    boolean setScheduleTask(String cron, String rootfilePath, Integer mode);
    boolean uploadGoodData(MultipartFile file,Integer mode);
    Good findById(Long id);
    boolean deleteById(Long id);
    // 对商品绑定的所有标签内容进行更新
    ResponseBean updateGoods(RequestBean requestBean);
    // 更新商品waitupdate字段为0的所有商品对应的标签
    ResponseBean updateGoods();
    // 通过商品信息获取其绑定的所有商品
    List<Tag> getBindTags(String query,String connection,String queryString);
}

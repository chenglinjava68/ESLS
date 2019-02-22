package com.datagroup.ESLS.serviceImpl;

import com.datagroup.ESLS.common.constant.ArrtributeConstant;
import com.datagroup.ESLS.common.constant.ModeConstant;
import com.datagroup.ESLS.common.constant.StyleType;
import com.datagroup.ESLS.common.constant.TableConstant;
import com.datagroup.ESLS.common.request.RequestBean;
import com.datagroup.ESLS.common.request.RequestItem;
import com.datagroup.ESLS.common.response.ResponseBean;
import com.datagroup.ESLS.cycleJob.DynamicTask;
import com.datagroup.ESLS.dao.CycleJobDao;
import com.datagroup.ESLS.dao.DispmsDao;
import com.datagroup.ESLS.dao.StyleDao;
import com.datagroup.ESLS.dao.TagDao;
import com.datagroup.ESLS.entity.Dispms;
import com.datagroup.ESLS.entity.Style;
import com.datagroup.ESLS.entity.Tag;
import com.datagroup.ESLS.entity.CycleJob;
import com.datagroup.ESLS.netty.command.CommandConstant;
import com.datagroup.ESLS.service.StyleService;
import com.datagroup.ESLS.service.TagService;
import com.datagroup.ESLS.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service("StyleService")
@Slf4j
public class StyleServiceImpl extends BaseServiceImpl implements StyleService {
    // 向选用该样式的标签发送刷新命令
    @Override
    public ResponseBean flushTags(RequestBean requestBean, Integer mode) {
        String contentType = CommandConstant.FLUSH;
        ResponseBean responseBean = null;
        List<Tag> tags = new ArrayList<>();
        for (RequestItem items : requestBean.getItems()) {
            // 获取指定属性的所有标签
            List<Style> styleList = findByArrtribute(TableConstant.TABLE_STYLE, items.getQuery(), items.getQueryString(), Style.class);
            for (Style style : styleList) {
                List<Tag> tagList = findByArrtribute(TableConstant.TABLE_TAGS, ArrtributeConstant.TAG_STYLEID, String.valueOf(style.getId()), Tag.class);
                tags.addAll(tagList);
            }
        }
        if (mode == 0) {
            log.info("向选用该样式的标签发送刷新命令");
            if(tags.size()>1) {
                nettyUtil.awakeFirst(tags);
                responseBean = SendCommandUtil.sendCommandWithTags(tags, contentType, CommandConstant.COMMANDTYPE_TAG);
                nettyUtil.awakeOverLast(tags);
            }
            else
                responseBean = SendCommandUtil.sendCommandWithTags(tags,contentType,CommandConstant.COMMANDTYPE_TAG);
        } else if (mode == 1) {
            log.info("向选用该样式的标签发送定期刷新");
            // 设置定期刷新
            CycleJob cyclejob = new CycleJob();
            cyclejob.setCron("0 0/1 * * * ?");
            cyclejob.setArgs(RequestBeanUtil.getRequestBeanAsString(tags));
            cyclejob.setMode(Integer.valueOf(ModeConstant.DO_BY_TAG));
            // 0刷新1巡检
            cyclejob.setType(ModeConstant.DO_BY_TAG_FLUSH);
            cycleJobDao.save(cyclejob);
            dynamicTask.addFlushTask("0 0/1 * * * ?",requestBean, ModeConstant.DO_BY_TAG);
            return new ResponseBean(requestBean.getItems().size(), requestBean.getItems().size());
        }
        return responseBean;
    }
    // 样式更改
    @Override
    public ResponseBean updateStyleById(long styleId, List<Long> dispmIds,Style style) {
        int sum = dispmIds.size();
        int successnumber = 0;
        // 更新所有小样式
        for(Long id : dispmIds) {
            try {
                Dispms dispms = dispmsDao.findById(id).get();
                dispms.setStyle(style);
                dispmsDao.save(dispms);
                successnumber++;
            } catch (Exception e) {
                System.out.println("StyleService - updateStyleById : " + e);
            }
        }
        // 通过styleId查找使用了此样式的所有标签实体
        List<Tag> tags = findByArrtribute(TableConstant.TABLE_TAGS, ArrtributeConstant.TAG_STYLEID, String.valueOf(styleId), Tag.class);
        // 通过标签实体的路由器IP地址发送更改标签内容包
        SendCommandUtil.updateTagStyle(tags);
        return  new ResponseBean(sum,successnumber);
    }

    @Override
    public Style findByStyleNumber(String styleNumber) {
        return styleDao.findByStyleNumber(styleNumber);
    }
    @Override
    public List<Style> findAll() {
        return styleDao.findAll();
    }
    @Override
    public List<Style> findAll(Integer page, Integer count){
        List<Style> content = styleDao.findAll(PageRequest.of(page, count, Sort.Direction.DESC, "id")).getContent();
        return content;
    }
    @Override
    public Style saveOne(Style style) {
        return styleDao.save(style);
    }

    @Override
    public List<Style> findByWidth(Integer width) {
        return styleDao.findByWidth(width);
    }

    @Override
    public Style saveOne(String styleType,String cron) {
        Style style = new Style();
        style.setCron(cron);
        style.setStyleType(styleType);
        Integer width;
        String begin = ""+styleType.charAt(0)+styleType.charAt(2);
        // 250 122
        if(styleType.contains("2.13")  && styleType.contains("黑白")){
            begin = "25";
            String[] s = StyleType.keyToWHMap.get("25").split(" ");
            width = Integer.valueOf(s[0]);
            style.setWidth(width);
            style.setHeight(Integer.valueOf(s[1]));
        }
        else {
            String[] s = StyleType.keyToWHMap.get(begin).split(" ");
            width = Integer.valueOf(s[0]);
            style.setWidth(width);
            style.setHeight(Integer.valueOf(s[1]));
        }
        List<Style> styles = findByWidth(width);
        String end = String.valueOf(styles.size()+1);;
        if(styles.size()+1<10)
            end = "0"+String.valueOf(styles.size()+1);
        style.setStyleNumber(begin+end);
        return styleDao.save(style);
    }

    @Override
    public Optional<Style> findById(Long id) {
        return styleDao.findById(id);
    }

    @Override
    public boolean deleteById(Long id) {
        List<Dispms> dispmsIds = dispmsDao.findByStyleId(id);
        for(Dispms dispms : dispmsIds)
            dispmsDao.deleteById(dispms.getId());
        try{
            styleDao.deleteById(id);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }
    @Autowired
    private StyleDao styleDao;
    @Autowired
    private DispmsDao dispmsDao;
    @Autowired
    private NettyUtil nettyUtil;
    @Autowired
    private TagDao tagDao;
    @Autowired
    private TagService tagService;
    @Autowired
    private CycleJobDao cycleJobDao;
    @Autowired
    private DynamicTask dynamicTask;
}

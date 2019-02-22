package com.datagroup.ESLS.serviceImpl;

import com.datagroup.ESLS.common.exception.TagServiceException;
import com.datagroup.ESLS.cycleJob.DynamicTask;
import com.datagroup.ESLS.common.constant.ArrtributeConstant;
import com.datagroup.ESLS.common.constant.ModeConstant;
import com.datagroup.ESLS.common.constant.TableConstant;
import com.datagroup.ESLS.common.request.RequestBean;
import com.datagroup.ESLS.common.response.ByteResponse;
import com.datagroup.ESLS.common.response.ResponseBean;
import com.datagroup.ESLS.common.response.ResultBean;
import com.datagroup.ESLS.dao.CycleJobDao;
import com.datagroup.ESLS.dao.GoodDao;
import com.datagroup.ESLS.dao.StyleDao;
import com.datagroup.ESLS.dao.TagDao;
import com.datagroup.ESLS.entity.*;
import com.datagroup.ESLS.netty.command.CommandConstant;
import com.datagroup.ESLS.service.DispmsService;
import com.datagroup.ESLS.service.TagService;
import com.datagroup.ESLS.utils.*;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service("TagService")
@Slf4j
public class TagServiceImpl extends BaseServiceImpl implements TagService {
    // 更新标签样式
    @Override
    public ResponseBean updateTagStyle(Tag tag, List<Dispms> dispmses) {
        int successNumber = 0;
        long begin = System.currentTimeMillis();
        try {
            if(tag==null)  return new ResponseBean(0, successNumber);
            Channel channel = SpringContextUtil.getChannelByRouter(tag.getRouter().getId());
//            if(channel==null)
//                throw new TagServiceException(ResultEnum.COMMUNITICATION_ERROR);
            System.out.println(dispmses.size());
            List<Dispms> dispmsesList = new ArrayList<>();
            Good good = tag.getGood();
            String regionNames = good.getRegionNames();
            boolean isRegion = !StringUtil.isEmpty(regionNames)?true:false;
            ByteResponse byteResponse;
            // 改价只更改区域
            if(isRegion){
                System.out.println("区域");
                for(Dispms dispms:dispmses)
                    if(dispms.getStatus()!=null && dispms.getStatus()==1 && regionNames.contains(dispms.getSourceColumn()))
                        dispmsesList.add(dispms);
                HashMap<String,Integer> regionIdMap = SpringContextUtil.getRegionIdList(regionNames,dispmses);
                // price spec name unit origin category barCode 0 3 2
                byteResponse = SpringContextUtil.getRegionRequest(dispmsesList, tag.getStyle().getStyleNumber(), good,regionIdMap);
                // 商品改价置1更新完毕
                good.setWaitUpdate(1);
                good.setRegionNames(null);
                goodDao.save(good);
            }
            else{
                for(int i=0 ;i<dispmses.size();i++) {
                    Dispms dispms = dispmses.get(i);
                    System.out.println("基本样式："+dispms.getSourceColumn());
                    if ( dispms.getStatus() == 1)
                        dispmsesList.add(dispmses.get(i));
                }
                byteResponse = SpringContextUtil.getRequest(dispmsesList, tag.getStyle().getStyleNumber(), good);
            }
            String resultString = "";
            if(!isRegion) {
                // 更改样式全局信息包
                resultString = nettyUtil.sendMessageWithRepeat(channel, CommandConstant.getBytesByType(SpringContextUtil.getAddressByBarCode(tag.getBarCode()), byteResponse.getFirstByte(), CommandConstant.COMMANDTYPE_TAG), Integer.valueOf(SystemVersionArgs.commandRepeatTime));
                System.out.println("更改样式全局信息包命令响应结果：" + resultString);
                if (ErrorUtil.isErrorCommunication(resultString))
                    return new ResponseBean(1, 0);
            }
            // 样式具体内容分包
            List<byte[]> byteList = byteResponse.getByteList();
            for(int i=0;i<byteList.size();i++) {
                resultString = nettyUtil.sendMessageWithRepeat(channel, CommandConstant.getBytesByType(SpringContextUtil.getAddressByBarCode(tag.getBarCode()), byteList.get(i),CommandConstant.COMMANDTYPE_TAG),Integer.valueOf(SystemVersionArgs.commandRepeatTime));
                System.out.println("样式具体内容分包命令"+i+"响应结果：" + resultString);
                if(ErrorUtil.isErrorCommunication(resultString))
                    return new ResponseBean(1, 0);
            }
            if("成功".equals(TagUtil.judgeResultAndSettingTag(resultString,begin,tag)))
                successNumber++;
        }
        catch (Exception e){
            System.out.println("TagServiceImpl--updateTagStyle : "+e);
        }
        return new ResponseBean(1, successNumber);
    }
    // 刷新指定标签
    @Override
    public ResponseBean flushTags(RequestBean requestBean) throws TagServiceException {
        String contentType = CommandConstant.FLUSH;
        List<Tag> tags = RequestBeanUtil.getTagsByRequestBean(requestBean);
        ResponseBean responseBean;
        if(tags.size()>1) {
            nettyUtil.awakeFirst(tags);
            responseBean = SendCommandUtil.sendCommandWithTags(tags, contentType, CommandConstant.COMMANDTYPE_TAG);
            nettyUtil.awakeOverLast(tags);
        }
        else
            responseBean = SendCommandUtil.sendCommandWithTags(tags,contentType,CommandConstant.COMMANDTYPE_TAG);
        return responseBean;
    }
    // 刷新指定路由器下的所有标签
    @Override
    public ResponseBean flushTagsByRouter(RequestBean requestBean) {
        String contentType = CommandConstant.FLUSH;
        List<Router> routers = RequestBeanUtil.getRoutersByRequestBean(requestBean);
        ResponseBean responseBean = SendCommandUtil.sendCommandWithRouters(routers, contentType,CommandConstant.COMMANDTYPE_ROUTER);
        return responseBean;
    }
    // 定期刷新
    @Override
    public ResponseBean flushTagsByCycle(RequestBean requestBean,Integer mode) {
        // 设置定期刷新
        CycleJob cyclejob = new CycleJob();
        cyclejob.setCron("0 0/1 * * * ?");
        cyclejob.setArgs(RequestBeanUtil.getRequestBeanAsString(requestBean));
        cyclejob.setMode(mode);
        cyclejob.setType(ModeConstant.DO_BY_TAG_FLUSH);
        cycleJobDao.save(cyclejob);
        dynamicTask.addFlushTask("0 0/1 * * * ?",requestBean,mode);
        return new ResponseBean(requestBean.getItems().size(), requestBean.getItems().size());
    }
    // 巡检指定地址的标签
    @Override
    public ResponseBean scanTags(RequestBean requestBean) {
        String contentType = CommandConstant.QUERYTAG;
        List<Tag> tags = RequestBeanUtil.getTagsByRequestBean(requestBean);
        ResponseBean responseBean;
        TagUtil.setIsNotWorking(tags);
        if(tags.size()>1){
            nettyUtil.awakeFirst(tags);
            responseBean = SendCommandUtil.sendCommandWithTags(tags,contentType,CommandConstant.COMMANDTYPE_TAG);
            nettyUtil.awakeOverLast(tags);
        }
        else
            responseBean = SendCommandUtil.sendCommandWithTags(tags,contentType,CommandConstant.COMMANDTYPE_TAG);
        return responseBean;
    }
    // 巡检指定路由器下的所有标签(广播命令只发一次)
    @Override
    public ResponseBean scanTagsByRouter(RequestBean requestBean) {
        String contentType = CommandConstant.QUERYTAG;
        List<Router> routers = RequestBeanUtil.getRoutersByRequestBean(requestBean);
        List<Tag> tags = TagUtil.getTagsByRouters(routers);
        TagUtil.setIsNotWorking(tags);
        ResponseBean responseBean = SendCommandUtil.sendCommandWithRouters(routers, contentType,CommandConstant.COMMANDTYPE_ROUTER);
        return responseBean;
    }
    // 定期巡检
    @Override
    public ResponseBean scanTagsByCycle(RequestBean requestBean,Integer mode) {
        // 设置定期巡检
        CycleJob cyclejob = new CycleJob();
        // cron表达式
        cyclejob.setCron("0 0/1 * * * ?");
        cyclejob.setArgs(RequestBeanUtil.getRequestBeanAsString(requestBean));
        cyclejob.setMode(mode);
        cyclejob.setType(ModeConstant.DO_BY_TAG_SCAN);
        cycleJobDao.save(cyclejob);
        dynamicTask.addTagScanTask("0 0/1 * * * ?",requestBean, mode);
        return new ResponseBean(requestBean.getItems().size(), requestBean.getItems().size());
    }

    // 闪灯或者结束闪灯
    @Override
    public ResponseBean changeLightStatus(RequestBean requestBean, Integer mode) {
        ResponseBean responseBean = null;
        try {
            String contentType = null;
            if (mode == 0) {
                log.info("向指定的信息集合发送结束闪灯命令");
                contentType= CommandConstant.TAGBLINGOVER;
            } else if (mode == 1) {
                log.info("向指定的信息集合发送闪灯命令");
                contentType = CommandConstant.TAGBLING;
            }
            List<Tag> tags = RequestBeanUtil.getTagsByRequestBean(requestBean);
            if(tags.size()>1){
                nettyUtil.awakeFirst(tags);
                responseBean = SendCommandUtil.sendCommandWithTags(tags,contentType,CommandConstant.COMMANDTYPE_TAG);
                nettyUtil.awakeOverLast(tags);
            }
            else
                responseBean = SendCommandUtil.sendCommandWithTags(tags,contentType,CommandConstant.COMMANDTYPE_TAG);
        }
        catch (Exception e) {
        }
        return responseBean;
    }
    // 对路由器下的所有标签闪灯或者结束闪灯
    @Override
    public ResponseBean changeLightStatusByRouter(RequestBean requestBean, Integer mode) {
        ResponseBean responseBean = null;
        try {
            String contentType = null;
            if (mode == 0) {
                log.info("向指定的信息集合发送结束闪灯命令");
                contentType= CommandConstant.TAGBLINGOVER;
            } else if (mode == 1) {
                log.info("向指定的信息集合发送闪灯命令");
                contentType = CommandConstant.TAGBLING;
            }
            List<Router> routers = RequestBeanUtil.getRoutersByRequestBean(requestBean);
            responseBean = SendCommandUtil.sendCommandWithRouters(routers, contentType, CommandConstant.COMMANDTYPE_ROUTER);
        }
        catch (Exception e) {
            System.out.println(e);
        }
        return responseBean;
    }
    //绑定商品和标签
    @Override
    public ResponseEntity<ResultBean> bindGoodAndTag(String sourceArgs1, String ArgsString1, String sourceArgs2, String ArgsString2, String mode) {
        // 获取商品实体
        List<Good> goods = findByArrtribute(TableConstant.TABLE_GOODS, sourceArgs1, ArgsString1, Good.class);
        // 获取标签实体
        List<Tag> tagList = findByArrtribute(TableConstant.TABLE_TAGS, sourceArgs2, ArgsString2, Tag.class);
        ResponseEntity<ResultBean> result ;
        if ((result = ResponseUtil.testListSize("没有相应的标签或商品 请重新选择", goods, tagList)) != null) return result;
        // 修改标签实体的goodid state
        if(goods.size()>1 ||  tagList.size()>1)
            return new ResponseEntity<>(ResultBean.error(" 根据字段获取的数据不唯一 请选择唯一字段 "), HttpStatus.BAD_REQUEST);
        Good good = goods.get(0);
        Tag tag = tagList.get(0);
        if (tag.getGood() == null) {
            if (mode.equals("1"))
                tag.setGood(good);
            else
                return new ResponseEntity<>(ResultBean.error(" 商品和标签暂未绑定 请改变mode重新发送请求 "), HttpStatus.BAD_REQUEST);
        } else if (tag.getGood() != null && mode.equals("1"))
            return new ResponseEntity<>(ResultBean.error(" 此标签已经绑定商品 请重新选择标签 "), HttpStatus.BAD_REQUEST);
        else {
            tag.setGood(null);
        }
        // regionNames置空
        good.setRegionNames(null);
        goodDao.save(good);
        List<Tag> tags = new ArrayList<>();
        tags.add(tag);
        try {
            if(mode.equals("1")){
                // 标签绑定商品命令
                // 多线程并行发送命令
                tag.setState((byte) 1);
                String contentType = CommandConstant.TAGBIND;
//                ResponseBean responseBean = SendCommandUtil.sendCommandWithTags(tags, contentType, CommandConstant.COMMANDTYPE_TAG);
//                if(responseBean.getSuccessNumber()==0)
//                    throw new TagServiceException(ResultEnum.GOOD_TAG_BIND_ERROR);
                SendCommandUtil.updateTagStyle(tags);
            }
            else if(mode.equals("0")){
                // 标签取消绑定商品命令
                tag.setState((byte) 0);
                String contentType = CommandConstant.TAGBINDOVER;
                SendCommandUtil.sendCommandWithTags(tags, contentType,CommandConstant.COMMANDTYPE_TAG);
            }
            saveOne(tag);
        } catch (Exception e) {
            System.out.println("发送样式修改包失败");
        }
        // 返回前端提示信息
        return new ResponseEntity<>(ResultBean.success(mode.equals("1") ? "绑定成功" : "取消绑定成功"), HttpStatus.OK);
    }

    // 标签移除 进入休眠状态
    @Override
    public ResponseBean removeTagCommand(RequestBean requestBean, Integer mode) {
        String contentType = CommandConstant.TAGREMOVE;
        ResponseBean responseBean = null ;
        if(mode==0) {
            List<Tag> tags = RequestBeanUtil.getTagsByRequestBean(requestBean);
            if (tags.size() > 1) {
                nettyUtil.awakeFirst(tags);
                responseBean = SendCommandUtil.sendCommandWithTags(tags, contentType, CommandConstant.COMMANDTYPE_TAG);
                nettyUtil.awakeOverLast(tags);
            } else
                responseBean = SendCommandUtil.sendCommandWithTags(tags, contentType, CommandConstant.COMMANDTYPE_TAG);
        }
        else if(mode == 1){
            List<Router> routers = RequestBeanUtil.getRoutersByRequestBean(requestBean);
            responseBean = SendCommandUtil.sendCommandWithRouters(routers, contentType, CommandConstant.COMMANDTYPE_ROUTER);
        }
        return responseBean;
    }

    // 标签更改样式
    @Override
    public ResponseEntity<ResultBean> updateTagStyleById(long tagId, long styleId) {
        // 修改标签实体对应的styleId
        List<Tag> tagList = findByArrtribute(TableConstant.TABLE_TAGS, ArrtributeConstant.TABLE_ID, String.valueOf(tagId), Tag.class);
        if (tagList.size() <= 0)
            return new ResponseEntity<>(ResultBean.error("没有相应ID的标签 请重新选择"), HttpStatus.BAD_REQUEST);
        Tag tag = tagList.get(0);
        if (tag.getStyle() != null && tag.getStyle().getId() == styleId)
            return new ResponseEntity<>(ResultBean.error("标签对应的样式一致,无需更改"), HttpStatus.BAD_REQUEST);
        else {
            Style one = styleDao.getOne(styleId);
            tag.setStyle(one);
            saveOne(tag);
            List<Tag> tags = new ArrayList<>();
            tags.add(tag);
            try {
                ResponseBean responseBean = SendCommandUtil.updateTagStyle(tags);
                return new ResponseEntity<>(ResultBean.success("样式更换成功(通信成功数："+responseBean.getSuccessNumber()+")"), HttpStatus.OK);
            }
            catch (Exception e){
                System.out.println("发送样式修改包失败"+e);
            }
            return new ResponseEntity<>(ResultBean.error("发送样式修改包失败"), HttpStatus.BAD_REQUEST);
        }
    }


    // 改变标签状态
    @Override
    public ResponseBean changeStatus(RequestBean requestBean, Integer mode) {
        int sum, successNumber=0;
        Tag tag;
        List<Tag> tags = RequestBeanUtil.getTagsByRequestBean(requestBean);
        sum = tags.size();
        if(mode == 0){
            removeTagCommand(requestBean,mode);
        }
        for (Tag itemTag : tags) {
            itemTag.setForbidState(mode);
            tag = saveOne(itemTag);
            if (tag != null)
                successNumber++;
        }
        return new ResponseBean(sum, successNumber);
    }
    @Override
    public List<Tag> findAll() {
        return tagDao.findAll();
    }

    @Override
    public List<Tag> findAll(Integer page, Integer count) {
        List<Tag> content = tagDao.findAll(PageRequest.of(page, count, Sort.Direction.DESC, "id")).getContent();
        return content;
    }

    @Override
    public List<Tag> findByRouterId(Long routerId) {
        return tagDao.findByRouterId(routerId);
    }

    @Override
    public Tag findByTagAddress(String tagAddress) {
        return tagDao.findByTagAddress(tagAddress);
    }

    @Override
    public Tag findByBarCode(String barCode) {
        return tagDao.findByBarCode(barCode);
    }

    @Override
    public Tag saveOne(Tag tag) {
        tag.setTagAddress(ByteUtil.getMergeMessage(SpringContextUtil.getAddressByBarCode(tag.getBarCode())));
        return tagDao.save(tag);
    }

    @Override
    public Optional<Tag> findById(Long id) {
        return tagDao.findById(id);
    }

    @Override
    public boolean deleteById(Long id) {
        try {
            tagDao.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    @Autowired
    private TagDao tagDao;
    @Autowired
    private GoodDao goodDao;
    @Autowired
    private StyleDao styleDao;
    @Autowired
    private NettyUtil nettyUtil;
    @Autowired
    private CycleJobDao cycleJobDao;
    @Autowired
    private DynamicTask dynamicTask;
    @Autowired
    private DispmsService dispmsService;
}

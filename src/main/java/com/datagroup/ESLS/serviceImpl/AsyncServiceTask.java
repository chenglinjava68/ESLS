package com.datagroup.ESLS.serviceImpl;

import com.datagroup.ESLS.common.response.ResponseBean;
import com.datagroup.ESLS.dao.TagDao;
import com.datagroup.ESLS.entity.Router;
import com.datagroup.ESLS.entity.Tag;
import com.datagroup.ESLS.netty.command.CommandConstant;
import com.datagroup.ESLS.netty.handler.ServiceHandler;
import com.datagroup.ESLS.service.RouterService;
import com.datagroup.ESLS.service.TagService;
import com.datagroup.ESLS.utils.NettyUtil;
import com.datagroup.ESLS.utils.SettingUtil;
import com.datagroup.ESLS.utils.SpringContextUtil;
import com.datagroup.ESLS.utils.TagUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.sql.Timestamp;


/**
 * @author lenovo
 */
@Slf4j
@Component("AsyncServiceTask")
public class AsyncServiceTask {
    @Autowired
    private NettyUtil nettyUtil;
    @Autowired
    private TagService tagService;

    @Async
    public ListenableFuture<String> sendMessageWithRepeat(Channel channel, byte[] message,Tag tag,long begin) {
        log.info("-----向(标签集合)发送命令线程-----");
        String result = nettyUtil.sendMessageWithRepeat(channel, message,SpringContextUtil.getRepeatTime());
        return new AsyncResult<>(TagUtil.judgeResultAndSettingTag(result,begin,tag));
    }
    @Async
    public ListenableFuture<String> sendMessageWithRepeat(Channel channel, byte[] message, Router router, long begin,int time) {
        log.info("-----向(路由器集合)发送命令线程-----");
        String result = nettyUtil.sendMessageWithRepeat(channel, message,time);
        return new AsyncResult<>(TagUtil.judgeResultAndSettingRouter(result,begin,router));
    }
    @Async
    public ListenableFuture<String> updateTagStyle(Tag tag,long begin) {
        log.info("-----向(标签集合)发送更新样式命令线程-----");
        ResponseBean responseBean = tagService.updateTagStyle(tag);
        String result = responseBean.getSuccessNumber()==1?"成功":"失败";
        return new AsyncResult<>(TagUtil.judgeResultAndSettingTag(result,begin,tag));
    }
}

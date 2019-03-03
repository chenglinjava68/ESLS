package com.datagroup.ESLS.serviceImpl;

import com.datagroup.ESLS.common.response.ResponseBean;
import com.datagroup.ESLS.entity.*;
import com.datagroup.ESLS.service.TagService;
import com.datagroup.ESLS.utils.NettyUtil;
import com.datagroup.ESLS.utils.SpringContextUtil;
import com.datagroup.ESLS.utils.TagUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


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
        String result = nettyUtil.sendMessageWithRepeat(channel, message, Integer.valueOf(SystemVersionArgs.commandRepeatTime),Integer.valueOf(SystemVersionArgs.commandWaitingTime));
        return new AsyncResult<>(TagUtil.judgeResultAndSettingTag(result,begin,tag));
    }
    @Async
    public ListenableFuture<String> sendMessageWithRepeat(Channel channel, byte[] message, Router router, long begin,int time) {
        log.info("-----向(路由器集合)发送命令线程-----");
        String result = nettyUtil.sendMessageWithRepeat(channel, message,time,Integer.valueOf(SystemVersionArgs.commandWaitingTime));
        return new AsyncResult<>(TagUtil.judgeResultAndSettingRouter(result,begin,router));
    }
    @Async
    public ListenableFuture<String> updateTagStyle(Tag tag,long begin) throws InterruptedException {
        log.info("-----向(标签集合)发送更新样式命令线程-----");
        ResponseBean responseBean = tagService.updateTagStyle(tag);
        String result = responseBean.getSuccessNumber()==1?"成功":"失败";
        return new AsyncResult<>(TagUtil.judgeResultAndSettingTagWaitUpdate(result,begin,tag));
    }
}

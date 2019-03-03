package com.datagroup.ESLS.utils;

import com.datagroup.ESLS.dto.TagsAndRouter;
import com.datagroup.ESLS.entity.SystemVersionArgs;
import com.datagroup.ESLS.netty.client.NettyClient;
import com.datagroup.ESLS.netty.command.CommandConstant;
import com.datagroup.ESLS.netty.server.ServerChannelHandler;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
@Slf4j
public class NettyUtil{
    // 单例模式
    @Autowired
    private ExecutorService executorService;
    public String sendMessageWithRepeat(Channel channel, byte[] message,int time,int waitingTime){
        waitFreeRouter(channel);
        String result = sendMessage(channel, message,waitingTime);
        SpringContextUtil.removeWorkingChannel(channel);
        for(int i=0;i<time-1;i++){
            if(result==null|| result.equals("失败")) {
                waitFreeRouter(channel);
                result = sendMessage(channel, message,waitingTime);
                SpringContextUtil.removeWorkingChannel(channel);
            }
            if(result!=null && (result.equals("成功") || result.equals("通讯超时")))
                return result;
        }
        return "通讯"+time+"次超时";
    }
    public String sendMessage(Channel channel, byte[] message,int waitingTime) {
        SpringContextUtil.addWorkingChannel(channel);
        try {
            //   ExecutorService executorService = Executors.newSingleThreadExecutor();
            NettyClient nettyClient = new NettyClient(channel, message);
            Future future = executorService.submit(nettyClient);
            long begin = System.currentTimeMillis();
            Integer commandWaitingTime = waitingTime;
            while(!future.isDone()){
                long end = System.currentTimeMillis();
                if((end - begin)> commandWaitingTime) {
                    log.info("NettyUtil--sendMessage : 命令没有响应");
                    ServerChannelHandler serverChannelHandler = SpringContextUtil.serverChannelHandler;
                    log.info("sendMessage 开始："+serverChannelHandler.getMapSize());
                    serverChannelHandler.removeMapWithKey(channel, message);
                    log.info("sendMessage 结束："+serverChannelHandler.getMapSize());
                    future.cancel(true);
                    return null;
                }
            }
            return future.get().toString();
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }
    public static synchronized void waitFreeRouter(Channel channel){
        while(true) {
            //路由器不工作时退出循环
            if(!SpringContextUtil.isWorking(channel))
                break;
        }
    }
    public void awakeFirst(List tags){
        // 对多个标签操作需要先批量唤醒，以路由器为单位进行唤醒
        List<TagsAndRouter> tagsAndRouters = TagUtil.splitTagsByRouter(tags);
        for(TagsAndRouter tagsAndRouter : tagsAndRouters)
            SendCommandUtil.sendAwakeMessage(SpringContextUtil.getAwakeBytes(tagsAndRouter.getTags()), tagsAndRouter.getRouter(), CommandConstant.COMMANDTYPE_ROUTER);
    }
    public void awakeOverLast(List tags) {
        // 以路由器为单位结束唤醒
        List<TagsAndRouter> tagsAndRouters = TagUtil.splitTagsByRouter(tags);
        for (TagsAndRouter tagsAndRouter : tagsAndRouters) {
            Channel channel = SpringContextUtil.getChannelByRouter(tagsAndRouter.getRouter().getId());
            sendMessageWithRepeat(channel, CommandConstant.COMMAND_BYTE.get(CommandConstant.ROUTERAWAKEOVER), 1,100);
        }
    }
}

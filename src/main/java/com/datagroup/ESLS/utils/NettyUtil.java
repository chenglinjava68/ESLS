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
    // Executor 管理多个异步任务的执行，而无需程序员显式地管理线程的生命周期。这里的异步是指多个任务的执行互不干扰，不需要进行同步操作。
    public String sendMessageWithRepeat(Channel channel, byte[] message,int time,int waitingTime){
        waitFreeRouter(channel);
        SpringContextUtil.addWorkingChannel(channel.id().toString());
        String result = sendMessage(channel, message,waitingTime);
        for(int i=0;i<time-1;i++){
            if(result==null|| result.equals("失败")) {
                result = sendMessage(channel, message,waitingTime);
            }
            if(result!=null && (result.equals("成功") || result.equals("通讯超时"))) {
                SpringContextUtil.removeWorkingChannel(channel.id().toString());
                return result;
            }
        }
        SpringContextUtil.removeWorkingChannel(channel.id().toString());
        byte[] overTimeMessage = getOverTimeMessage(message);
        sendMessage(channel, overTimeMessage,100);
        return "通讯"+time+"次超时";
    }
    public String sendMessage(Channel channel, byte[] message,int waitingTime) {
        try {
            //   ExecutorService executorService = Executors.newSingleThreadExecutor();
            NettyClient nettyClient = new NettyClient(channel, message);
            Future future = executorService.submit(nettyClient);
            long begin = System.currentTimeMillis();
            Integer commandWaitingTime = waitingTime;
            while(!future.isDone()){
                long end = System.currentTimeMillis();
                if((end - begin)> commandWaitingTime) {
                    ServerChannelHandler serverChannelHandler = SpringContextUtil.serverChannelHandler;
                    if(!serverChannelHandler.isBroadcastCommand(message)) {
                        log.info("线程移除前（命令没有响应）:" + serverChannelHandler.getMapSize());
                        serverChannelHandler.removeMapWithKey(channel, message);
                        log.info("线程移除后（命令没有响应）:" + serverChannelHandler.getMapSize());
                    }
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
    public static void waitFreeRouter(Channel channel){
        while(true) {
            //路由器不工作时退出循环
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!SpringContextUtil.isWorking(channel.id().toString()))
                break;
        }
    }
    public void awakeFirst(List tags){
        // 对多个标签操作需要先批量唤醒，以路由器为单位进行唤醒
        List<TagsAndRouter> tagsAndRouters = TagUtil.splitTagsByRouter(tags);
        for(TagsAndRouter tagsAndRouter : tagsAndRouters)
            if(tagsAndRouter.getTags().size()>1)
                SendCommandUtil.sendAwakeMessage(SpringContextUtil.getAwakeBytes(tagsAndRouter.getTags()), tagsAndRouter.getRouter(), CommandConstant.COMMANDTYPE_ROUTER);
    }
    public void awakeOverLast(List tags) {
        // 以路由器为单位结束唤醒
        List<TagsAndRouter> tagsAndRouters = TagUtil.splitTagsByRouter(tags);
        for (TagsAndRouter tagsAndRouter : tagsAndRouters) {
            if(tagsAndRouter.getTags().size()>1) {
                Channel channel = SpringContextUtil.getChannelByRouter(tagsAndRouter.getRouter().getId());
                sendMessageWithRepeat(channel, CommandConstant.COMMAND_BYTE.get(CommandConstant.ROUTERAWAKEOVER), 1, 100);
            }
        }
    }
    public static byte[] getOverTimeMessage(byte[] message){
        byte[] overTimeMessage = new byte[13];
        for(int i=0;i<8;i++)
           overTimeMessage[i] = message[i];
        overTimeMessage[2] = 0;
        overTimeMessage[3] = 9;
        overTimeMessage[8] = 0x01;
        overTimeMessage[9] = 0x03;
        overTimeMessage[10] = 0x02;
        overTimeMessage[11] = message[8];
        overTimeMessage[12] = message[9];
        return overTimeMessage;
    }
}

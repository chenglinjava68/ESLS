package com.datagroup.ESLS.utils;

import com.datagroup.ESLS.dto.TagsAndRouter;
import com.datagroup.ESLS.entity.SystemVersionArgs;
import com.datagroup.ESLS.netty.client.NettyClient;
import com.datagroup.ESLS.netty.command.CommandConstant;
import com.datagroup.ESLS.netty.server.ServerChannelHandler;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class NettyUtil{
    // 单例模式
    @Autowired
    private ExecutorService executorService;
    public String sendMessage(Channel channel, byte[] message) {
        SpringContextUtil.addWorkingChannel(channel);
        try {
            //   ExecutorService executorService = Executors.newSingleThreadExecutor();
            NettyClient nettyClient = new NettyClient(channel, message);
            Future future = executorService.submit(nettyClient);
            long begin = System.currentTimeMillis();
            Integer commandWaitingTime = Integer.valueOf(SystemVersionArgs.commandWaitingTime);
            while(!future.isDone()){
                long end = System.currentTimeMillis();
                if((end - begin)> commandWaitingTime) {
                    System.out.println("NettyUtil--sendMessage : 命令没有响应");
                    ServerChannelHandler serverChannelHandler = SpringContextUtil.serverChannelHandler;
                    System.out.println("startAndWrite 开始："+serverChannelHandler.getMapSize());
                    serverChannelHandler.removeMapWithKey(channel, message);
                    System.out.println("startAndWrite 结束："+serverChannelHandler.getMapSize());
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
    public String sendMessageWithRepeat(Channel channel, byte[] message,int time){
        waitFreeRouter(channel);
        String result = sendMessage(channel, message);
        SpringContextUtil.removeWorkingChannel(channel);
        for(int i=0;i<time-1;i++){
            if(result==null|| result.equals("失败")) {
                result = sendMessage(channel, message);
                SpringContextUtil.removeWorkingChannel(channel);
            }
            if(result!=null && (result.equals("成功") || result.equals("通讯超时")))
                return result;
        }
        System.out.println("NettyUtil--sendMessageWithRepeat : 工作路由器数量:"+SpringContextUtil.getWorkingChannel().size());
        return "通讯"+time+"次超时";
    }
    public static void waitFreeRouter(Channel channel){
        while(true) {
            //路由器不工作时退出循环
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
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
            sendMessageWithRepeat(channel, CommandConstant.COMMAND_BYTE.get(CommandConstant.ROUTERAWAKEOVER), 1);
        }
    }
}

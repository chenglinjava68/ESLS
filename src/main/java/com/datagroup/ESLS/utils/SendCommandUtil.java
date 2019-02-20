package com.datagroup.ESLS.utils;

import com.datagroup.ESLS.common.response.ResponseBean;
import com.datagroup.ESLS.entity.Router;
import com.datagroup.ESLS.entity.Tag;
import com.datagroup.ESLS.netty.command.CommandConstant;
import com.datagroup.ESLS.serviceImpl.AsyncServiceTask;
import io.netty.channel.Channel;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
public class SendCommandUtil {
    public static ResponseBean sendCommandWithTags(List<Tag> tags,String contentType,Integer messageType){
        int sum=tags.size(), successNumber;
        ArrayList<ListenableFuture<String>> listenableFutures = new ArrayList<>();
        try {
            for (Tag tag : tags) {
                byte[] address = SpringContextUtil.getAddressByBarCode(tag.getBarCode());
                byte[] content = CommandConstant.COMMAND_BYTE.get(contentType);
                if(address == null || (tag.getForbidState()!=null && tag.getForbidState()==0 )) continue;
                byte[] message = CommandConstant.getBytesByType(address, content, messageType);
                Channel channel = SpringContextUtil.getChannelByRouter(tag.getRouter().getId());
                ListenableFuture<String> result = ((AsyncServiceTask) SpringContextUtil.getBean("AsyncServiceTask")).sendMessageWithRepeat(channel, message, tag, System.currentTimeMillis());
                listenableFutures.add(result);
            }
        }
        catch (Exception e){
            System.out.println(e);
        }
        //等待所有线程执行完在返回
        successNumber = waitAllThread(listenableFutures);
        return new ResponseBean(sum, successNumber);
    }
    public static ResponseBean sendCommandWithRouters(List<Router> routers, String contentType, Integer messageType){
        int sum= routers.size();
        ArrayList<ListenableFuture<String>> listenableFutures = new ArrayList<>();
        byte[] content = CommandConstant.COMMAND_BYTE.get(contentType);
        byte[] message = CommandConstant.getBytesByType(null, content, messageType);
        try{
            for (Router router : routers) {
                // 路由器未连接或禁用
                if(router.getIsWorking()==0 || (router.getState()!=null && router.getState()==0)) continue;
                Channel channel = SpringContextUtil.getChannelByRouter(router);
                // 广播命令只发一次 广播命令没有响应
                ListenableFuture<String> result = ((AsyncServiceTask) SpringContextUtil.getBean("AsyncServiceTask")).sendMessageWithRepeat(channel, message,router,System.currentTimeMillis(),1);
                listenableFutures.add(result);
            }
        }
        catch (Exception e){
            System.out.println(e);
        }
        return new ResponseBean(sum, sum);
    }
    public static ResponseBean sendCommandWithSettingRouters(List<Router> routers){
        int sum= routers.size(), successNumber;
        ArrayList<ListenableFuture<String>> listenableFutures = new ArrayList<>();
        try{
            for (Router router : routers) {
                if(router.getIsWorking()==0 || (router.getState()!=null && router.getState()==0)) continue;
                // 更新路由器 发送设置命令
                byte[] message = new byte[16];
                message[0]=0x02;
                message[1]=0x05;
                message[2]=0x0D;
                byte[] mac = ByteUtil.getMacMessage(router.getMac());
                for(int i = 0 ;i<mac.length;i++)
                    message[3+i] = mac[i];
                // IP地址
                String ip = router.getIp();
                String[] ips = ip.split("\\.");
                for(int i=0;i<4;i++)
                    message[9+i] = (byte) Integer.parseInt(ips[i]);
                // 信道
                message[13] = Byte.parseByte(router.getChannelId());
                // 频率
                byte[] frequency = SpringContextUtil.int2ByteArr(Integer.valueOf(router.getFrequency()), 2);
                for(int i = 0 ;i<frequency.length;i++)
                    message[14+i] = frequency[i];
                SpringContextUtil.printBytes("路由器设置信息：",message);
                byte[] realMessage = CommandConstant.getBytesByType(null, message, CommandConstant.COMMANDTYPE_ROUTER);
                Channel channel = SpringContextUtil.getChannelByRouter(router);
                ListenableFuture<String> result = ((AsyncServiceTask) SpringContextUtil.getBean("AsyncServiceTask")).sendMessageWithRepeat(channel, realMessage,router,System.currentTimeMillis(),SpringContextUtil.getRepeatTime());
                listenableFutures.add(result);
            }
        }
        catch (Exception e){
            System.out.println(e);
        }
        //等待所有线程执行完在返回
        successNumber = waitAllThread(listenableFutures);
        return new ResponseBean(sum, successNumber);
    }
    public static ResponseBean updateTagStyle(List<Tag> tags){
        int sum= tags.size();
        ArrayList<ListenableFuture<String>> listenableFutures = new ArrayList<>();
        try{
            for (Tag tag : tags) {
                if(tag.getForbidState()!=null && tag.getForbidState()==0) continue;
                ListenableFuture<String> result = ((AsyncServiceTask) SpringContextUtil.getBean("AsyncServiceTask")).updateTagStyle(tag,System.currentTimeMillis());
                listenableFutures.add(result);
            }
        }
        catch (Exception e){
            System.out.println("SendCommandUtil--updateTagStyle : "+e);
        }
         int successNumber = waitAllThread(listenableFutures);
        return new ResponseBean(sum, sum);
    }
    public  static ResponseBean sendAwakeMessage(List<byte[]> byteList,Router router,Integer messageType){
        int sum= byteList.size(), successNumber;
        ArrayList<ListenableFuture<String>> listenableFutures = new ArrayList<>();
        Channel channel = SpringContextUtil.getChannelByRouter(router);
        try {
            for (byte[] content : byteList) {
                byte[] message = CommandConstant.getBytesByType(null, content, messageType);
                ListenableFuture<String> result = ((AsyncServiceTask) SpringContextUtil.getBean("AsyncServiceTask")).sendMessageWithRepeat(channel, message, router, System.currentTimeMillis(), 1);
                listenableFutures.add(result);
                // 判断是否成功
            }
        }
        catch (Exception e){
            System.out.println(e);
        }
        //等待所有线程执行完在返回
        successNumber = waitAllThread(listenableFutures);
        return new ResponseBean(sum, successNumber);
    }
    public static int waitAllThread(ArrayList<ListenableFuture<String>> listenableFutures){
        ArrayList<String> listenableFuturesResults = new ArrayList<>();
        //等待所有线程执行完在返回
        int sumBreak = 0;
        try {
            while (true) {
                for (ListenableFuture<String> item : listenableFutures)
                    if (item.isDone()) {
                        sumBreak++;
                        System.out.println("waitAllThread : "+item.toString()+"响应结果:"+item.get());
                        if (item.get().equals("成功"))
                            listenableFuturesResults.add("成功");
                    }
                if (sumBreak == listenableFutures.size())
                    break;
            }
            return listenableFuturesResults.size();
        }
        catch (Exception e){}
        return 0;
    }
}

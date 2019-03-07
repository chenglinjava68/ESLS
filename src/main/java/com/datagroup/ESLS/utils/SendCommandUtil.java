package com.datagroup.ESLS.utils;

import com.datagroup.ESLS.common.response.ResponseBean;
import com.datagroup.ESLS.entity.Router;
import com.datagroup.ESLS.entity.SystemVersionArgs;
import com.datagroup.ESLS.entity.Tag;
import com.datagroup.ESLS.netty.command.CommandConstant;
import com.datagroup.ESLS.serviceImpl.AsyncServiceTask;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
@Slf4j
public class SendCommandUtil {
    public static ResponseBean sendCommandWithRouters(List<Router> routers, String contentType, Integer messageType){
        int sum= routers.size();
        ArrayList<ListenableFuture<String>> listenableFutures = new ArrayList<>();
        byte[] content = CommandConstant.COMMAND_BYTE.get(contentType);
        byte[] message = CommandConstant.getBytesByType(null, content, messageType);
        try{
            for (Router router : routers) {
                // 路由器未连接或禁用
                if( router.getState()!=null && router.getState()==0) continue;
                Channel channel = SocketChannelHelper.getChannelByRouter(router);
                if(channel == null) continue;
                // 广播命令只发一次 广播命令没有响应
                ListenableFuture<String> result = ((AsyncServiceTask) SpringContextUtil.getBean("AsyncServiceTask")).sendMessageWithRepeat(channel, message,router,System.currentTimeMillis(),1);
                listenableFutures.add(result);
            }
        }
        catch (Exception e){
            log.info("SendCommandUtil - sendCommandWithRouters : "+e);
        }
        return new ResponseBean(sum, sum);
    }
    public static ResponseBean sendCommandWithTags(List<Tag> tags,String contentType,Integer messageType){
        int sum=tags.size(), successNumber;
        ArrayList<ListenableFuture<String>> listenableFutures = new ArrayList<>();
        try {
            for (Tag tag : tags) {
                if(tag.getForbidState()==0)  continue;
                Channel channel = SocketChannelHelper.getChannelByRouter(tag.getRouter().getId());
                if(channel == null) continue;
                byte[] address = SpringContextUtil.getAddressByBarCode(tag.getBarCode());
                byte[] content = CommandConstant.COMMAND_BYTE.get(contentType);
                if(address == null || (tag.getForbidState()!=null && tag.getForbidState()==0 )) continue;
                byte[] message = CommandConstant.getBytesByType(address, content, messageType);
                ListenableFuture<String> result = ((AsyncServiceTask) SpringContextUtil.getBean("AsyncServiceTask")).sendMessageWithRepeat(channel, message, tag, System.currentTimeMillis());
                listenableFutures.add(result);
            }
        }
        catch (Exception e){
            log.info("SendCommandUtil - sendCommandWithTags : "+e);
        }
        //等待所有线程执行完在返回
        successNumber = waitAllThread(listenableFutures);
        return new ResponseBean(sum, successNumber);
    }
    public static ResponseBean sendCommandWithSettingRouters(List<Router> routers){
        int sum= routers.size(), successNumber;
        ArrayList<ListenableFuture<String>> listenableFutures = new ArrayList<>();
        try{
            for (Router router : routers) {
                Channel channel = SocketChannelHelper.getChannelByRouter(router);
                if(channel == null) continue;
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
                ListenableFuture<String> result = ((AsyncServiceTask) SpringContextUtil.getBean("AsyncServiceTask")).sendMessageWithRepeat(channel, realMessage,router,System.currentTimeMillis(), Integer.valueOf(SystemVersionArgs.commandRepeatTime));
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
        int sum= tags.size(),successNumber;
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
        successNumber = waitAllThread(listenableFutures);
        return new ResponseBean(sum, successNumber);
    }
    public static ResponseBean sendAwakeMessage(List<byte[]> byteList,Router router,Integer messageType){
        int sum= byteList.size(), successNumber;
        ArrayList<ListenableFuture<String>> listenableFutures = new ArrayList<>();
        Channel channel = SocketChannelHelper.getChannelByRouter(router);
        if(channel == null)
            return new ResponseBean(sum, 0);
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
        int sumBreak = 0,sumThreads = listenableFutures.size();
        try {
            while (true) {
                //遍历所有线程 获得结果
                for (int i=0;i<listenableFutures.size();i++) {
                    ListenableFuture<String> item = listenableFutures.get(i);
                    if (item.isDone()) {
                        sumBreak++;
                        listenableFutures.remove(i);
                        log.info(item.toString() + "最终响应结果:" + item.get());
                        if (item.get().equals("成功"))
                            listenableFuturesResults.add("成功");
                    }
                }
                if (sumBreak == sumThreads)
                    break;
            }
            return listenableFuturesResults.size();
        }
        catch (Exception e){}
        return 0;
    }

    // 路由器测试

    // AP写入
    public static ResponseBean sendAPWrite(List<Router> routers,String barCode,String channelId,String hardVersion){
        int sum= routers.size(), successNumber;
        ArrayList<ListenableFuture<String>> listenableFutures = new ArrayList<>();
        try{
            for (Router router : routers) {
                Channel channel = SocketChannelHelper.getChannelByRouter(router);
                if(channel == null) continue;
                if(router.getIsWorking()==0 || (router.getState()!=null && router.getState()==0)) continue;
                // 更新路由器 发送设置命令
                byte[] message = new byte[22];
                message[0]=0x09;
                message[1]=0x02;
                message[2]=0x13;
                // 条码
                for(int i = 0 ;i<barCode.length();i++)
                    message[3+i] = (byte) barCode.charAt(i);
                // 通道号
                message[15] = Byte.parseByte(channelId);
                // 硬件版本号
                byte[] versionMessage = ByteUtil.getVersionMessage(hardVersion);
                for(int i=0;i<versionMessage.length;i++)
                    message[16+i] = versionMessage[i];
                SpringContextUtil.printBytes("AP写入信息：",message);
                byte[] realMessage = CommandConstant.getBytesByType(null, message, CommandConstant.COMMANDTYPE_ROUTER);
                ListenableFuture<String> result = ((AsyncServiceTask) SpringContextUtil.getBean("AsyncServiceTask")).sendMessageWithRepeat(channel, realMessage,router,System.currentTimeMillis(), Integer.valueOf(SystemVersionArgs.commandRepeatTime));
                listenableFutures.add(result);
            }
        }
        catch (Exception e){
            log.error("sendAPWrite:"+e);
        }
        //等待所有线程执行完在返回
        successNumber = waitAllThread(listenableFutures);
        return new ResponseBean(sum, successNumber);
    }

    // AP读取
    public static ResponseBean sendAPRead(List<Router> routers){
        int sum= routers.size(), successNumber;
        ArrayList<ListenableFuture<String>> listenableFutures = new ArrayList<>();
        try{
            for (Router router : routers) {
                Channel channel = SocketChannelHelper.getChannelByRouter(router);
                if(channel == null) continue;
                if(router.getIsWorking()==0 || (router.getState()!=null && router.getState()==0)) continue;
                // 更新路由器 发送设置命令
                byte[] message = new byte[3];
                message[0]=0x09;
                message[1]=0x12;
                message[2]=0;
                SpringContextUtil.printBytes("AP读取信息：",message);
                byte[] realMessage = CommandConstant.getBytesByType(null, message, CommandConstant.COMMANDTYPE_ROUTER);
                ListenableFuture<String> result = ((AsyncServiceTask) SpringContextUtil.getBean("AsyncServiceTask")).sendMessageWithRepeat(channel, realMessage,router,System.currentTimeMillis(), Integer.valueOf(SystemVersionArgs.commandRepeatTime));
                listenableFutures.add(result);
            }
        }
        catch (Exception e){
            log.error("sendAPRead:"+e);
        }
        //等待所有线程执行完在返回
        successNumber = waitAllThread(listenableFutures);
        return new ResponseBean(sum, successNumber);
    }
    // AP发送无线帧
    public static ResponseBean sendAPByChannelId(List<Router> routers,String channelId){
        int sum= routers.size(), successNumber;
        ArrayList<ListenableFuture<String>> listenableFutures = new ArrayList<>();
        try{
            for (Router router : routers) {
                Channel channel = SocketChannelHelper.getChannelByRouter(router);
                if(channel == null) continue;
                if(router.getIsWorking()==0 || (router.getState()!=null && router.getState()==0)) continue;
                // 更新路由器 发送设置命令
                byte[] message = new byte[4];
                message[0]=0x09;
                message[1]=0x6;
                message[2]=0x01;
                message[3]=Byte.parseByte(channelId);
                SpringContextUtil.printBytes("AP发送无线帧：",message);
                byte[] realMessage = CommandConstant.getBytesByType(null, message, CommandConstant.COMMANDTYPE_ROUTER);
                ListenableFuture<String> result = ((AsyncServiceTask) SpringContextUtil.getBean("AsyncServiceTask")).sendMessageWithRepeat(channel, realMessage,router,System.currentTimeMillis(), Integer.valueOf(SystemVersionArgs.commandRepeatTime));
                listenableFutures.add(result);
            }
        }
        catch (Exception e){
            log.error("sendAPByChannelId:"+e);
        }
        //等待所有线程执行完在返回
        successNumber = waitAllThread(listenableFutures);
        return new ResponseBean(sum, successNumber);
    }
    // AP停止发送无线帧
    public static ResponseBean sendAPByChannelIdStop(List<Router> routers){
        int sum= routers.size(), successNumber;
        ArrayList<ListenableFuture<String>> listenableFutures = new ArrayList<>();
        try{
            for (Router router : routers) {
                Channel channel = SocketChannelHelper.getChannelByRouter(router);
                if(channel == null) continue;
                if(router.getIsWorking()==0 || (router.getState()!=null && router.getState()==0)) continue;
                // 更新路由器 发送设置命令
                byte[] message = new byte[3];
                message[0]=0x09;
                message[1]=0x16;
                message[2]=0;
                SpringContextUtil.printBytes("AP发送停止无线帧：",message);
                byte[] realMessage = CommandConstant.getBytesByType(null, message, CommandConstant.COMMANDTYPE_ROUTER);
                ListenableFuture<String> result = ((AsyncServiceTask) SpringContextUtil.getBean("AsyncServiceTask")).sendMessageWithRepeat(channel, realMessage,router,System.currentTimeMillis(), Integer.valueOf(SystemVersionArgs.commandRepeatTime));
                listenableFutures.add(result);
            }
        }
        catch (Exception e){
            log.error("sendAPByChannelIdStop:"+e);
        }
        //等待所有线程执行完在返回
        successNumber = waitAllThread(listenableFutures);
        return new ResponseBean(sum, successNumber);
    }
    // AP接收无线帧
    public static ResponseBean sendAPReceiveByChannelId(List<Router> routers,String channelId){
        int sum= routers.size(), successNumber;
        ArrayList<ListenableFuture<String>> listenableFutures = new ArrayList<>();
        try{
            for (Router router : routers) {
                Channel channel = SocketChannelHelper.getChannelByRouter(router);
                if(channel == null) continue;
                if(router.getIsWorking()==0 || (router.getState()!=null && router.getState()==0)) continue;
                // 更新路由器 发送设置命令
                byte[] message = new byte[4];
                message[0]=0x09;
                message[1]=0x7;
                message[2]=0x01;
                message[3]=Byte.parseByte(channelId);
                SpringContextUtil.printBytes("AP发送接收无线帧：",message);
                byte[] realMessage = CommandConstant.getBytesByType(null, message, CommandConstant.COMMANDTYPE_ROUTER);
                ListenableFuture<String> result = ((AsyncServiceTask) SpringContextUtil.getBean("AsyncServiceTask")).sendMessageWithRepeat(channel, realMessage,router,System.currentTimeMillis(), Integer.valueOf(SystemVersionArgs.commandRepeatTime));
                listenableFutures.add(result);
            }
        }
        catch (Exception e){
            log.error("sendAPReceiveByChannelId:"+e);
        }
        //等待所有线程执行完在返回
        successNumber = waitAllThread(listenableFutures);
        return new ResponseBean(sum, successNumber);
    }
    // AP发送停止接收无线帧
    public static ResponseBean sendAPReceiveByChannelIdStop(List<Router> routers){
        int sum= routers.size(), successNumber;
        ArrayList<ListenableFuture<String>> listenableFutures = new ArrayList<>();
        try{
            for (Router router : routers) {
                Channel channel = SocketChannelHelper.getChannelByRouter(router);
                if(channel == null) continue;
                if(router.getIsWorking()==0 || (router.getState()!=null && router.getState()==0)) continue;
                // 更新路由器 发送设置命令
                byte[] message = new byte[3];
                message[0]=0x09;
                message[1]=0x17;
                message[2]=0;
                SpringContextUtil.printBytes("AP发送停止接收无线帧：",message);
                byte[] realMessage = CommandConstant.getBytesByType(null, message, CommandConstant.COMMANDTYPE_ROUTER);
                ListenableFuture<String> result = ((AsyncServiceTask) SpringContextUtil.getBean("AsyncServiceTask")).sendMessageWithRepeat(channel, realMessage,router,System.currentTimeMillis(), Integer.valueOf(SystemVersionArgs.commandRepeatTime));
                listenableFutures.add(result);
            }
        }
        catch (Exception e){
            log.error("sendAPReceiveByChannelIdStop:"+e);
        }
        //等待所有线程执行完在返回
        successNumber = waitAllThread(listenableFutures);
        return new ResponseBean(sum, successNumber);
    }
    // 电子秤
    // 获取计量数据
    public static ResponseBean sendGetBalance(List<Tag> tags){
        int sum=tags.size(), successNumber;
        ArrayList<ListenableFuture<String>> listenableFutures = new ArrayList<>();
        try {
            for (Tag tag : tags) {
                Channel channel = SocketChannelHelper.getChannelByRouter(tag.getRouter().getId());
                if(channel == null) continue;
                byte[] message = new byte[3];
                message[0] = 0x08;
                message[1] = 0x01;
                message[2] = 0x00;
                byte[] address = SpringContextUtil.getAddressByBarCode(tag.getBarCode());
                if(address == null || (tag.getForbidState()!=null && tag.getForbidState()==0 )) continue;
                byte[] realMessage = CommandConstant.getBytesByType(address, message, CommandConstant.COMMANDTYPE_TAG);
                ListenableFuture<String> result = ((AsyncServiceTask) SpringContextUtil.getBean("AsyncServiceTask")).sendMessageWithRepeat(channel, realMessage, tag, System.currentTimeMillis());
                listenableFutures.add(result);
            }
        }
        catch (Exception e){
            log.error("sendGetBalance:"+e);
        }
        //等待所有线程执行完在返回
        successNumber = waitAllThread(listenableFutures);
        return new ResponseBean(sum, successNumber);
    }
    // 电子秤置零
    public static ResponseBean sendBalanceToZero(List<Tag> tags){
        int sum=tags.size(), successNumber;
        ArrayList<ListenableFuture<String>> listenableFutures = new ArrayList<>();
        try {
            for (Tag tag : tags) {
                Channel channel = SocketChannelHelper.getChannelByRouter(tag.getRouter().getId());
                if(channel == null) continue;
                byte[] message = new byte[3];
                message[0] = 0x08;
                message[1] = 0x02;
                message[2] = 0x00;
                byte[] address = SpringContextUtil.getAddressByBarCode(tag.getBarCode());
                if(address == null || (tag.getForbidState()!=null && tag.getForbidState()==0 )) continue;
                byte[] realMessage = CommandConstant.getBytesByType(address, message, CommandConstant.COMMANDTYPE_TAG);
                ListenableFuture<String> result = ((AsyncServiceTask) SpringContextUtil.getBean("AsyncServiceTask")).sendMessageWithRepeat(channel, realMessage, tag, System.currentTimeMillis());
                listenableFutures.add(result);
            }
        }
        catch (Exception e){
            log.error("sendBalanceToZero:"+e);
        }
        //等待所有线程执行完在返回
        successNumber = waitAllThread(listenableFutures);
        return new ResponseBean(sum, successNumber);
    }
    // 电子秤去皮
    public static ResponseBean sendBalanceToFlay(List<Tag> tags){
        int sum=tags.size(), successNumber;
        ArrayList<ListenableFuture<String>> listenableFutures = new ArrayList<>();
        try {
            for (Tag tag : tags) {
                Channel channel = SocketChannelHelper.getChannelByRouter(tag.getRouter().getId());
                if(channel == null) continue;
                byte[] message = new byte[3];
                message[0] = 0x08;
                message[1] = 0x03;
                message[2] = 0x00;
                byte[] address = SpringContextUtil.getAddressByBarCode(tag.getBarCode());
                if(address == null || (tag.getForbidState()!=null && tag.getForbidState()==0 )) continue;
                byte[] realMessage = CommandConstant.getBytesByType(address, message, CommandConstant.COMMANDTYPE_TAG);
                ListenableFuture<String> result = ((AsyncServiceTask) SpringContextUtil.getBean("AsyncServiceTask")).sendMessageWithRepeat(channel, realMessage, tag, System.currentTimeMillis());
                listenableFutures.add(result);
            }
        }
        catch (Exception e){
            log.error("sendBalanceToFlay:"+e);
        }
        //等待所有线程执行完在返回
        successNumber = waitAllThread(listenableFutures);
        return new ResponseBean(sum, successNumber);
    }
    // 获取电子秤电量
    public static ResponseBean sendGetBalancePower(List<Tag> tags){
        int sum=tags.size(), successNumber;
        ArrayList<ListenableFuture<String>> listenableFutures = new ArrayList<>();
        try {
            for (Tag tag : tags) {
                Channel channel = SocketChannelHelper.getChannelByRouter(tag.getRouter().getId());
                if(channel == null) continue;
                byte[] message = new byte[3];
                message[0] = 0x08;
                message[1] = 0x04;
                message[2] = 0x00;
                byte[] address = SpringContextUtil.getAddressByBarCode(tag.getBarCode());
                if(address == null || (tag.getForbidState()!=null && tag.getForbidState()==0 )) continue;
                byte[] realMessage = CommandConstant.getBytesByType(address, message, CommandConstant.COMMANDTYPE_TAG);
                ListenableFuture<String> result = ((AsyncServiceTask) SpringContextUtil.getBean("AsyncServiceTask")).sendMessageWithRepeat(channel, realMessage, tag, System.currentTimeMillis());
                listenableFutures.add(result);
            }
        }
        catch (Exception e){
            log.error("sendGetBalancePower:"+e);
        }
        //等待所有线程执行完在返回
        successNumber = waitAllThread(listenableFutures);
        return new ResponseBean(sum, successNumber);
    }
}

package com.datagroup.ESLS.netty.server;

import com.datagroup.ESLS.common.constant.TableConstant;
import com.datagroup.ESLS.common.request.RequestBean;
import com.datagroup.ESLS.common.request.RequestItem;
import com.datagroup.ESLS.entity.Router;
import com.datagroup.ESLS.netty.command.CommandCategory;
import com.datagroup.ESLS.netty.command.CommandConstant;
import com.datagroup.ESLS.netty.executor.AsyncTask;
import com.datagroup.ESLS.netty.handler.ServiceHandler;
import com.datagroup.ESLS.service.RouterService;
import com.datagroup.ESLS.service.Service;
import com.datagroup.ESLS.utils.ByteUtil;
import com.datagroup.ESLS.utils.SpringContextUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.concurrent.ListenableFuture;

import java.net.InetSocketAddress;
import java.util.HashMap;

@Slf4j
@ChannelHandler.Sharable
public class ServerChannelHandler extends SimpleChannelInboundHandler<Object> {
    private HashMap<String,ChannelPromise> promiseMap = new HashMap<>();
    private HashMap<String,String> dataMap = new HashMap<>();

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("服务端客户加入连接====>" + ctx.channel().toString());
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        // 根据IP查询路由器信息
        Router router = ((RouterService) SpringContextUtil.getBean("RouterService")).findByIp(socketAddress.getAddress().getHostAddress());
        if(router!=null) {
            SpringContextUtil.getChannelIdGroup().put(router.getBarCode(),ctx.channel());
            // 更改路由器端口号
            RequestBean source = new RequestBean();
            RequestItem itemSource = new RequestItem("ip", socketAddress.getAddress().getHostAddress());
            source.getItems().add(itemSource);
            RequestBean target = new RequestBean();
            RequestItem itemTarget = new RequestItem("port", String.valueOf(socketAddress.getPort()));
            target.getItems().add(itemTarget);
            itemTarget = new RequestItem("state", String.valueOf(1));
            target.getItems().add(itemTarget);
            itemTarget = new RequestItem("isWorking", String.valueOf(1));
            target.getItems().add(itemTarget);
            // 更新记录数
            Integer result = ((Service) SpringContextUtil.getBean("BaseService")).updateByArrtribute(TableConstant.TABLE_ROUTERS, source, target);
            if (result > 0)
                log.info(new StringBuffer("路由器：").append(ctx.channel().toString()).append("更新端口成功").toString());
        }
        else {
            // 不存在该路由器
            Router newRouter = new Router();
            newRouter.setIp(socketAddress.getAddress().getHostAddress());
            newRouter.setPort(socketAddress.getPort());
            newRouter.setState((byte) 1);
            newRouter.setIsWorking((byte) 1);
            ((RouterService) SpringContextUtil.getBean("RouterService")).saveOne(newRouter);
            log.info(new StringBuffer("路由器：").append(ctx.channel().toString()).append("添加成功").toString());
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.info("服务端客户移除连接====>" + ctx.channel().remoteAddress());
        removeWorkingRouter(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("【系统异常】======>" + cause.toString());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info((new StringBuilder("NettyServerHandler::活跃 remoteAddress=")).append(ctx.channel().remoteAddress()).toString());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    // 超时处理
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent stateEvent = (IdleStateEvent) evt;
            switch (stateEvent.state()) {
                //读空闲（服务器端）
                case READER_IDLE:
                    log.info("【" + ctx.channel().remoteAddress() + "】读空闲（服务器端）");
                    channelInactive(ctx);
                    Thread.sleep(1000L);
                    break;
                //写空闲（客户端）
                case WRITER_IDLE:
                    log.info("【" + ctx.channel().remoteAddress() + "】写空闲（客户端）");
                    break;
                case ALL_IDLE:
                    log.info("【" + ctx.channel().remoteAddress() + "】读写空闲");
                    break;
                default:
                    break;
            }
        }
    }

    // 接受消息
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        byte[] req = new byte[in.readableBytes()];
        in.readBytes(req);
        if(req.length<3)  return;
        byte[] header = new byte[2];
        header[0] = req[8];
        header[1] = req[9];
        String handlerName = "handler" + header[0] + "" + header[1];
        String key = ctx.channel().id().toString()+"-"+req[11]+req[12];
        // ACK
        if (promiseMap.size()>0 && CommandConstant.ACK.equals(CommandCategory.getCommandCategory(header))) {
            SpringContextUtil.printBytes("key = "+key+" 接收ACK消息",req);
            this.dataMap.put(key,"成功");
            this.promiseMap.get(key).setSuccess();
        }
        // NACK
        else if (promiseMap.size()>0 && CommandConstant.NACK.equals(CommandCategory.getCommandCategory(header))) {
            SpringContextUtil.printBytes("key = "+key+" 接收NACK消息",req);
            this.dataMap.put(key,"失败");
            this.promiseMap.get(key).setSuccess();
        }
        // 通讯超时
        else if (promiseMap.size()>0 && CommandConstant.OVERTIME.equals(CommandCategory.getCommandCategory(header))) {
            SpringContextUtil.printBytes("key = "+key+" 接收通讯超时消息",req);
            this.dataMap.put(key,"通讯超时");
            this.promiseMap.get(key).setSuccess();
        }
        // tag巡检应答包
        else if(CommandConstant.TAGRESPONSE.equals(CommandCategory.getCommandCategory(header))){
            ((AsyncTask) SpringContextUtil.getBean("AsyncTask")).execute(handlerName,ctx.channel(), header,ByteUtil.splitByte(req,11,req[10]));
            key = ctx.channel().id().toString()+"-"+req[8]+req[9];
            SpringContextUtil.printBytes("key = "+key+" 接收tag巡检应答包",req);
            this.dataMap.put(key,"成功");
            this.promiseMap.get(key).setSuccess();
        }
        // router巡检应答包
        else if(CommandConstant.ROUTERRESPONSE.equals(CommandCategory.getCommandCategory(header))){
            ((AsyncTask) SpringContextUtil.getBean("AsyncTask")).execute("handler23",ctx.channel(), header,ByteUtil.splitByte(req,11,req[10]));
            key = ctx.channel().id().toString()+"-"+req[8]+req[9];
            SpringContextUtil.printBytes("key = "+key+" 接收router巡检应答包",req);
            this.dataMap.put(key,"成功");
            this.promiseMap.get(key).setSuccess();
        }
        // 路由器注册命令
        else if(CommandConstant.ROUTERREGISTY.equals(CommandCategory.getCommandCategory(header))){
            SpringContextUtil.printBytes("key = "+key+" 接收路由器注册消息",req);
            ((AsyncTask) SpringContextUtil.getBean("AsyncTask")).execute(handlerName,ctx.channel(), header,ByteUtil.splitByte(req,11,req[10]));
        }
        // 标签注册命令
        else if(CommandConstant.TAGREGISTY.equals(CommandCategory.getCommandCategory(header))){
            SpringContextUtil.printBytes("key = "+key+" 接收标签注册消息",req);
            ((AsyncTask) SpringContextUtil.getBean("AsyncTask")).execute(handlerName,ctx.channel(), header,ByteUtil.splitByte(req,11,req[10]));
        }
        else{
            SpringContextUtil.printBytes("key = "+key+" 接收到其他消息",req);
        }
    }

    // 主动发送命令
    public ChannelPromise sendMessage(Channel channel, byte[] message) {
        if (channel == null)
            throw new IllegalStateException();
        SpringContextUtil.printBytes("key = "+channel.id().toString()+"-"+message[8]+message[9]+" 主动发送命令包：",message);
        ChannelPromise promise = channel.writeAndFlush(Unpooled.wrappedBuffer(message)).channel().newPromise();
        promiseMap.put(channel.id().toString()+"-"+message[8]+message[9],promise);
        return promise;
    }
    public String getData(Channel channel, byte[] message) {
        return dataMap.get(channel.id().toString()+"-"+message[8]+message[9]);
    }
    public void removeMapWithKey(Channel channel, byte[] message) {
        String key;
        key = channel.id().toString()+"-"+message[8]+message[9];
        dataMap.remove(key);
        promiseMap.remove(key);
    }
    public String getMapSize(){
        return "dataMap--"+dataMap.size()+" "+dataMap.toString()+"  promiseMap--"+promiseMap.size()+" "+promiseMap.toString();
    }
    public void removeWorkingRouter(Channel channel){
        InetSocketAddress socketAddress = (InetSocketAddress)channel.remoteAddress();
        RequestBean source = new RequestBean();
        RequestItem itemSource = new RequestItem("ip", socketAddress.getAddress().getHostAddress());
        source.getItems().add(itemSource);
        RequestBean target = new RequestBean();
        RequestItem itemTarget = new RequestItem("isWorking", String.valueOf(0));
        target.getItems().add(itemTarget);
        // 更新记录数
        ((Service) SpringContextUtil.getBean("BaseService")).updateByArrtribute(TableConstant.TABLE_ROUTERS, source, target);
    }
}
package com.datagroup.ESLS.netty.handler;

import com.datagroup.ESLS.entity.Router;
import com.datagroup.ESLS.netty.command.CommandCategory;
import com.datagroup.ESLS.netty.command.CommandConstant;
import com.datagroup.ESLS.service.RouterService;
import com.datagroup.ESLS.utils.ByteUtil;
import com.datagroup.ESLS.utils.SocketChannelHelper;
import com.datagroup.ESLS.utils.SpringContextUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component("handler23")
@Slf4j
public class Handler23 implements ServiceHandler {
    @Override
    public byte[] executeRequest(byte[] header, byte[] message, Channel channel) {
        log.info("路由器注册（更新）-----处理器执行！");
        try {
            SpringContextUtil.printBytes("接受注册路由器消息包",message);
            String routerMac = ByteUtil.getMergeMessage(ByteUtil.splitByte(message, 0, 6));
            String routerIP = ByteUtil.getIpMessage(ByteUtil.splitByte(message, 6, 4));
            String routerBarCode = ByteUtil.getDigitalMessage(ByteUtil.splitByte(message, 10, 12));
            String routerChannelId = ByteUtil.getRealMessage(ByteUtil.splitByte(message, 22, 1));
            String routerFrequency = ByteUtil.getRealMessage(ByteUtil.splitByte(message, 23, 2));
            String routerHardVersion = ByteUtil.getVersionMessage(ByteUtil.splitByte(message, 25, 6));
            String routerSoftVersion = ByteUtil.getVersionMessage(ByteUtil.splitByte(message, 31, 6));
            SocketChannelHelper.channelIdGroup.put(routerBarCode, channel);
            System.out.println("mac:"+routerMac);
            System.out.println("routerIP:"+routerIP);
            System.out.println("routerBarCode:"+routerBarCode);
            System.out.println("routerChannelId:"+routerChannelId);
            System.out.println("routerFrequency:"+routerFrequency);
            System.out.println("routerHardVersion:"+routerHardVersion);
            System.out.println("routerSoftVersion:"+routerSoftVersion);
            RouterService routerService = ((RouterService)SpringContextUtil.getBean("RouterService"));
            Router router = routerService.findByIp(routerIP);
            // 为空则新增，否则更新
            Router r = router==null?new Router():router;
            r.setMac(routerMac);
            r.setIp(routerIP);
            r.setBarCode(routerBarCode);
            r.setChannelId(routerChannelId);
            r.setSoftVersion(routerSoftVersion);
            r.setHardVersion(routerHardVersion);
            r.setFrequency(routerFrequency);
            r.setIsWorking((byte) 1);
            r.setState((byte) 1);
            r.setCompleteTime(new Timestamp(System.currentTimeMillis()));
            routerService.saveOne(r);
        }
        catch (Exception e){
            System.out.println(e);
        }
        return CommandCategory.getResponse(CommandConstant.ACK,header,CommandConstant.COMMANDTYPE_ROUTER,null);
    }
}

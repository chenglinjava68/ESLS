package com.datagroup.ESLS.netty.handler;

import com.datagroup.ESLS.entity.Router;
import com.datagroup.ESLS.netty.command.CommandCategory;
import com.datagroup.ESLS.netty.command.CommandConstant;
import com.datagroup.ESLS.service.RouterService;
import com.datagroup.ESLS.utils.ByteUtil;
import com.datagroup.ESLS.utils.SpringContextUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component("Handler918")
@Slf4j
public class Handler918 implements ServiceHandler{
    @Override
    public byte[] executeRequest(byte[] header, byte[] message, Channel channel) {
        log.info("AP信息读取（应答包）-----处理器执行！");
        String barCode = ByteUtil.getDigitalMessage(ByteUtil.splitByte(message, 0, 12));
        String channelId = ByteUtil.getRealMessage(ByteUtil.splitByte(message, 12, 1));
        String routerHardVersion = ByteUtil.getVersionMessage(ByteUtil.splitByte(message, 13, 6));
        String routerSoftVersion = ByteUtil.getVersionMessage(ByteUtil.splitByte(message, 19, 6));
        System.out.println("条码："+barCode);
        System.out.println("channelId："+channelId);
        System.out.println("routerHardVersion："+routerHardVersion);
        System.out.println("routerSoftVersion："+routerSoftVersion);
        RouterService routerService = ((RouterService) SpringContextUtil.getBean("RouterService"));
        Router router = routerService.findByBarCode(barCode);
        // 为空则新增，否则更新
        Router r = router==null?new Router():router;
        r.setChannelId(channelId);
        r.setHardVersion(routerHardVersion);
        r.setSoftVersion(routerSoftVersion);
        routerService.saveOne(r);
        r.setCompleteTime(new Timestamp(System.currentTimeMillis()));
        routerService.saveOne(r);
        return CommandCategory.getResponse(CommandConstant.ACK,header,CommandConstant.COMMANDTYPE_ROUTER,null);
    }
}

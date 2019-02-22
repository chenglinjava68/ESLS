package com.datagroup.ESLS.netty.handler;

import com.datagroup.ESLS.entity.Router;
import com.datagroup.ESLS.entity.Style;
import com.datagroup.ESLS.entity.Tag;
import com.datagroup.ESLS.netty.command.CommandCategory;
import com.datagroup.ESLS.netty.command.CommandConstant;
import com.datagroup.ESLS.service.RouterService;
import com.datagroup.ESLS.service.StyleService;
import com.datagroup.ESLS.service.TagService;
import com.datagroup.ESLS.utils.ByteUtil;
import com.datagroup.ESLS.utils.SpringContextUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.swing.*;
import java.net.InetSocketAddress;
import java.sql.Timestamp;

@Component("handler21")
@Slf4j
public class Handler21 implements ServiceHandler{

    @Override
    @Transactional(rollbackFor = Exception.class)
    public byte[] executeRequest(byte[] header, byte[] message,Channel channel){
        log.info("标签注册-----处理器执行！");
        String barCode = null;
        try {
            SpringContextUtil.printBytes("接受标签注册消息包",message);
            barCode = ByteUtil.getDigitalMessage(ByteUtil.splitByte(message, 0, 12));
            String styleNumber = ByteUtil.getDigitalMessage(ByteUtil.splitByte(message, 12, 4));
            String resolutionWidth = ByteUtil.getRealMessage(ByteUtil.splitByte(message, 17, 2));
            String resolutionHeight = ByteUtil.getRealMessage(ByteUtil.splitByte(message, 19, 2));
            String screenType = ByteUtil.getRealMessage(ByteUtil.splitByte(message, 21, 1));
            String power = ByteUtil.getRealMessage(ByteUtil.splitByte(message, 22, 1));
            String tag_rssi = ByteUtil.getRealMessage(ByteUtil.splitByte(message, 23, 1));
            String ap_rssi = ByteUtil.getRealMessage(ByteUtil.splitByte(message, 24, 1));
            String hardversion = ByteUtil.getVersionMessage(ByteUtil.splitByte(message, 25, 6));
            String softversion = ByteUtil.getVersionMessage(ByteUtil.splitByte(message, 31, 6));
            System.out.println("条码："+barCode);
            System.out.println("样式数字："+styleNumber);
            System.out.println("分辨率宽（长边）："+resolutionWidth);
            System.out.println("分辨率高（短边）："+resolutionHeight);
            // 2：黑白屏 ； 3：三色屏
            System.out.println("屏幕类型："+screenType);
            System.out.println("电量："+power);
            System.out.println("tagrssi："+tag_rssi);
            System.out.println("aprssi："+ap_rssi);
            System.out.println("hardversion："+hardversion);
            System.out.println("softversion："+softversion);
            TagService tagService = ((TagService)SpringContextUtil.getBean("TagService"));
            String tagAddress = ByteUtil.getMergeMessage(SpringContextUtil.getAddressByBarCode(barCode));
            System.out.println("标签地址："+tagAddress);
            Tag tagByTagAddress = tagService.findByTagAddress(tagAddress);
            Tag tag = tagByTagAddress==null?new Tag():tagByTagAddress;
            tag.setTagAddress(tagAddress);
            tag.setBarCode(barCode);
            tag.setPower(power+"%");
            tag.setTagRssi(tag_rssi);
            tag.setApRssi(ap_rssi);
            tag.setHardwareVersion(hardversion);
            tag.setSoftwareVersion(softversion);
            // 不等待变价
            tag.setWaitUpdate(0);
            // 1启用
            tag.setForbidState(1);
            // 没有绑定
            tag.setState((byte) 0);
            // 已经工作
            tag.setIsWorking((byte) 1);
            tag.setScreenType(screenType);
            tag.setResolutionWidth(resolutionWidth);
            tag.setResolutionHeight(resolutionHeight);
            tag.setCompleteTime(new Timestamp(System.currentTimeMillis()));
            // 绑定路由器
            Router router = SpringContextUtil.getRouterByChannel(channel);
            tag.setRouter(router);
            // 找到标签对应的样式
            StyleService styleService = ((StyleService)SpringContextUtil.getBean("StyleService"));
            Style style = styleService.findByStyleNumber(styleNumber);
            tag.setStyle(style);
            Tag save = tagService.saveOne(tag);
            if(save==null)
                return CommandCategory.getResponse(CommandConstant.NACK,header,CommandConstant.COMMANDTYPE_TAG,SpringContextUtil.getAddressByBarCode(barCode));
        }
        catch (Exception e){
            System.out.println(e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return CommandCategory.getResponse(CommandConstant.NACK,header,CommandConstant.COMMANDTYPE_TAG,SpringContextUtil.getAddressByBarCode(barCode));
        }
        return CommandCategory.getResponse(CommandConstant.ACK,header,CommandConstant.COMMANDTYPE_TAG,SpringContextUtil.getAddressByBarCode(barCode));
    }

}

package com.datagroup.ESLS.netty.handler;

import com.datagroup.ESLS.entity.Balance;
import com.datagroup.ESLS.entity.Tag;
import com.datagroup.ESLS.netty.command.CommandCategory;
import com.datagroup.ESLS.netty.command.CommandConstant;
import com.datagroup.ESLS.service.BalanceService;
import com.datagroup.ESLS.service.TagService;
import com.datagroup.ESLS.utils.ByteUtil;
import com.datagroup.ESLS.utils.SpringContextUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component("Handler81")
@Slf4j
public class Handler81 implements ServiceHandler{
    @Override
    public byte[] executeRequest(byte[] header, byte[] message, Channel channel) {
        log.info("获取电子秤计量数据（应答包）-----处理器执行！");
        String weight = ByteUtil.getDigitalMessage(ByteUtil.splitByte(message, 0, 4));
        String weightTips = ByteUtil.getWeightTipsMessage(ByteUtil.splitByte(message, 4, 1));
        String powerInteger = ByteUtil.getRealMessage(ByteUtil.splitByte(message, 5, 1));
        String powerDecimal = ByteUtil.getRealMessage(ByteUtil.splitByte(message, 6, 1));
        String tagBarcode = ByteUtil.getDigitalMessage(ByteUtil.splitByte(message, 7, 12));
        System.out.println("weight:"+weight);
        System.out.println("weightTips:"+weightTips);
        System.out.println("powerInteger:"+powerInteger);
        System.out.println("powerDecimal:"+powerDecimal);
        System.out.println("tagBarcode:"+tagBarcode);
        TagService tagService = ((TagService)SpringContextUtil.getBean("TagService"));
        String tagAddress = ByteUtil.getMergeMessage(SpringContextUtil.getAddressByBarCode(tagBarcode));
        System.out.println("标签地址："+tagAddress);
        Tag tag = tagService.findByTagAddress(tagAddress);
        BalanceService balanceService = ((BalanceService) SpringContextUtil.getBean("BalanceService"));
        Balance balance = new Balance();
        balance.setWeight(weight);
        balance.setPowerInterger(powerInteger);
        balance.setPowerDecimal(powerDecimal);
        // 00000000
        System.out.println(weightTips.substring(6,7));
        balance.setSteady(Byte.parseByte(weightTips.substring(6,7)));
        balance.setFlay(Byte.parseByte(weightTips.substring(5,6)));
        balance.setZero(Byte.parseByte(weightTips.substring(4,5)));
        balance.setOverWeight(Byte.parseByte(weightTips.substring(3,4)));
        balance.setNetWeight(Byte.parseByte(weightTips.substring(2,3)));
        balance.setTag(tag);
        balanceService.saveOne(balance);
        return CommandCategory.getResponse(CommandConstant.ACK,header,CommandConstant.COMMANDTYPE_ROUTER,null);
    }
}

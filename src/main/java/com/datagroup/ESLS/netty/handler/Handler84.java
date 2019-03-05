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

@Component("Handler84")
@Slf4j
public class Handler84 implements ServiceHandler{
    @Override
    public byte[] executeRequest(byte[] header, byte[] message, Channel channel) {
        log.info("获取电子秤电量（应答包）-----处理器执行！");
        String powerInteger = ByteUtil.getRealMessage(ByteUtil.splitByte(message, 0, 1));
        String powerDecimal = ByteUtil.getRealMessage(ByteUtil.splitByte(message, 1, 1));
        String tagBarcode = ByteUtil.getDigitalMessage(ByteUtil.splitByte(message, 2, 12));
        System.out.println("powerInteger:"+powerInteger);
        System.out.println("powerDecimal:"+powerDecimal);
        TagService tagService = ((TagService)SpringContextUtil.getBean("TagService"));
        String tagAddress = ByteUtil.getMergeMessage(SpringContextUtil.getAddressByBarCode(tagBarcode));
        System.out.println("标签地址："+tagAddress);
        Tag tag = tagService.findByTagAddress(tagAddress);
        BalanceService balanceService = ((BalanceService) SpringContextUtil.getBean("BalanceService"));
        Balance balance = new Balance();
        balance.setPowerInterger(powerInteger);
        balance.setPowerDecimal(powerDecimal);
        balance.setTag(tag);
        balanceService.saveOne(balance);
        return CommandCategory.getResponse(CommandConstant.ACK,header,CommandConstant.COMMANDTYPE_ROUTER,null);
    }
}

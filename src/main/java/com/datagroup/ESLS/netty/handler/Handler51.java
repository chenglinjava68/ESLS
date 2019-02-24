package com.datagroup.ESLS.netty.handler;

        import com.datagroup.ESLS.entity.Tag;
        import com.datagroup.ESLS.netty.command.CommandCategory;
        import com.datagroup.ESLS.netty.command.CommandConstant;
        import com.datagroup.ESLS.service.TagService;
        import com.datagroup.ESLS.utils.ByteUtil;
        import com.datagroup.ESLS.utils.SpringContextUtil;
        import io.netty.channel.Channel;
        import lombok.extern.slf4j.Slf4j;
        import org.springframework.stereotype.Component;

        import java.util.List;

@Component("handler51")
@Slf4j
public class Handler51 implements ServiceHandler{


    @Override
    public byte[] executeRequest(byte[] header, byte[] message, Channel channel) {
        log.info("标签巡检（应答包）-----处理器执行！");
        String barCode = ByteUtil.getDigitalMessage(ByteUtil.splitByte(message, 0, 12));
        String power = ByteUtil.getRealMessage(ByteUtil.splitByte(message, 12, 1));
        String tag_rssi = String.valueOf(Integer.valueOf(ByteUtil.getRealMessage(ByteUtil.splitByte(message, 13, 1)))-256);
        String ap_rssi = String.valueOf(Integer.valueOf(ByteUtil.getRealMessage(ByteUtil.splitByte(message, 14, 1)))-256);
        byte[] state = ByteUtil.splitByte(message, 15, 1);
        System.out.println("条码："+barCode);
        System.out.println("电量："+power);
        System.out.println("tagrssi："+tag_rssi);
        System.out.println("aprssi："+ap_rssi);
        System.out.println("state："+state[0]);
        TagService tagService = ((TagService)SpringContextUtil.getBean("TagService"));
        Tag tag  = tagService.findByBarCode(barCode);
        tag = tag==null?new Tag():tag;
        if(tag!=null){
            tag.setBarCode(barCode);
            tag.setPower(power+"%");
            tag.setTagRssi(tag_rssi);
            tag.setApRssi(ap_rssi);
            if(state[0]==0) {
                tag.setGood(null);
                tag.setState(state[0]);
            }
            tag.setForbidState(1);
            tag.setIsWorking((byte) 1);
            Tag save = tagService.saveOne(tag);
            if(save==null)
                return CommandCategory.getResponse(CommandConstant.NACK,header,CommandConstant.COMMANDTYPE_ROUTER,null);
        }
        return CommandCategory.getResponse(CommandConstant.ACK,header,CommandConstant.COMMANDTYPE_ROUTER,null);
    }
}

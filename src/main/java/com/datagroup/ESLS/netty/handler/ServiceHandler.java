package com.datagroup.ESLS.netty.handler;

import com.datagroup.ESLS.entity.Router;
import io.netty.channel.Channel;

public interface ServiceHandler {
    byte[] executeRequest(byte[] header, byte[] message, Channel channel);
}

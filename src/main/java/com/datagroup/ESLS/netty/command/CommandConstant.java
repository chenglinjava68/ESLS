package com.datagroup.ESLS.netty.command;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
@Component
public class CommandConstant {
    // 命令常量
    public static String ACK = "ACK";
    public static String NACK = "NACK";
    public static String OVERTIME = "OVERTIME";
    public static String TAGRESPONSE = "TAGRESPONSE";
    public static String ROUTERRESPONSE = "ROUTERRESPONSE";
    // 命令内容常量
    // 路由注册
    public static String ROUTERREGISTY = "ROUTERREGISTY";
    public static String TAGREGISTY = "TAGREGISTY";
    public static String MAC = "mac";
    // 路由器测试
    public static String APREAD = "APREAD";
    // 电子秤
    public static String BALANCEDATA = "BALANCEDATA";
    public static String BALANCEPOWER = "BALANCEPOWER";
    // 命令常量
    // 0对标签 1对路由器
    public static Integer COMMANDTYPE_TAG = 0;
    public static Integer COMMANDTYPE_ROUTER = 1;
    public static Integer COMMANDTYPE_TAG_BROADCAST = 2;

    public static Map<String,byte[]> COMMAND_BYTE = null;
    public static String TAGBIND = "标签绑定";
    public static String TAGBINDOVER = "标签取消绑定";
    public static String TAGBLING = "标签LED闪烁";
    public static String TAGBLINGOVER = "标签结束LED闪烁";
    public static String FLUSH = "墨水屏刷新";
    public static String QUERYTAG = "查询标签信息";
    public static String QUERYROUTER = "查询路由器信息";
    public static String SETTINGROUTER = "设置路由器信息";
    public static String TAGREMOVE = "标签移除";
    public static String ROUTERAWAKEOVER = "路由器结束唤醒";

    @PostConstruct
    public static void init(){
        COMMAND_BYTE = new HashMap<>();
        // TAG命令
        // 通知标签绑定
        COMMAND_BYTE.put(TAGBIND, getBytes(0x04,0x01));
        // 通知标签解绑
        COMMAND_BYTE.put(TAGBINDOVER, getBytes(0x04,0x02));
        // 通知标签LED闪烁
        COMMAND_BYTE.put(TAGBLING, getBytes(0x04,0x03));
        // 通知标签LED停止闪烁
        COMMAND_BYTE.put(TAGBLINGOVER, getBytes(0x04,0x04));
        // 通知墨水瓶刷新
        COMMAND_BYTE.put(FLUSH, getBytes(0x04,0x05));
        // 标签巡检
        COMMAND_BYTE.put(QUERYTAG, getBytes(0x05,0x01));
        // 标签移除命令
        COMMAND_BYTE.put(TAGREMOVE, getBytes(0x02,0x02));

        //Router命令（3个注册 查询 设置）
        // 路由器巡检
        COMMAND_BYTE.put(QUERYROUTER, getBytes(0x05,0x02));
        // 路由器设置
        COMMAND_BYTE.put(SETTINGROUTER, getBytes(0x02,0x05,COMMANDTYPE_ROUTER));
        // 路由器结束唤醒
        COMMAND_BYTE.put(ROUTERAWAKEOVER, getBytes(0x04,0x07,COMMANDTYPE_ROUTER));
    }
    private static byte[] getBytes(int _0,int _1,int type){
        byte[] bytes = new byte[11];
        //100010 00100010
        if(type ==COMMANDTYPE_TAG) {
            bytes[0] = 0x22;
            bytes[1] = 0x22;
        }
        else if(type == COMMANDTYPE_ROUTER){
            // 临时修改
            bytes[0] = 0x11;
            bytes[1] = 0x11;
        }
        bytes[2] = 0;
        bytes[3]= 7;
        bytes[4] = (byte) 0xff;
        bytes[5] = (byte) 0xff;
        bytes[6] = (byte) 0xff;
        bytes[7] = (byte) 0xff;
        bytes[8] = (byte)_0;
        bytes[9] = (byte)_1;
        bytes[10] = 0;
        return bytes;
    }
    private static byte[] getBytes(int _0,int _1){
        byte[] bytes = new byte[3];
        bytes[0] = (byte)_0;
        bytes[1] = (byte)_1;
        bytes[2] = 0;
        return bytes;
    }
    public static byte[] getBytesByType(byte[] address,byte[] message,int type){
        byte[] bytes = new byte[8+message.length];
        if(type == COMMANDTYPE_TAG){
            // int length = address.length + message.length;
            int length = 4 + message.length;
            // 通讯对象
            bytes[0] = 0x22;
            bytes[1] = 0x22;
            // 长度
            bytes[2] = (byte)(length >> 8) ;
            bytes[3] = (byte)(length >> 0) ;
            // 标签地址
            for (int i = 0; i < address.length; i++)
                bytes[i + 4] = address[i];
            //数据段
            for(int i = 0;i<message.length;i++)
                bytes[i+8] = message[i];

        }
        else if(type == COMMANDTYPE_ROUTER){
            // 通讯对象
            bytes[0] = 0x11;
            bytes[1] = 0x11;
            // 长度
            int length = 4 + message.length;
            bytes[2] = (byte)(length >> 8) ;
            bytes[3] = (byte)(length >> 0) ;
            // 地址
            bytes[4] = (byte)0xff;
            bytes[5] = (byte)0xff;
            bytes[6] = (byte)0xff;
            bytes[7] = (byte)0xff;
            //数据段
            for(int i = 0;i<message.length;i++)
                bytes[i+8] = message[i];
        }
        else if(type == COMMANDTYPE_TAG_BROADCAST){
            // 通讯对象
            bytes[0] = 0x22;
            bytes[1] = 0x22;
            // 长度
            int length = 4 + message.length;
            bytes[2] = (byte)(length >> 8) ;
            bytes[3] = (byte)(length >> 0) ;
            // 地址
            bytes[4] = 0;
            bytes[5] = 0;
            bytes[6] = 0;
            bytes[7] = 0;
            //数据段
            for(int i = 0;i<message.length;i++)
                bytes[i+8] = message[i];
        }
        return bytes;
    }
}

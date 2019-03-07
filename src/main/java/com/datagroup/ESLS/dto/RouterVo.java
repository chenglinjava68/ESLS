package com.datagroup.ESLS.dto;

import lombok.Data;
import lombok.ToString;

import java.sql.Timestamp;

@Data
@ToString
public class RouterVo {
    private long id;
    private String mac;
    private String ip;
    private Integer port;
    private String channelId;
    private Byte state;
    private String softVersion;
    private String frequency;
    private String hardVersion;
    private Integer execTime;
    private String barCode;
    private Byte isWorking;
    private Timestamp completeTime;

    private long shopId;
    private byte type;
    private String number;
    private String fatherShop;
    private String name;
    private String manager;
    private String address;
    private String account;
    private String password;
    private String phone;
}

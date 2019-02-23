package com.datagroup.ESLS.dto;

import lombok.Data;
import lombok.ToString;

import java.sql.Timestamp;

@Data
@ToString
public class TagVo {
    private long id;
    private String power;
    private String tagRssi;
    private String apRssi;
    private Byte state;
    private String hardwareVersion;
    private String softwareVersion;
    private Integer forbidState;
    private Integer waitUpdate;
    private Integer execTime;
    private Timestamp completeTime;
    private String barCode;
    private String tagAddress;
    private String screenType;
    private String resolutionWidth;
    private String resolutionHeight;
    private Byte isWorking;

    private long goodId;
    private long styleId;
    private long routerId;
}

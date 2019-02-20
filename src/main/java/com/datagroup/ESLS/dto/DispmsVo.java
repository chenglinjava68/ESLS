package com.datagroup.ESLS.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class DispmsVo {
    private long id;
    private String name;
    private Integer x;
    private Integer y;
    private Integer width;
    private Integer height;
    private String sourceColumn;
    private String columnType;
    private Integer backgroundColor;
    private String text;
    private String startText;
    private String endText;
    private String fontBold;
    private String fontFamily;
    private Integer fontColor;
    private Integer fontSize;
    // status代表是否要显示
    private Byte status;
}

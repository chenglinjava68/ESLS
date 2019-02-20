package com.datagroup.ESLS.dto;

import lombok.Data;

@Data
public class UserVo {
    private long id;
    private String name;
    private String passwd;
    private Byte sex;
    private String telephone;
    private String address;
    private String department;
}

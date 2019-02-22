package com.datagroup.ESLS.dto;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class UserVo {
    private long id;
    private String name;
    private String passwd;
    private Byte sex;
    private String telephone;
    private String address;
    private String department;
    private Timestamp createTime;
    private Timestamp lastLoginTime;
    private Byte status;
    private long shopId;
    private String roleList;
}

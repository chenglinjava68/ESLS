package com.datagroup.ESLS.entity;

import com.datagroup.ESLS.dao.SystemVersionDao;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;

@Component
@Data
public class SystemVersionArgs {
    @Autowired
    private SystemVersionDao systemVersionDao;
    public static String softVersion;
    public static String productor;
    public static Timestamp date;

    public static String tokenAliveTime;
    public static String packageLength;
    public static String commandRepeatTime;
    public static String commandWaitingTime;
    @PostConstruct
    public void init(){
        SystemVersion  systemVersion= systemVersionDao.findById((long)1).get();
        SystemVersionArgs.softVersion = systemVersion.getSoftVersion();
        SystemVersionArgs.productor = systemVersion.getProductor();
        SystemVersionArgs.date = systemVersion.getDate();
        SystemVersionArgs.tokenAliveTime = systemVersion.getTokenAliveTime();
        SystemVersionArgs.commandRepeatTime = systemVersion.getCommandRepeatTime();
        SystemVersionArgs.packageLength = systemVersion.getPackageLength();
        SystemVersionArgs.commandWaitingTime = systemVersion.getCommandWaitingTime();
    }
}

package com.datagroup.ESLS.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Data
@Table(name = "systemversion", schema = "tags", catalog = "")
public class SystemVersion implements Serializable {
    @Id
    @Column(name = "id", nullable = false)
    private long id;
    @Column(name = "softVersion")
    private String softVersion;
    @Column(name = "productor")
    private String productor;
    @Column(name = "date")
    private Timestamp date;

    @Column(name = "tokenAliveTime")
    private String tokenAliveTime;
    @Column(name = "commandRepeatTime")
    private String commandRepeatTime;
    @Column(name = "packageLength")
    private String packageLength;
    @Column(name = "commandWaitingTime")
    private String commandWaitingTime;

}

package com.datagroup.ESLS.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@Entity
@Table(name = "routers", schema = "tags", catalog = "")
@Data
@ToString
public class Router implements Serializable {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)//自增主键
    private long id;
    @Column(name = "mac")
    private String mac;
    @Column(name = "ip")
    private String ip;
    @Column(name = "port")
    private Integer port;
    @Column(name = "channelId")
    private String channelId;
    @Column(name = "state")
    private Byte state;
    @Column(name = "softVersion")
    private String softVersion;
    @Column(name = "frequency")
    private String frequency;
    @Column(name = "hardVersion")
    private String hardVersion;
    @Column(name = "execTime")
    private Integer execTime;
    @Column(name = "barCode")
    private String barCode;
    @Column(name = "isWorking")
    private Byte isWorking;
    @Column(name = "completeTime")
    private Timestamp completeTime;
    @ManyToOne
    @JoinColumn(name = "shopid", referencedColumnName = "id")
    private Shop shop;
}

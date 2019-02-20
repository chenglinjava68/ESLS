package com.datagroup.ESLS.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "cyclejob", schema = "tags", catalog = "")
@Data
public class cyclejob {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)//自增主键
    private long id;
    @Column(name = "cron")
    private String cron;
    @Column(name = "args")
    private String args;
    @Column(name = "mode")
    private Integer mode;
    @Column(name = "type")
    private Integer type;
}

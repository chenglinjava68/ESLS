package com.datagroup.ESLS.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Data
public class Logs implements Serializable {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)//自增主键
    private Long id;
    @Column(name = "username")
    private String username;
    @Column(name = "operation")
    private String operation;
    @Column(name = "method")
    private String method;
    @Column(name = "params")
    private String params;
    @Column(name = "ip")
    private String ip;
    @Column(name = "runningTime")
    private String runningTime;
    @Column(name = "createDate")
    private Timestamp createDate;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Logs logs = (Logs) o;
        return Objects.equals(id, logs.id) &&
                Objects.equals(username, logs.username) &&
                Objects.equals(operation, logs.operation) &&
                Objects.equals(method, logs.method) &&
                Objects.equals(params, logs.params) &&
                Objects.equals(ip, logs.ip) &&
                Objects.equals(runningTime, logs.runningTime) &&
                Objects.equals(createDate, logs.createDate);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id, username, operation, method, params, ip, runningTime, createDate);
    }
}

package com.datagroup.ESLS.entity;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@Entity
@Table(name = "tags", schema = "tags", catalog = "")
@Data
public class Tag implements Serializable {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)//自增主键
    private long id;
    @Column(name = "power")
    private String power;
    @Column(name = "tagRssi")
    private String tagRssi;
    @Column(name = "apRssi")
    private String apRssi;
    @Column(name = "state")
    private Byte state;
    @Column(name = "hardwareVersion")
    private String hardwareVersion;
    @Column(name = "softwareVersion")
    private String softwareVersion;
    @Column(name = "waitUpdate")
    private Integer waitUpdate;
    @Column(name = "forbidState")
    private Integer forbidState;
    @Column(name = "execTime")
    private Integer execTime;
    @Column(name = "completeTime")
    private Timestamp completeTime;
    @Column(name = "barCode")
    private String barCode;
    @Column(name = "tagAddress")
    private String tagAddress;
    @Column(name = "screenType")
    private String screenType;
    @Column(name = "resolutionWidth")
    private String resolutionWidth;
    @Column(name = "resolutionHeight")
    private String resolutionHeight;
    @Column(name = "isWorking")
    private Byte isWorking;
    @ManyToOne
    @JoinColumn(name = "goodid", referencedColumnName = "id")
    private Good good;
    @ManyToOne
    @JoinColumn(name = "styleid", referencedColumnName = "id")
    private Style style;
    @ManyToOne
    @JoinColumn(name = "routerid", referencedColumnName = "id")
    private Router router;
    @OneToMany(mappedBy = "tag")
    @JsonIgnore
    private Collection<Balance> balances;
    public Tag() {
    }
}

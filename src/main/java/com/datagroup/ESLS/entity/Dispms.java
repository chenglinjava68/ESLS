package com.datagroup.ESLS.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@Entity
@ToString
@Data
@Table(name = "dispms", schema = "tags", catalog = "")
public class Dispms implements Serializable {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)//自增主键
    private long id;
    @Column(name = "name")
    private String name;
    @Column(name = "x")
    private Integer x;
    @Column(name = "y")
    private Integer y;
    @Column(name = "width")
    private Integer width;
    @Column(name = "height")
    private Integer height;
    @Column(name = "sourceColumn")
    private String sourceColumn;
    @Column(name = "columnType")
    private String columnType;
    @Column(name = "backgroundColor")
    private Integer backgroundColor;
    @Column(name = "text")
    private String text;
    @Column(name = "startText")
    private String startText;
    @Column(name = "endText")
    private String endText;
    @Column(name = "fontType")
    private String fontType;
    @Column(name = "fontFamily")
    private String fontFamily;
    @Column(name = "fontSize")
    private Integer fontSize;
    @Column(name = "fontColor")
    private Integer fontColor;
    @Column(name = "status")
    private Byte status;
    @Column(name = "imageUrl")
    private String imageUrl;
    @Column(name = "backup")
    private String backup;
    @Column(name = "regionId")
    private String regionId;
    @ManyToOne
    @JoinColumn(name = "styleid", referencedColumnName = "id")
    @JsonIgnore
    private Style style;

    public Dispms() {
    }
}

package com.datagroup.ESLS.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@Entity
@Data
@NoArgsConstructor
public class Role {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)//自增主键
    private long id;
    @Column(name = "type")
    private String type;
    @Column(name = "name")
    private String name;
    //角色 -- 权限关系：多对多关系;
    @ManyToMany(fetch= FetchType.EAGER)
    @JoinTable(name="role_permission",joinColumns={@JoinColumn(name="roleId")},inverseJoinColumns={@JoinColumn(name="permissionId")})
    private List<Permission> permissions;
    // 用户 - 角色关系定义;
    // 一个角色对应多个用户
    @ManyToMany
    @JoinTable(name="user_role",joinColumns={@JoinColumn(name="roleId")},inverseJoinColumns={@JoinColumn(name="userId")})
    private List<User> users;

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
    public Role(String name,String type) {
        this.name = name;
        this.type = type;
    }
}

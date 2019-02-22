package com.datagroup.ESLS.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "user_role", schema = "tags", catalog = "")
@Data
@NoArgsConstructor
public class UserRole {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)//自增主键
    private Long id;
    @Column(name = "roleId")
    private Long roleId;
    @Column(name = "userId")
    private Long userId;

    public UserRole(Long roleId, Long userId) {
        this.roleId = roleId;
        this.userId = userId;
    }
}

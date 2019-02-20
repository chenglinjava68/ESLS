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
    @Column(name = "role_id")
    private Long role_id;
    @Column(name = "user_id")
    private Long user_id;

    public UserRole(Long roleId, Long userId) {
        this.role_id = roleId;
        this.user_id = userId;
    }
}

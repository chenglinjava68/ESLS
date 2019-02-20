package com.datagroup.ESLS.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "role_permission", schema = "tags", catalog = "")
@Data
@NoArgsConstructor
public class RolePermission {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)//自增主键
    private Long id;
    @Column(name = "permisson_id")
    private Long permisson_id;
    @Column(name = "role_id")
    private Long role_id;

    public RolePermission(Long permissonId, Long roleId) {
        this.permisson_id = permissonId;
        this.role_id = roleId;
    }
}

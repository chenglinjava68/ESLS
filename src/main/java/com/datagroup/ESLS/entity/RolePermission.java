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
    @Column(name = "permissionId")
    private Long permissionId;
    @Column(name = "roleId")
    private Long roleId;
    public RolePermission(Long permission_id, Long roleId) {
        this.permissionId = permissionId;
        this.roleId = roleId;
    }
}

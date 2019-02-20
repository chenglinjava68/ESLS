package com.datagroup.ESLS.dao;

import com.datagroup.ESLS.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserAndRoleDao extends JpaRepository<UserRole,Long> {
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO user_role(role_id,user_id) SELECT :role_id , :user_id FROM DUAL WHERE NOT EXISTS(SELECT * FROM user_role b WHERE b.role_id=:role_id AND b.user_id=:user_id)",nativeQuery = true)
    Integer insertByCondition(@Param("role_id") Long role_id,@Param("user_id") Long user_id);
}

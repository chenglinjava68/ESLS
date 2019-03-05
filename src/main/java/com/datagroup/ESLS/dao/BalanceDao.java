package com.datagroup.ESLS.dao;

import com.datagroup.ESLS.entity.Balance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BalanceDao extends JpaRepository<Balance,Long> {
}

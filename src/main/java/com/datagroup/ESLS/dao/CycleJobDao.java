package com.datagroup.ESLS.dao;

import com.datagroup.ESLS.entity.Dispms;
import com.datagroup.ESLS.entity.cyclejob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CycleJobDao extends JpaRepository<cyclejob,Long> {
}

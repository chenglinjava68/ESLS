package com.datagroup.ESLS.dao;

import com.datagroup.ESLS.entity.CycleJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CycleJobDao extends JpaRepository<CycleJob,Long> {
    CycleJob findByMode(Integer mode);
}

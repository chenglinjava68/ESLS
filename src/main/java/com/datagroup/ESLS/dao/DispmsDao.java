package com.datagroup.ESLS.dao;

import com.datagroup.ESLS.entity.Dispms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DispmsDao extends JpaRepository<Dispms,Long> {
    List<Dispms> findByStyleId(Long StyleId);
}

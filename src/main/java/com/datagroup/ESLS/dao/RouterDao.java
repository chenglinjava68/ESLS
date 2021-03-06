package com.datagroup.ESLS.dao;

import com.datagroup.ESLS.entity.Router;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouterDao extends JpaRepository<Router,Long> {
    Router findByIp(String ip);
    Router findByBarCode(String barCode);
}

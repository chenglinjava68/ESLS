package com.datagroup.ESLS.dao;

import com.datagroup.ESLS.entity.Style;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StyleDao extends JpaRepository<Style,Long> {
    Style findByStyleNumber(String styleNumber);
    List<Style> findByWidth(Integer width);
}

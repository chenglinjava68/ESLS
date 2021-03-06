package com.datagroup.ESLS.dao;

import com.datagroup.ESLS.entity.Style;
import com.datagroup.ESLS.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagDao extends JpaRepository<Tag,Long> {
    Tag findByBarCode(String barCodd);
    List<Tag> findByRouterId(Long routerId);
    List<Tag> findByGoodId(Long goodId);
    Tag findByTagAddress(String tagAddress);
    List<Tag> findByStyleId(Long styleId);
}

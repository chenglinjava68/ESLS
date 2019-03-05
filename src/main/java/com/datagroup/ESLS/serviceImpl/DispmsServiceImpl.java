package com.datagroup.ESLS.serviceImpl;

import com.datagroup.ESLS.common.constant.ArrtributeConstant;
import com.datagroup.ESLS.common.constant.TableConstant;
import com.datagroup.ESLS.dao.DispmsDao;
import com.datagroup.ESLS.entity.Dispms;
import com.datagroup.ESLS.entity.Style;
import com.datagroup.ESLS.entity.Tag;
import com.datagroup.ESLS.service.DispmsService;
import com.datagroup.ESLS.utils.SendCommandUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("DispmsService")
public class DispmsServiceImpl extends BaseServiceImpl  implements DispmsService {
    @Autowired
    private DispmsDao dispmsDao;
    @Override
    public List<Dispms> findAll() {
        return dispmsDao.findAll();
    }

    @Override
    public List<Dispms> findAll(Integer page, Integer count) {
        List<Dispms> content = dispmsDao.findAll(PageRequest.of(page, count, Sort.Direction.DESC, "id")).getContent();
        return content;
    }
    @Override
    public Dispms saveOne(Dispms dispms) {
//        Style style = dispms.getStyle();
//        if(dispms.getId()!=0 && style!=null) {
//            Long styleId = style.getId();
//            // 通过styleId查找使用了此样式的所有标签实体
//            try {
//                List<Tag> tags = findByArrtribute(TableConstant.TABLE_TAGS, ArrtributeConstant.TAG_STYLEID, String.valueOf(styleId), com.datagroup.ESLS.entity.Tag.class);
//                // 通过标签实体的路由器IP地址发送更改标签内容包
//                SendCommandUtil.updateTagStyle(tags);
//            }
//            catch (Exception e){
//                System.out.println("DispmsServiceImpl - saveOne : "+e);
//            }
//        }
        return dispmsDao.save(dispms);
    }

    @Override
    public Optional<Dispms> findById(Long id) {
        return dispmsDao.findById(id);
    }

    @Override
    public boolean deleteById(Long id) {
        try{
            dispmsDao.deleteById(id);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    @Override
    public Dispms findByStyleIdAndColumnTypeAndSourceColumn(Long styleId, String columnType, String sourceColumn) {
        return dispmsDao.findByStyleIdAndColumnTypeAndSourceColumn(styleId,columnType,sourceColumn);
    }
}

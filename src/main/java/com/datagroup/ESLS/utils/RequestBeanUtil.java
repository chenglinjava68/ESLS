package com.datagroup.ESLS.utils;

import com.datagroup.ESLS.common.constant.TableConstant;
import com.datagroup.ESLS.common.request.RequestBean;
import com.datagroup.ESLS.common.request.RequestItem;
import com.datagroup.ESLS.entity.*;
import com.datagroup.ESLS.service.Service;

import java.util.ArrayList;
import java.util.List;

public class RequestBeanUtil {
    public static String getRequestBeanAsString(RequestBean requestBean){
        StringBuffer sb = new StringBuffer();
        for(RequestItem item:requestBean.getItems()){
            sb.append(item.getCron()+" "+item.getQuery()+" "+item.getQueryString());
            sb.append("-");
        }
        return sb.toString();
    }
    public static RequestBean stringtoRequestBean(String str){
        RequestBean requestBean = new RequestBean();
        List<RequestItem> items = requestBean.getItems();
        String sb[] = str.split("-");
        for(String s : sb){
            String[] s1 = s.split(" ");
            RequestItem requestItem = new RequestItem(s1[0], s1[1],s1[2]);
            items.add(requestItem);
        }
        return requestBean;
    }
    public static List<Tag> getTagsByRequestBean(RequestBean requestBean){
        List tags = new ArrayList();
        Service service  = (Service)SpringContextUtil.getBean("BaseService");
        for (RequestItem items : requestBean.getItems()){
            List<Tag> tagItem = service.findByArrtribute(TableConstant.TABLE_TAGS, items.getQuery(), items.getQueryString(), Tag.class);
            for(Tag tag:tagItem)
                // 标签启用
                if(tag.getForbidState()!=0)
                    tags.add(tag);
        }
        return tags;
    }
    public static List<Router> getRoutersByRequestBean(RequestBean requestBean){
        List routers = new ArrayList();
        Service service  = (Service)SpringContextUtil.getBean("BaseService");
        for (RequestItem items : requestBean.getItems()){
            List<Router> routerItem = service.findByArrtribute(TableConstant.TABLE_ROUTERS, items.getQuery(), items.getQueryString(), Router.class);
            for(Router r:routerItem)
                if(r.getState()==1)
                    routers.add(r);
        }
        return routers;
    }
    public static List<Style> getStylesByRequestBean(RequestBean requestBean){
        List styles = new ArrayList();
        Service service  = (Service)SpringContextUtil.getBean("BaseService");
        for (RequestItem items : requestBean.getItems()){
            List<Style> styleItem = service.findByArrtribute(TableConstant.TABLE_STYLE, items.getQuery(), items.getQueryString(), Style.class);
            styles.addAll(styleItem);
        }
        return styles;
    }
    public static List<Dispms> getDispmsByRequestBean(RequestBean requestBean){
        List dispms = new ArrayList();
        Service service  = (Service)SpringContextUtil.getBean("BaseService");
        for (RequestItem items : requestBean.getItems()){
            List<Dispms> dispmItem = service.findByArrtribute(TableConstant.TABLE_DISPMS, items.getQuery(), items.getQueryString(), Dispms.class);
            dispms.addAll(dispmItem);
        }
        return dispms;
    }
    public static List<Good> getGoodsByRequestBean(RequestBean requestBean){
        List goods = new ArrayList();
        Service service  = (Service)SpringContextUtil.getBean("BaseService");
        for (RequestItem items : requestBean.getItems()){
            List<Good> goodItem = service.findByArrtribute(TableConstant.TABLE_GOODS, items.getQuery(), items.getQueryString(), Good.class);
            goods.addAll(goodItem);
        }
        return goods;
    }
}

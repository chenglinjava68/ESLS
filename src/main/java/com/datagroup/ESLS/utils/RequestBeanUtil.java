package com.datagroup.ESLS.utils;

import com.datagroup.ESLS.common.request.RequestBean;
import com.datagroup.ESLS.common.request.RequestItem;

import java.util.List;

public class RequestBeanUtil {
    public static String getRequestBeanAsString(RequestBean requestBean){
        StringBuffer sb = new StringBuffer();
        for(RequestItem item:requestBean.getItems()){
            sb.append(item.getQuery()+" "+item.getQueryString());
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
            RequestItem requestItem = new RequestItem(s1[0], s1[1]);
            items.add(requestItem);
        }
        return requestBean;
    }
}

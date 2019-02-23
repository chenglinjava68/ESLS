package com.datagroup.ESLS.utils;

import com.datagroup.ESLS.entity.Dispms;
import com.datagroup.ESLS.entity.Good;
import org.apache.commons.lang3.StringUtils;

public class StringUtil {
    public static String NUMBER_LEFT = "数字左侧";
    public static String NUMBER_RIGHT = "数字右侧";
    public static String NUMBER = "数字";
    public static String BACKGROUND = "背景";
    public static String PHOTO = "图片";
    public static String LINE = "线段";
    public static String Str = "字符串";
    public static String QRCODE = "二维码";
    public static String BARCODE = "条形码";
    public static boolean isEmpty(String value){
        return StringUtils.isEmpty(value)  || value.contains("null");
    }
    public static String getRealString(Dispms dispM, Good good){
        StringBuffer sb = new StringBuffer();
        if(!isEmpty(dispM.getStartText())){
            sb.append(dispM.getStartText());
        }
        // 为与商品有关字段
        if(!dispM.getSourceColumn().equals("0")){
            String text = SpringContextUtil.getSourceData(dispM.getSourceColumn(),good);
            if(dispM.getColumnType().equals(NUMBER_LEFT)) {
                String left = text.substring(0,text.indexOf(".")+1);
                sb.append(left);
            }
            else
                sb.append(text);
        }
        // 为与商品无关字段
        else if(!isEmpty(dispM.getText())){
            sb.append(dispM.getText());
        }
        if(!isEmpty(dispM.getEndText())){
            sb.append(dispM.getEndText());
        }
        return sb.toString();
    }
    public static boolean isDigitalString(String sourceColumn){
        if(sourceColumn.contains(NUMBER)  || sourceColumn.contains(NUMBER_LEFT)  || sourceColumn.contains(NUMBER_RIGHT) )
            return true;
        return false;
    }
}

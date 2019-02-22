package com.datagroup.ESLS.utils;

import com.datagroup.ESLS.entity.Dispms;
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
    public static String getRealString(Dispms dispM){
        StringBuffer sb = new StringBuffer();
        if(!isEmpty(dispM.getStartText())){
            sb.append(dispM.getStartText());
        }
        if(!isEmpty(dispM.getText())){
            String text = dispM.getText();
            if(dispM.getColumnType().equals(NUMBER_LEFT)) {
                String left = text.substring(0,text.indexOf(".")+1);
                sb.append(left);
            }
            else
                sb.append(text);
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

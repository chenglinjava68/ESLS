package com.datagroup.ESLS.utils;

import org.apache.commons.lang3.StringUtils;

public class StringUtil {

    public static boolean isEmpty(String value){
        return StringUtils.isEmpty(value)  || value.contains("null");
    }
}

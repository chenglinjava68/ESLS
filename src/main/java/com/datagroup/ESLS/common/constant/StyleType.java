package com.datagroup.ESLS.common.constant;

import java.util.HashMap;
import java.util.Map;

public class StyleType {
    public static Integer StyleType_21 = 0;
    public static Integer StyleType_25 = 1;
    public static Integer StyleType_29 = 2;
    public static Integer StyleType_40 = 3;
    public static Map<String, String> keyToWHMap = new HashMap();
    static {
        keyToWHMap.put("21","212 104");
        keyToWHMap.put("25","250 122");
        keyToWHMap.put("29","296 128");
        keyToWHMap.put("40","400 300");
    }
}

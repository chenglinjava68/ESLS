package com.datagroup.ESLS.common.constant;

public class StyleNumberToHeight {
    public static int styleNumberToHeight(String styleNumber){
        int number = Integer.parseInt(styleNumber.substring(0,2));
        // 104 212
        if(number==13){
            return 104;
        }
        else if(number==21){
            return 104;
        }
        // 128 296
        else if (number==29){
            return 128;
        }
        // 300 400
        else if (number==42){
            return 300;
        }
        return 104;
    }
}

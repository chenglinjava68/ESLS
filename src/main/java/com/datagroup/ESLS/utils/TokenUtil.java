//package com.datagroup.ESLS.utils;
//
//import com.auth0.jwt.JWT;
//import com.auth0.jwt.algorithms.Algorithm;
//import com.datagroup.ESLS.entity.User;
//
//import java.util.UUID;
//
//public class TokenUtil {
//
//    public static String getToken(User admin){
//        String token  =  "";
//        token = JWT.create().withAudience(String.valueOf(admin.getId()+ UUID.randomUUID().toString() + System.currentTimeMillis()))
//                .sign(Algorithm.HMAC256(admin.getPasswd()));
//        return token;
//    }
//}

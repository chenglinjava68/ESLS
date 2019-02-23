
package com.datagroup.ESLS.utils;

import com.datagroup.ESLS.common.constant.StyleNumberToHeight;
import com.datagroup.ESLS.common.constant.StyleType;
import com.datagroup.ESLS.common.response.ByteResponse;
import com.datagroup.ESLS.dto.ByteAndRegion;
import com.datagroup.ESLS.entity.*;
import com.datagroup.ESLS.netty.server.ServerChannelHandler;
import com.datagroup.ESLS.service.RouterService;
import io.netty.channel.Channel;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.List;

@Component
public class SpringContextUtil implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
    public static ByteResponse getRequest(List<Dispms> dispms,String styleNumber,Good good) throws IOException {
        FileUtils.deleteDirectory(new File("D:\\styles\\"+styleNumber));
        List<byte[]> allbyteList = new ArrayList<>();
        byte[] firstByte = new byte[3+6+dispms.size()*12];
        int i,j;
        firstByte[0] = 0x03;
        firstByte[1] = 0x01;
        firstByte[2] = (byte) (6+dispms.size()*12);
        for(j=0;j<styleNumber.length();j++)
            firstByte[j+3] = (byte) styleNumber.charAt(j);
        firstByte[7]  = '\0';
        firstByte[8] = (byte) dispms.size();
        for (i = 0; i < dispms.size(); i++) {
            try {
                Dispms region = dispms.get(i);
                System.out.println(region.getColumnType() + " " + region.getX() + " " + region.getY() + " " + region.getWidth() + " " + region.getHeight());
                ByteAndRegion byteAndRegion = getRegionImage(region, styleNumber,good);
                region = byteAndRegion.getRegion();
                byte[] regionImage = byteAndRegion.getRegionBytes();
                System.out.println(region.getColumnType() + " " + region.getX() + " " + region.getY() + " " + region.getWidth() + " " + region.getHeight());
                // 区域编号
                firstByte[(i * 12) + 9] = (byte) (i + 1);
                // 颜色
                firstByte[(i * 12) + 10] = ColorUtil.getColorByte(region.getBackgroundColor(), region.getFontColor());
                byte[] x, y, height, width;
                if (ImageHelper.getTypeByStyleNumber(styleNumber).equals(StyleType.StyleType_40)) {
                    width = int2ByteArr(region.getWidth());
                    height = int2ByteArr(region.getHeight());
                    x = int2ByteArr(region.getX());
                    y = int2ByteArr(region.getY());
                    // x
                    firstByte[(i * 12) + 11] = x[1];
                    firstByte[(i * 12) + 12] = x[0];
                    // y
                    firstByte[(i * 12) + 13] = y[1];
                    firstByte[(i * 12) + 14] = y[0];
                } else {
                    width = int2ByteArr(region.getWidth());
                    height = int2ByteArr(region.getHeight());
                    x = int2ByteArr(region.getX());
                    y = int2ByteArr(StyleNumberToHeight.styleNumberToHeight(styleNumber) - region.getY()-region.getHeight());
                    // x
                    firstByte[(i * 12) + 11] = y[1];
                    firstByte[(i * 12) + 12] = y[0];
                    // y
                    firstByte[(i * 12) + 13] = x[1];
                    firstByte[(i * 12) + 14] = x[0];
                }
                byte[] length = int2ByteArr(regionImage.length);
                // 长度
                firstByte[(i * 12) + 15] = width[1];
                firstByte[(i * 12) + 16] = width[0];
                // 宽度
                firstByte[(i * 12) + 17] = height[1];
                firstByte[(i * 12) + 18] = height[0];
                // 显示存储字节数
                firstByte[(i * 12) + 19] = length[1];
                firstByte[(i * 12) + 20] = length[0];
                List<byte[]> byteList = getByteList(regionImage, i + 1);
                allbyteList.addAll(byteList);
            }
            catch (Exception e){
                System.out.println("getRequest - " + e);
            }
        }
        byte[] bytes = allbyteList.get(allbyteList.size() - 1);
        bytes[4] = 0x01;
        return new ByteResponse(firstByte,allbyteList);
    }
    public static ByteResponse getRegionRequest(List<Dispms> dispms,String styleNumber,Good good,HashMap<String,Integer> regionIdMap){
        List<byte[]> allbyteList = new ArrayList<>();
        byte[] firstByte = new byte[3+6+dispms.size()*12];
        int i,j;
        firstByte[0] = 0x03;
        firstByte[1] = 0x01;
        firstByte[2] = (byte) (6+dispms.size()*12);
        for(j=0;j<styleNumber.length();j++)
            firstByte[j+3] = (byte) styleNumber.charAt(j);
        firstByte[7]  = '\0';
        firstByte[8] = (byte) dispms.size();
        for(i = 0;i<dispms.size();i++){
            Dispms region = dispms.get(i);
            if(good!=null)
                region = ImageHelper.getText(region,good);
            System.out.println(region.getColumnType()+" " +region.getX() + " "+region.getY()+" "+region.getWidth()+" "+region.getHeight());
            ByteAndRegion byteAndRegion = getRegionImage(region, styleNumber,good);
            region = byteAndRegion.getRegion();
            byte[] regionImage = byteAndRegion.getRegionBytes();
            System.out.println(region.getColumnType()+" "+region.getX() + " "+region.getY()+" "+region.getWidth()+" "+region.getHeight());
            // 区域编号
            System.out.println("区域编号"+String.valueOf(regionIdMap.get(region.getSourceColumn())));
            firstByte[(i*12)+9] = Byte.valueOf(String.valueOf(regionIdMap.get(region.getSourceColumn())));
            firstByte[(i*12)+10] =  ColorUtil.getColorByte(region.getBackgroundColor(),region.getFontColor());
            byte[] x,y,height,width;
            if (ImageHelper.getTypeByStyleNumber(styleNumber).equals(StyleType.StyleType_40)){
                width = int2ByteArr(region.getWidth());
                height = int2ByteArr(region.getHeight());
                x = int2ByteArr(region.getX());
                y = int2ByteArr(region.getY());
                // x
                firstByte[(i*12)+11] =  x[1];
                firstByte[(i*12)+12] =  x[0];
                // y
                firstByte[(i*12)+13] =  y[1];
                firstByte[(i*12)+14] =  y[0];
            }
            else{
                width = int2ByteArr(region.getWidth());
                height = int2ByteArr(region.getHeight());
                x = int2ByteArr(region.getX());
                y = int2ByteArr(StyleNumberToHeight.styleNumberToHeight(styleNumber)-region.getY()-region.getHeight());
                // x
                firstByte[(i*12)+11] =  y[1];
                firstByte[(i*12)+12] =  y[0];
                // y
                firstByte[(i*12)+13] =  x[1];
                firstByte[(i*12)+14] =  x[0];
            }
            byte[] length = int2ByteArr(regionImage.length);
            // 长度
            firstByte[(i*12)+15] =  width[1];
            firstByte[(i*12)+16] =  width[0];
            // 宽度
            firstByte[(i*12)+17] =  height[1];
            firstByte[(i*12)+18] =  height[0];
            // 显示存储字节数
            firstByte[(i*12)+19] = length[1];
            firstByte[(i*12)+20] = length[0];
            List<byte[]> byteList = getByteList(regionImage, i + 1);
            allbyteList.addAll(byteList);
        }
        byte[] bytes = allbyteList.get(allbyteList.size() - 1);
        bytes[4] = 0x01;
        return new ByteResponse(firstByte,allbyteList);
    }
    // 分包发送
    public static List<byte[]> getByteList(byte[] regionImage,int number){
        List<byte[]> byteList =  new ArrayList<>();
        int i,j;
        int packageLength = Integer.valueOf(SystemVersionArgs.packageLength);
        int len = regionImage.length / packageLength;
        int remainder = regionImage.length % packageLength;
        if(regionImage.length>packageLength){
            for(i=0;i<len;i++){
                byte[] bytes = new byte[8 + packageLength];
                bytes[0] = 0x03;
                bytes[1] = 0x02;
                // 长度
                bytes[2] = (byte) (5 + packageLength);
                // 编号
                bytes[3] = (byte) number;
                // 是否刷新
                bytes[4] = 0x00;
                byte[] loc = int2ByteArr(i * packageLength);
                // 起始位置
                bytes[5] = loc[1];
                bytes[6] = loc[0];
                // 数据长度
                bytes[7] = (byte) (packageLength);
                int begin = 8;
                // 区域显示数据
                for (j = i * packageLength; j < (i+1)*packageLength; j++)
                    bytes[begin++] = regionImage[j];
                byteList.add(bytes);
            }
            byte[] bytes = new byte[8 + remainder];
            bytes[0] = 0x03;
            bytes[1] = 0x02;
            // 长度
            bytes[2] = (byte) (5 + remainder);
            // 编号
            bytes[3] = (byte) number;
            bytes[4] = 0x00;
            byte[] loc = int2ByteArr(i * packageLength);
            // 起始位置
            bytes[5] = loc[1];
            bytes[6] = loc[0];
            // 数据长度
            bytes[7] = (byte) (remainder);
            int begin = 8;
            // 区域显示数据
            for (j = i * packageLength; j < i * packageLength+remainder; j++)
                bytes[begin++] = regionImage[j];
            byteList.add(bytes);
        }
        else {
            byte[] bytes = new byte[8+regionImage.length];
            bytes[0] = 0x03;
            bytes[1] = 0x02;
            // 长度
            bytes[2] = (byte) (5+regionImage.length);
            // 编号
            bytes[3] = (byte) number;
            // 是否刷新
            bytes[4] = 0x00;
            // 起始位置
            bytes[5] = 0;
            bytes[6] = 0;
            // 数据长度
            bytes[7] = (byte) (regionImage.length);
            // 区域显示数据
            for(i=0;i<regionImage.length;i++)
                bytes[i+8] = regionImage[i];
            byteList.add(bytes);
        }
        return byteList;
    }
    public static ByteAndRegion getRegionImage(Dispms dispM, String styleNumber,Good good) {
        try {
            FileUtil.createFileIfNotExist("D:\\styles\\",styleNumber);
            String columnType = dispM.getColumnType();
            if(!ImageHelper.getImageType(columnType))
                return ImageHelper.getImageByType1(dispM,styleNumber,good);
            else
                return ImageHelper.getImageByType2(dispM,styleNumber,good);
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }
    // 把byte 转化为两位十六进制数
    public static String toHex(byte b) {
        String result = Integer.toHexString(b & 0xFF);
        if (result.length() == 1) {
            result = '0' + result;
        }
        return result;
    }
    // 将数字转为数组
    public static byte[] int2ByteArr(int i,int n){
        byte[] bytes = new byte[n] ;
        int begin =n;
        for(int j=0;j<n;j++) {
            begin = begin-1;
            bytes[j] = (byte)(i >>8*begin);
        }
        return bytes ;
    }
    public static byte[] int2ByteArr(int i){
        byte[] bytes = new byte[2] ;
        bytes[0] = (byte)(i >> 8) ;
        bytes[1] = (byte)(i >> 0) ;
        return bytes ;
    }
    // 十进制整数转点分IP地址
    public static String longToIP(long longIp) {
        StringBuffer sb = new StringBuffer("");
        // 直接右移24位
        sb.append(String.valueOf((longIp >>> 24)));
        sb.append(".");
        // 将高8位置0，然后右移16位
        sb.append(String.valueOf((longIp & 0x00FFFFFF) >>> 16));
        sb.append(".");
        // 将高16位置0，然后右移8位
        sb.append(String.valueOf((longIp & 0x0000FFFF) >>> 8));
        sb.append(".");
        // 将高24位置0
        sb.append(String.valueOf((longIp & 0x000000FF)));
        return sb.toString();
    }
    public static void sleepSomeTime(Long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    // 获取指定对象的指定属性值
    public static String getSourceData(String name, Object source){
        String sourceData = null;
        try {
            Field field = source.getClass().getDeclaredField(name);
            //设置对象的访问权限，保证对private的属性的访问
            field.setAccessible(true);
            sourceData = field.get(source).toString();
        }
        catch (Exception e){}
        return sourceData;
    }
    // 切换大小端
    public static byte[] changeBytes(byte[] a){
        if(a==null) return null;
        byte[] b = new byte[a.length];
        for (int i = 0; i < b.length; i++) {
            b[i] = a[b.length - i - 1];
        }
        return b;
    }
    public static byte[] hexStringToBytes(String str) {
        if(str == null || str.trim().equals("")) {
            return new byte[0];
        }
        byte[] bytes = new byte[str.length() / 2];
        for(int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }
        byte[] result = new byte[4];
        int index =bytes.length-1;
        for(int i=3;i>=0;i--,index--) {
            if(index>=0)
                result[i] = bytes[index];
            else
                result[i] = 0;
        }
        return result;
    }
    public static byte hexStringtoByte(String str){
        return (byte) Integer.parseInt(str, 16);
    }
    public static byte[] getAddressByBarCode(String barCode){
        String substring = barCode.substring(3, 12);
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<9;i++)
            sb.append(substring.charAt(i)-'0');
        substring = Long.toHexString(Long.valueOf(sb.toString()));
        if(substring.length()%2!=0)
            substring="0"+substring;
        if(barCode!=null && barCode.length()==12)
            return changeBytes(hexStringToBytes(substring));
        return new byte[0];
    }
    public static List<byte[]> getAwakeBytes(List<Tag> tags){
        List<byte[]> addressList = new ArrayList<>();
        for (Tag tag: tags) {
            byte[] addressByBarCode = getAddressByBarCode(tag.getBarCode());
            if(addressByBarCode!=null)
                addressList.add(addressByBarCode);
        }
        int i=0,j=0,length = 4*addressList.size();
        List<byte[]> byteList =  new ArrayList<>();
        int len =length / 200;
        int remainder = length % 200;
        if(length>200){
            for(i=0;i<len;i++){
                byte[] bytes = new byte[5 + 200];
                bytes[0] = 0x04;
                bytes[1] = 0x06;
                // 长度
                bytes[2] = (byte) (2 + 200);
                // 地址数量
                bytes[3] = (byte) 50;
                // 是否刷新
                bytes[4] = 0x00;
                int begin = 5;
                for (j = i * 50; j < (i+1)*50; j++) {
                    byte[] address = addressList.get(j);
                    for (byte b : address)
                        bytes[begin++] = b;
                }
                byteList.add(bytes);
            }
            byte[] bytes = new byte[5 + remainder];
            bytes[0] = 0x04;
            bytes[1] = 0x06;
            // 长度
            bytes[2] = (byte) (2 + remainder);
            // 地址数量
            bytes[3] = (byte) (remainder/4);
            bytes[4] = 0x01;
            int begin = 5;
            for (j = i * 50; j < i * 50+remainder/4; j++) {
                byte[] address = addressList.get(j);
                for (byte b : address)
                    bytes[begin++] = b;
            }
            byteList.add(bytes);
        }
        else {
            byte[] bytes = new byte[5+length];
            bytes[0] = 0x04;
            bytes[1] = 0x06;
            // 长度
            bytes[2] = (byte) (2+length);
            // 地址数量
            bytes[3] = (byte) (length/4);
            // 是否刷新
            bytes[4] =  0x01;
            int begin = 5;
            for(i=0;i<addressList.size();i++) {
                byte[] address = addressList.get(i);
                for (byte b : address)
                    bytes[begin++] = b;
            }
            byteList.add(bytes);
        }
        return byteList;
    }
    public static String bytesToString(byte[] bytes){
        StringBuffer sb = new StringBuffer();
        for (byte b : bytes)
            sb.append(b);
        return sb.toString();
    }
    public static Channel getChannelByRouter(Router router){
        Channel channel = getChannelIdGroup().get(router.getBarCode());
        return channel;
    }
    public static Channel getChannelByRouter(Long routerId){
        RouterService routerService = (RouterService) getBean("RouterService");
        Router router= routerService.findById(routerId).get();
        Channel channel = getChannelIdGroup().get(router.getBarCode());
        return channel;
    }
    public static Router getRouterByChannel(Channel channel){
        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        Router router = ((RouterService)getBean("RouterService")).findByIp(socketAddress.getAddress().getHostAddress());
        return router;
    }
    public static void printBytes(String comment,byte []message){
        System.out.print(comment+" ");
        for(byte b : message)
            System.out.print(toHex(b)+" ");
        //System.out.print(b+" ");
        System.out.println();
    }
    public static HashMap<String,Integer> getRegionIdList(String regionName,List<Dispms> dispms){
        HashMap<String,Integer> map = new HashMap<>();
        String[] regionNames = regionName.split(" ");
        String styleRegionNames = getStyleRegionNames(dispms);
        regionNames = getRealRegionNames(regionNames, styleRegionNames).split(" ");
        for(String name : regionNames){
            Integer idByRegionName = getIdByRegionName(dispms, name);
            System.out.println(name +" "+ idByRegionName);
            if(idByRegionName>0)
                map.put(name,idByRegionName);
        }
        return map;
    }
    private static String getStyleRegionNames(List<Dispms> dispms){
        StringBuffer sb = new StringBuffer();
        for(Dispms dispm:dispms){
            sb.append(dispm.getSourceColumn()+" ");
        }
        return sb.toString();
    }
    private static String getRealRegionNames(String[] regionNames,String styleRegionNames ){
        StringBuffer sb = new StringBuffer();
        for(String name:regionNames){
            if(styleRegionNames.contains(name))
                sb.append(name+" ");
        }
        return sb.toString();
    }
    public static Integer getIdByRegionName(List<Dispms> dispms,String name){
        for(int i=0;i<dispms.size();i++){
            if(dispms.get(i).getSourceColumn().equals(name))
                return i+1;
        }
        return 0;
    }
    public static Map<String, Channel> channelIdGroup = new HashMap();
    public static ServerChannelHandler serverChannelHandler;
    private static ArrayList<Channel> workingChannel = new ArrayList<>();
    public static ArrayList<Channel> getWorkingChannel() {
        return workingChannel;
    }
    public static void addWorkingChannel(Channel channel){
        workingChannel.add(channel);
    }
    public static void removeWorkingChannel(Channel channel){
        workingChannel.remove(channel);
    }
    public static boolean isWorking(Channel channel){
        return workingChannel.contains(channel);
    }


    public static Map<String, Channel> getChannelIdGroup() {
        return channelIdGroup;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringContextUtil.applicationContext == null) {
            SpringContextUtil.applicationContext = applicationContext;
        }
    }

    // 获取applicationContext
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    // 通过name获取 Bean.
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    // 通过class获取Bean.
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    //通过name,以及Clazz返回指定的Bean
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    public SpringContextUtil() {
    }
}

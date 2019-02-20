
package com.datagroup.ESLS.utils;

import com.datagroup.ESLS.common.constant.StyleNumberToHeight;
import com.datagroup.ESLS.common.constant.StyleType;
import com.datagroup.ESLS.common.constant.TableConstant;
import com.datagroup.ESLS.common.response.ByteResponse;
import com.datagroup.ESLS.entity.*;
import com.datagroup.ESLS.graphic.BarCode;
import com.datagroup.ESLS.graphic.QRCode;
import com.datagroup.ESLS.netty.server.ServerChannelHandler;
import com.datagroup.ESLS.service.GoodService;
import com.datagroup.ESLS.service.RouterService;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * @author lenovo
 */
@Component
public class SpringContextUtil implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
    private static BufferedImage createBufferedImage(int width, int height) throws IOException {
        // 单色位图
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++)
                bufferedImage.setRGB(i, j, Color.WHITE.getRGB());
        }
        return bufferedImage;
    }
    public static ByteResponse getRequest(List<Dispms> dispms,String styleNumber,Good good){
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
        try {
            for (i = 0; i < dispms.size(); i++) {
                Dispms region = dispms.get(i);
                if (good != null )
                    region = getText(region, good);
                byte[] regionImage = getRegionImage(region, styleNumber);
                // 区域编号
                firstByte[(i * 12) + 9] = (byte) (i + 1);
                firstByte[(i * 12) + 10] = ColorUtil.getColorByte(region.getBackgroundColor(), region.getFontColor());
                byte[] x,y,height,width;
                if (getTypeByStyleNumber(styleNumber).equals(StyleType.StyleType_42)){
                    width = int2ByteArr(get8Number(region.getWidth()));
                    height = int2ByteArr(region.getHeight());
                    x = int2ByteArr(region.getX());
                    y = int2ByteArr(region.getY());
                    // x
                    firstByte[(i * 12) + 11] = x[1];
                    firstByte[(i * 12) + 12] = x[0];
                    // y
                    firstByte[(i * 12) + 13] = y[1];
                    firstByte[(i * 12) + 14] = y[0];
                }
                else{
                    width = int2ByteArr(region.getWidth());
                    height = int2ByteArr(get8Number(region.getHeight()));
                    x = int2ByteArr(region.getX());
                    y = int2ByteArr(StyleNumberToHeight.styleNumberToHeight(styleNumber) - region.getY());
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
            System.out.println("结束");
        }
        catch (Exception e){
            System.out.println("getRequest:"+e);
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
                region = getText(region,good);
            byte[] regionImage = getRegionImage(region,styleNumber);
            // 区域编号
            System.out.println("区域编号"+String.valueOf(regionIdMap.get(region.getSourceColumn())));
            firstByte[(i*12)+9] = Byte.valueOf(String.valueOf(regionIdMap.get(region.getSourceColumn())));
            firstByte[(i*12)+10] =  ColorUtil.getColorByte(region.getBackgroundColor(),region.getFontColor());
            byte[] x,y,height,width;
            if (getTypeByStyleNumber(styleNumber).equals(StyleType.StyleType_42)){
                width = int2ByteArr(get8Number(region.getWidth()));
                System.out.println(get8Number(region.getWidth()));
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
                height = int2ByteArr(get8Number(region.getHeight()));
                x = int2ByteArr(region.getX());
                y = int2ByteArr(StyleNumberToHeight.styleNumberToHeight(styleNumber)-region.getY());
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
        int packageLength = getPackageLength();
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
    private static Dispms getText(Dispms dispM,Good good) {
        if(dispM.getSourceColumn().equalsIgnoreCase("0")) {
            return dispM;
        }
        StringBuilder sqlBuilder = new StringBuilder("select ");
        sqlBuilder.append(dispM.getSourceColumn()).append(" ");
        sqlBuilder.append("from ").append(TableConstant.TABLE_GOODS).append(" ");
        sqlBuilder.append("where id=").append(good.getId());
        List list = ((GoodService) getBean("GoodService")).findBySql(sqlBuilder.toString());
        if (list != null && list.size() > 0) {
            Object obj = list.get(0);
            if (dispM.getColumnType().contains("数字")) {
                dispM.setText(String.format("%.2f", new Object[]{
                        obj
                }));
            } else if (dispM.getColumnType().equals("条形码")) {
                Long barCodeNumber = Long.parseLong(good.getBarCode());
                dispM.setText(String.valueOf(barCodeNumber));
            }
            // 字符串
            else if(dispM.getColumnType().equals("字符串") || dispM.getColumnType().equals("二维码")){
                dispM.setText(String.valueOf(obj));
            }
        }
        return dispM;
    }
    public static byte[] getRegionImage(Dispms dispM,String styleNumber) {
        BufferedImage bufferedImage ;
        int bold = 0;
        byte result[] = null;
        int imageWidth,imageHeight;
        try {
            if (getTypeByStyleNumber(styleNumber).equals(StyleType.StyleType_42)){
                imageWidth = get8Number(dispM.getWidth());
                imageHeight = dispM.getHeight();
            }
            else{
                imageWidth = dispM.getWidth();
                imageHeight = get8Number(dispM.getHeight());
            }
            bufferedImage = createBufferedImage(imageWidth, imageHeight);
            Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
            Boolean flag = ColorUtil.isRedAndBlack(dispM.getBackgroundColor(), dispM.getFontColor());
            // (红黑)
            if(flag) {
                g2d.setBackground(ColorUtil.getColorByInt(dispM.getBackgroundColor() == 0 ? 1 : dispM.getBackgroundColor()));
            }
            else
                g2d.setBackground(ColorUtil.getColorByInt(dispM.getBackgroundColor()));
            g2d.clearRect(0, 0, imageWidth, imageHeight);
            if((dispM.getColumnType().contains("数字") && !dispM.getText().contains(".")) || dispM.getColumnType().contains("字符串")){
                if (dispM.getFontBold().equals("1"))
                    bold = 1;
                Font font = new Font(dispM.getFontFamily(), bold, dispM.getFontSize());
                g2d.setFont(font);
                // 字体色
                if(flag)
                    g2d.setColor(ColorUtil.getColorByInt(dispM.getFontColor()==0?1:dispM.getFontColor()));
                else
                    g2d.setColor(ColorUtil.getColorByInt(dispM.getFontColor()));
                String s = dispM.getStartText() + dispM.getText() + dispM.getEndText();
                g2d.drawString(s, 0, get8Number(dispM.getHeight()) );
            }
            else if(dispM.getColumnType().contains("数字")  &&  dispM.getText().contains(".")){
                if (dispM.getFontBold().equals("1"))
                    bold = 1;
                Font font = new Font(dispM.getFontFamily(), bold, dispM.getFontSize());
                g2d.setFont(font);
                // 字体色
                if(flag)
                    g2d.setColor(ColorUtil.getColorByInt(dispM.getFontColor()==0?1:dispM.getFontColor()));
                else
                    g2d.setColor(ColorUtil.getColorByInt(dispM.getFontColor()));
                String s =  dispM.getText();
                String begin = s.substring(0,s.indexOf(".")+1);
                String end = s.substring(s.indexOf(".")+1);
                g2d.drawString(begin, 0, get8Number(dispM.getHeight()) );
                FontMetrics fontMetrics = g2d.getFontMetrics(font);
                int strWidth = fontMetrics.stringWidth(begin);
                font =  new Font(dispM.getFontFamily(), bold, dispM.getFontSize()-5);
                g2d.setFont(font);
                g2d.drawString(end, strWidth, get8Number(dispM.getHeight())-5 );
                if(dispM.getIsLineation()==1)
                    g2d.drawLine(0,imageHeight/2,imageWidth,imageHeight/2);
            }
            else if (dispM.getColumnType().contains("二维码") ) {
                int value = Math.min(dispM.getWidth(), get8Number(dispM.getHeight()) );
                BufferedImage img = QRCode.encode(dispM.getText(), value, value);
                g2d.drawImage(img, 0, 0, null);
                ImageIO.write(img, "BMP", new File("D:\\"+dispM.getId()+dispM.getColumnType()+"二维码"+".bmp"));
            } else if (dispM.getColumnType().contains("条形码") ) {
                BufferedImage img = BarCode.encode(dispM.getText(), dispM.getWidth(), get8Number(dispM.getHeight()) );
                g2d.drawImage(img, 0, 0, null);
                ImageIO.write(img, "BMP", new File("D:\\"+dispM.getId()+dispM.getColumnType()+"条形码"+".bmp"));
            }
            else if(dispM.getColumnType().contains("线段") ) {
                g2d.drawLine(0,imageHeight,imageWidth,imageHeight);
            }
            ImageIO.write(bufferedImage, "BMP", new File("D:\\"+dispM.getId()+dispM.getColumnType()+".bmp"));
            result = changeImage(bufferedImage,styleNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    public static byte[] changeImage(BufferedImage bimg,String styleNumber) {
        // 212 104   296 128
        // 400 300
        int[][] data = new int[bimg.getWidth()][bimg.getHeight()];
        byte result[] = new byte[bimg.getWidth() * bimg.getHeight() / 8];
        int sum = 0;
        System.out.println(styleNumber);
        if (getTypeByStyleNumber(styleNumber).equals(StyleType.StyleType_42)) {
            for (int i = 0; i < bimg.getHeight(); i++) {
                int time = 0;
                StringBuffer byteString = new StringBuffer();
                for (int j = 0; j < bimg.getWidth(); j++) {
                    data[j][i] = bimg.getRGB(j, i);
                    if (data[j][i] == -1 && time < 8) {
                        byteString.append("1");
                        time++;
                    } else if (data[j][i] == -16777216 && time < 8) {
                        byteString.append("0");
                        time++;
                    } else if (time == 8) {
                        byte b = (byte) Integer.parseInt(byteString.toString(), 2);
                        result[sum++] = b;
                        time = 0;
                        byteString = new StringBuffer();
                        j--;
                    }
                    if(j==bimg.getWidth()-1) {
                        byte b = (byte) Integer.parseInt(byteString.toString(),2);
                        result[sum++]=b;
                    }
                }
            }
        } else {
            for (int i = 0; i < bimg.getWidth(); i++) {
                int time = 0;
                StringBuffer byteString = new StringBuffer();
                //-16777216 -1
                for (int j = bimg.getHeight() - 1; j >= 0; j--) {
                    data[i][j] = bimg.getRGB(i, j);
                    if (data[i][j] == -1 && time < 8) {
                        byteString.append("1");
                        time++;
                    } else if (data[i][j] == -16777216 && time < 8) {
                        byteString.append("0");
                        time++;
                    } else if (time == 8) {
                        byte b = (byte) Integer.parseInt(byteString.toString(), 2);
                        result[sum++] = b;
                        time = 0;
                        byteString = new StringBuffer();
                        j++;
                    }
                    if (j == 0) {
                        byte b = (byte) Integer.parseInt(byteString.toString(), 2);
                        result[sum++] = b;
                    }
                }
            }
        }
        System.out.println("全部区域数据开始");
        for (int i = 0; i < result.length; i++) {
            byte b = result[i];
            System.out.print (toHex(b)+"    ");
            if(i%12==0 && i!=0)
                System.out.println();
        }
        System.out.println();
        System.out.println("全部区域数据结束");
        return result;
    }
    public static Integer getTypeByStyleNumber(String styleNumber){
        if(styleNumber.substring(0,2).equals("13") )
            return StyleType.StyleType_13;
        else if(styleNumber.substring(0,2).equals("21") )
            return StyleType.StyleType_21;
        else if(styleNumber.substring(0,2).equals("29") )
            return StyleType.StyleType_29;
        else if(styleNumber.substring(0,2).equals("42") )
            return StyleType.StyleType_42;
        return 0;
    }
    // 把byte 转化为两位十六进制数
    public static String toHex(byte b) {
        String result = Integer.toHexString(b & 0xFF);
        if (result.length() == 1) {
            result = '0' + result;
        }
        return result;
    }
    // 将数字转换为8的整数倍
    public static int get8Number(int a){
        // 47  57  29
        int remainder =  a % 8;
        return a+(8-remainder);
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
        //InetSocketAddress tagAddress = new InetSocketAddress(router.getIp(), router.getPort());
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
    public static ChannelGroup channelGroup;
    public static Map<String, Channel> channelIdGroup = new HashMap();
    public static ServerChannelHandler serverChannelHandler;
    private static String commandTime = "4";
    private static Long aliveTime = (long) 6000;
    private static int repeatTime = 3;
    private static Tag currentTag ;
    private static Integer PackageLength = 220;
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
        // System.out.println("路由器是否工作："+workingChannel.contains(channel));
        return workingChannel.contains(channel);
    }
    public static int getRepeatTime() {
        return repeatTime;
    }

    public static void setRepeatTime(int repeatTime) {
        SpringContextUtil.repeatTime = repeatTime;
    }

    public static String getCommandTime() {
        return commandTime;
    }

    public static void setCommandTime(String commandTime) {
        SpringContextUtil.commandTime = commandTime;
    }

    public static Long getAliveTime() {
        return aliveTime;
    }

    public static void setAliveTime(Long aliveTime) {
        SpringContextUtil.aliveTime = aliveTime;
    }

    public static Tag getCurrentTag() {
        return currentTag;
    }

    public static void setCurrentTag(Tag currentTag) {
        SpringContextUtil.currentTag = currentTag;
    }
    public static Integer getPackageLength(){
        return PackageLength;
    }
    public static void setPackageLength(Integer PackageLength){
        SpringContextUtil.PackageLength = PackageLength;
    }
    public static Map<String, Channel> getChannelIdGroup() {
        return channelIdGroup;
    }

    public static void setChannelIdGroup(Map<String, Channel> channelIdGroup) {
        SpringContextUtil.channelIdGroup = channelIdGroup;
    }


    public static ChannelGroup getChannelGroup() {
        return channelGroup;
    }

    public static void setChannelGroup(ChannelGroup channelGroup) {
        SpringContextUtil.channelGroup = channelGroup;
    }
    static {
        channelGroup = new DefaultChannelGroup("NettyServer", GlobalEventExecutor.INSTANCE);
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

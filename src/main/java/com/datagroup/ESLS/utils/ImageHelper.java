package com.datagroup.ESLS.utils;

import com.datagroup.ESLS.common.constant.StyleType;
import com.datagroup.ESLS.common.constant.TableConstant;
import com.datagroup.ESLS.dto.ByteAndRegion;
import com.datagroup.ESLS.entity.Dispms;
import com.datagroup.ESLS.entity.Good;
import com.datagroup.ESLS.graphic.BarCode;
import com.datagroup.ESLS.graphic.QRCode;
import com.datagroup.ESLS.service.DispmsService;
import com.datagroup.ESLS.service.GoodService;
import org.springframework.beans.BeanUtils;
import sun.font.FontDesignMetrics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class ImageHelper {
    public static ByteAndRegion getImageByType1(Dispms dispM,String styleNumber) throws Exception {
        // 宽 高 columnType backgroundColor fontColor（非文字）
        String columnType = dispM.getColumnType();
        int imageWidth = dispM.getWidth(),imageHeight = dispM.getHeight();
        if (getTypeByStyleNumber(styleNumber).equals(StyleType.StyleType_40)){
            imageWidth = get8Number(imageWidth);
        }
        else{
            imageHeight = get8Number(imageHeight);
        }
        dispM.setWidth(imageWidth);
        dispM.setHeight(imageHeight);
        BufferedImage bufferedImage = SpringContextUtil.createBufferedImage(imageWidth, imageHeight);
        Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
        // 背景
        g2d.setColor(ColorUtil.getColorByInt(dispM.getBackgroundColor()));
        g2d.fillRect(0, 0, imageWidth, imageHeight);
        // 二维码
        if (columnType.contains(StringUtil.QRCODE) ) {
            int value = Math.min(get8Number(imageWidth), get8Number(imageHeight));
            bufferedImage = QRCode.encode(dispM.getText(), value, value);
        }
        // 条形码
        else if (dispM.getColumnType().contains(StringUtil.BARCODE) ) {
            BufferedImage image= BarCode.encode(dispM.getText(), imageWidth, imageHeight );
            g2d.drawImage(image,0,0,null);

        }
        // 线段
        else if(columnType.contains(StringUtil.LINE) ) {
            g2d.drawLine(0,imageHeight,imageWidth,imageHeight);
        }
        // 背景
        else if(columnType.contains(StringUtil.BACKGROUND)){
            bufferedImage = SpringContextUtil.createBackgroundImage(imageWidth, imageHeight);
        }
        // 图片
        else if(columnType.contains(StringUtil.PHOTO)){
            URL url = new URL(dispM.getImageUrl());
            bufferedImage = ImageIO.read(url);
            // image转bytes
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "bmp", out);
            byte[] bytes = ImageHelper.ChangeImgSize(out.toByteArray(), imageWidth, imageHeight);
            // bytes转image
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            bufferedImage = ImageIO.read(in);
        }
        ImageIO.write(bufferedImage, "BMP", new File("D:\\styles\\"+styleNumber+"\\"+dispM.getId()+columnType+"("+dispM.getX()+" "+dispM.getY()+" "+dispM.getWidth()+" "+dispM.getHeight()+").bmp"));
        return new ByteAndRegion(changeImage(bufferedImage,styleNumber),dispM);
    }
    public static ByteAndRegion getImageByType2(Dispms dispM,String styleNumber) throws Exception {
        // 文本宽 高 columnType backgroundColor fontColor（非文字）
        DispmsService dispmsService = (DispmsService) SpringContextUtil.getBean("DispmsService");
        String columnType = dispM.getColumnType();
        int fontType = ColorUtil.getFontType(dispM.getFontType());
        int imageWidth,imageHeight ,imageAscent;
        Boolean flag = ColorUtil.isRedAndBlack(dispM.getBackgroundColor(), dispM.getFontColor());
        // 以下为含字符串或数字
        String s = StringUtil.getRealString(dispM);
        String[] args = ImageHelper.getWidthAndHeight(dispM.getFontFamily(), fontType, dispM.getFontSize(),columnType, s);
        imageWidth = Integer.valueOf(args[0]);
        imageHeight = Integer.valueOf(args[1]);
        imageAscent = Integer.valueOf(args[2]);
        if (getTypeByStyleNumber(styleNumber).equals(StyleType.StyleType_40)){
            imageWidth = get8Number(imageWidth);
        }
        else{
            imageHeight = get8Number(imageHeight);
        }
        dispM.setWidth(imageWidth);
        dispM.setHeight(imageHeight);
        BufferedImage bufferedImage = SpringContextUtil.createBufferedImage(imageWidth, imageHeight);
        Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
        // (红黑) 0红黑 1白 背景色
        g2d.setColor(ColorUtil.getColorByInt(dispM.getBackgroundColor()));
        g2d.fillRect(0, 0, imageWidth, imageHeight);
        Font font = new Font(dispM.getFontFamily(), fontType, dispM.getFontSize());
        g2d.setFont(font);
        g2d = ImageHelper.getGraphiceByColor(g2d,dispM,flag);
        // 数字左侧 数字
        if(columnType.contains(StringUtil.NUMBER_LEFT) || columnType.equals(StringUtil.NUMBER)){
            String backup = dispM.getBackup();
            if(backup.equals("1"))
                g2d.drawLine(0,imageHeight/2,imageWidth,imageHeight/2);
            g2d.drawString(s, 0,imageAscent);
        }
        // 数字右侧
        else if(columnType.contains(StringUtil.NUMBER_RIGHT)){
            Dispms left = dispmsService.findByStyleIdAndColumnTypeAndSourceColumn(dispM.getStyle().getId(), StringUtil.NUMBER_LEFT, dispM.getSourceColumn());
            dispM.setX(left.getX()+left.getWidth());
            String right = s.substring(s.indexOf(".")+1);
            String backup = dispM.getBackup();
            if(backup.equals("1"))
                g2d.drawLine(0,imageHeight/2,imageWidth,imageHeight/2);
            g2d.drawString(right, 0,imageAscent);
        }
        // 字符串
        else if(columnType.contains(StringUtil.Str)){
            g2d.drawString(s, 0,imageAscent);
        }
        ImageIO.write(bufferedImage, "BMP", new File("D:\\styles\\"+styleNumber+"\\"+dispM.getId()+columnType+"("+dispM.getX()+" "+dispM.getY()+" "+dispM.getWidth()+" "+dispM.getHeight()+").bmp"));
        return new ByteAndRegion(changeImage(bufferedImage,styleNumber),dispM);
    }
    public static Dispms getText(Dispms dispM, Good good) {
        if(dispM.getSourceColumn().equalsIgnoreCase("0")) {
            return dispM;
        }
        StringBuilder sqlBuilder = new StringBuilder("select ");
        sqlBuilder.append(dispM.getSourceColumn()).append(" ");
        sqlBuilder.append("from ").append(TableConstant.TABLE_GOODS).append(" ");
        sqlBuilder.append("where id=").append(good.getId());
        List list = ((GoodService) SpringContextUtil.getBean("GoodService")).findBySql(sqlBuilder.toString());
        if (list != null && list.size() > 0) {
            Object obj = list.get(0);
            if (dispM.getColumnType().contains("数字")) {
                dispM.setText(String.format("%.2f", new Object[]{
                        obj
                }));
            } else if (dispM.getColumnType().equals("条形码")) {
                dispM.setText(good.getBarCode());
            }
            // 字符串
            else {
                dispM.setText(String.valueOf(obj));
            }
        }
        return dispM;
    }
    public static  byte[] ChangeImgSize(byte[] data, int nw, int nh){
        byte[] newdata = null;
        try{
            BufferedImage bis = ImageIO.read(new ByteArrayInputStream(data));
            int w = bis.getWidth();
            int h = bis.getHeight();
            double sx = (double) nw / w;
            double sy = (double) nh / h;
            AffineTransform transform = new AffineTransform();
            transform.setToScale(sx, sy);
            AffineTransformOp ato = new AffineTransformOp(transform, null);
            //原始颜色
            BufferedImage bid = new BufferedImage(nw, nh, 12);
            ato.filter(bis, bid);
            //转换成byte字节
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bid, "bmp", baos);
            newdata = baos.toByteArray();
        }catch(Exception e){
            e.printStackTrace();
        }
        return newdata;
    }
    public static String[] getWidthAndHeight(String name, int style, int size, String columnType,String realString){
        if(columnType.equals(StringUtil.NUMBER_LEFT))
            realString = realString.substring(0,realString.indexOf(".")+1);
        else if(columnType.equals(StringUtil.NUMBER_RIGHT))
            realString = realString.substring(realString.indexOf(".")+1);
        StringBuffer sb = new StringBuffer();
        Font font = new Font(name, style, size);
        FontDesignMetrics metrics = FontDesignMetrics.getMetrics(font);
        sb.append(metrics.stringWidth(realString)+" ");
        sb.append(metrics.getHeight()+" ");
        sb.append(metrics.getAscent());
        return sb.toString().split(" ");
    }
    public static Graphics2D getGraphiceByColor(Graphics2D g2d,Dispms dispM,boolean flag){
        if(flag)
            g2d.setColor(ColorUtil.getColorByInt(dispM.getFontColor()==0?1:dispM.getFontColor()));
        else
            g2d.setColor(ColorUtil.getColorByInt(dispM.getFontColor()));
        return g2d;
    }
    public static boolean getImageType(String columnType){
        if(columnType.contains(StringUtil.QRCODE)  || columnType.contains(StringUtil.BARCODE) || columnType.contains(StringUtil.BACKGROUND)  || columnType.contains(StringUtil.PHOTO)  || columnType.contains(StringUtil.LINE))
            return false;
        return true;
    }

    public static byte[] changeImage(BufferedImage bimg,String styleNumber) {
        // 212 104   296 128
        // 400 300
        int[][] data = new int[bimg.getWidth()][bimg.getHeight()];
        byte result[] = new byte[bimg.getWidth() * bimg.getHeight() / 8];
        int sum = 0;
        if (getTypeByStyleNumber(styleNumber).equals(StyleType.StyleType_40)) {
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
//        System.out.println("全部区域数据开始");
//        for (int i = 0; i < result.length; i++) {
//            byte b = result[i];
//            System.out.print (SpringContextUtil.toHex(b)+"    ");
//            if(i%12==0 && i!=0)
//                System.out.println();
//        }
//        System.out.println();
//        System.out.println("全部区域数据结束");
        return result;
    }
    public static Integer getTypeByStyleNumber(String styleNumber){
        if(styleNumber.substring(0,2).equals("21") )
            return StyleType.StyleType_21;
        else if(styleNumber.substring(0,2).equals("25") )
            return StyleType.StyleType_25;
        else if(styleNumber.substring(0,2).equals("29") )
            return StyleType.StyleType_29;
        else if(styleNumber.substring(0,2).equals("40") )
            return StyleType.StyleType_40;
        return 0;
    }
    // 将数字转换为8的整数倍
    public static int get8Number(int a){
        if(a % 8 ==  0)
            return a;
        // 47  57  29
        int remainder =  a % 8;
        return a+(8-remainder);
    }
}

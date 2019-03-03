package com.datagroup.ESLS.utils;

import com.datagroup.ESLS.common.constant.StyleType;
import com.datagroup.ESLS.common.constant.TableConstant;
import com.datagroup.ESLS.common.response.ByteResponse;
import com.datagroup.ESLS.common.response.ResponseBean;
import com.datagroup.ESLS.dto.ByteAndRegion;
import com.datagroup.ESLS.entity.Dispms;
import com.datagroup.ESLS.entity.Good;
import com.datagroup.ESLS.entity.Tag;
import com.datagroup.ESLS.graphic.BarCode;
import com.datagroup.ESLS.graphic.BarcodeUtil;
import com.datagroup.ESLS.graphic.QRCode;
import com.datagroup.ESLS.service.DispmsService;
import com.datagroup.ESLS.service.GoodService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import sun.font.FontDesignMetrics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class ImageHelper {
    public static ByteAndRegion getImageByType1(Dispms dispM,String styleNumber,Good good) throws Exception {
        DispmsService dispmsService = (DispmsService) SpringContextUtil.getBean("DispmsService");
        Dispms returnDispms = new Dispms();
        BeanUtils.copyProperties(dispM,returnDispms);
        returnDispms.setId(0);
        // 宽 高 columnType backgroundColor fontColor（非文字）
        String columnType = dispM.getColumnType();
        int imageWidth = dispM.getWidth(),imageHeight = dispM.getHeight();
        if (getTypeByStyleNumber(styleNumber).equals(StyleType.StyleType_40)){
            imageWidth = get8Number(imageWidth);
        }
        else{
            imageHeight = get8Number(imageHeight);
        }
        returnDispms.setWidth(imageWidth);
        returnDispms.setHeight(imageHeight);
        BufferedImage bufferedImage = createBufferedImage(imageWidth, imageHeight);
        Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
        // 背景
        g2d.setColor(ColorUtil.getColorByInt(dispM.getBackgroundColor()));
        g2d.fillRect(0, 0, imageWidth, imageHeight);
        // 二维码
        if (columnType.contains(StringUtil.QRCODE) ) {
            int value = Math.min(get8Number(imageWidth), get8Number(imageHeight));
            BufferedImage image = QRCode.encode(dispM.getText(), value, value);
            g2d.drawImage(image,0,0,null);
        }
        // 条形码
        else if (dispM.getColumnType().contains(StringUtil.BARCODE) ) {
//            byte[] bytes = ImageHelper.ChangeImgSize( BarcodeUtil.generateFile(dispM.getText()), imageWidth, imageHeight);
//            // bytes转image
//            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
//            BufferedImage image = ImageIO.read(in);
            String result = SpringContextUtil.getSourceData(dispM.getSourceColumn(), good);
            BufferedImage image= BarCode.encode(result, imageWidth, imageHeight );
            g2d.drawImage(image,0,0,null);
        }
        // 线段
        else if(columnType.contains(StringUtil.LINE) ) {
            String result = SpringContextUtil.getSourceData("name", good);
            Dispms name = dispmsService.findByStyleIdAndColumnTypeAndSourceColumn(dispM.getStyle().getId(), StringUtil.Str, "name");
            int fontType = ColorUtil.getFontType(name.getFontType());
            String[] leftArgs = ImageHelper.getWidthAndHeight(name.getFontFamily(), fontType, name.getFontSize(),StringUtil.Str, result);
            returnDispms.setY(Integer.valueOf(leftArgs[1]));
            bufferedImage = createBackgroundImage(imageWidth, imageHeight);
            g2d = (Graphics2D) bufferedImage.getGraphics();
            g2d.drawLine(0,imageHeight,imageWidth,imageHeight);
        }
        // 背景
        else if(columnType.contains(StringUtil.BACKGROUND)){
            bufferedImage = createBackgroundImage(imageWidth, imageHeight);
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
        ImageIO.write(bufferedImage, "BMP", new File("D:\\styles\\"+styleNumber+"("+dispM.getStyle().getId()+")"+"\\"+dispM.getId()+columnType+" (x"+returnDispms.getX()+" y"+returnDispms.getY()+" w"+returnDispms.getWidth()+" h"+returnDispms.getHeight()+").bmp"));
        return new ByteAndRegion(changeImage(bufferedImage,styleNumber),returnDispms);
    }
    public static ByteAndRegion getImageByType2(Dispms dispM,String styleNumber,Good good) throws Exception {
        DispmsService dispmsService = (DispmsService)SpringContextUtil.getBean("DispmsService");
        Dispms returnDispms = new Dispms();
        BeanUtils.copyProperties(dispM,returnDispms);
        returnDispms.setId(0);
        // 文本宽 高 columnType backgroundColor fontColor（非文字）
        String columnType = dispM.getColumnType();
        int fontType = ColorUtil.getFontType(dispM.getFontType());
        int imageWidth,imageHeight ,imageAscent;
        Boolean flag = ColorUtil.isRedAndBlack(dispM.getBackgroundColor(), dispM.getFontColor());
        // 以下为含字符串或数字
        String s = StringUtil.getRealString(dispM,good);
        //获得最大宽
        String[] args = ImageHelper.getWidthAndHeight(dispM.getFontFamily(), fontType, dispM.getFontSize(),columnType, s);
        imageWidth = dispM.getWidth();
        imageHeight = Integer.valueOf(args[1]);
        imageAscent = Integer.valueOf(args[2]);
        if (getTypeByStyleNumber(styleNumber).equals(StyleType.StyleType_40)){
            imageWidth = get8Number(imageWidth);
        }
        else{
            imageHeight = get8Number(imageHeight);
        }
        returnDispms.setWidth(imageWidth);
        returnDispms.setHeight(imageHeight);
        // 存高
        dispM.setHeight(imageHeight);
        BufferedImage bufferedImage = createBufferedImage(imageWidth, imageHeight);
        Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
        // (红黑) 0红黑 1白 背景色
        g2d.setColor(ColorUtil.getColorByInt(dispM.getBackgroundColor()));
        g2d.fillRect(0, 0, imageWidth, imageHeight);
        Font font = new Font(dispM.getFontFamily(), fontType, dispM.getFontSize());
        g2d.setFont(font);
        g2d = ImageHelper.getGraphiceByColor(g2d,dispM,flag);
        //  数字
        if(columnType.equals(StringUtil.NUMBER)){
            String left = s.substring(0,s.indexOf(".")+1);
            String right = s.substring(s.indexOf(".")+1);
            String[] leftArgs = ImageHelper.getWidthAndHeight(dispM.getFontFamily(), fontType, dispM.getFontSize(), columnType, left);
            String[] backup = dispM.getBackup().split("/");
            if(Integer.valueOf(right)>0) {
                String[] rightArgs = ImageHelper.getWidthAndHeight(dispM.getFontFamily(), fontType, Integer.valueOf(backup[1]), columnType, right);
                // 左侧数字直接画
                g2d.drawString(left, 0, imageAscent);
                // 0/fontsize/left的宽我存
                // 画划线
                if (backup[0].equals("1")) {
                    g2d.drawLine(0, imageHeight / 2, Integer.valueOf(leftArgs[0]), imageHeight / 2);
                    g2d.drawLine(Integer.valueOf(leftArgs[0]), Integer.valueOf(rightArgs[1]) / 2, Integer.valueOf(leftArgs[0]) + Integer.valueOf(rightArgs[0]), Integer.valueOf(rightArgs[1]) / 2);
                }
                font = new Font(dispM.getFontFamily(), fontType, Integer.valueOf(backup[1]));
                g2d.setFont(font);
                // 画右侧数字 改变画笔
                g2d.drawString(right, Integer.valueOf(leftArgs[0]), Integer.valueOf(rightArgs[2]));
                dispM.setText(s);
            }
            else {
                g2d.drawString(left.substring(0,left.length()-1), 0,imageAscent);
                if(backup[0].equals("1")) {
                    g2d.drawLine(0, imageHeight / 2, Integer.valueOf(leftArgs[0]), imageHeight / 2);
                }
                dispM.setText(left);
            }
            // 还是要存fontSize
            if (backup.length == 2)
                dispM.setBackup(dispM.getBackup() + "/" + leftArgs[0]);
            else {
                String[] backup1 = dispM.getBackup().split("/");
                dispM.setBackup(backup1[0] + "/" + backup1[1] + "/" + leftArgs[0]);
            }
        }
        // 字符串
        else if(columnType.contains(StringUtil.Str)){
            g2d.drawString(s, 0,imageAscent);
            // 字符串的宽
            //dispM.setBackup(args[0]);
            dispM.setText(s);
        }
        dispmsService.saveOne(dispM);
        ImageIO.write(bufferedImage, "BMP", new File("D:\\styles\\"+styleNumber+"("+dispM.getStyle().getId()+")"+"\\"+dispM.getId()+columnType+" (x"+returnDispms.getX()+" y"+returnDispms.getY()+" w"+returnDispms.getWidth()+" h"+returnDispms.getHeight()+").bmp"));
        return new ByteAndRegion(changeImage(bufferedImage,styleNumber),returnDispms);
    }
    public static synchronized ByteResponse getByteResponse(Tag tag) throws IOException {
        List<Dispms> dispmses = (List<Dispms>)tag.getStyle().getDispmses();
        List<Dispms> dispmsesList = new ArrayList<>();
        Good good = tag.getGood();
        String regionNames = good.getRegionNames();
        boolean isRegion = !StringUtil.isEmpty(regionNames)?true:false;
        ByteResponse byteResponse;
        // 改价只更改区域
        if(isRegion){
            for(Dispms dispms:dispmses){
                if(dispms.getStatus()!=null && dispms.getStatus()==1 && regionNames.contains(dispms.getSourceColumn())) {
                    dispmsesList.add(dispms);
                }
            }
            log.info("区域:"+dispmsesList.size());
            byteResponse = SpringContextUtil.getRegionRequest(dispmsesList, tag.getStyle().getStyleNumber(), good);
        }
        else{
            for(int i=0 ;i<dispmses.size();i++) {
                Dispms dispms = dispmses.get(i);
                if ( dispms.getStatus() == 1)
                    dispmsesList.add(dispmses.get(i));
            }
            log.info("全局:"+dispmsesList.size());
            byteResponse = SpringContextUtil.getRequest(dispmsesList, tag.getStyle().getStyleNumber(), good);
        }
        return byteResponse;
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
    public static BufferedImage createBufferedImage(int width, int height) throws IOException {
        // 单色位图
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++)
                bufferedImage.setRGB(i, j, Color.WHITE.getRGB());
        }
        return bufferedImage;
    }
    public static BufferedImage createBackgroundImage(int width, int height) throws IOException {
        // 单色位图
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        for (int i = 0; i < height/2; i++) {
            for (int j = 0; j < width; j++)
                bufferedImage.setRGB(j, i, Color.BLACK.getRGB());
        }
        //后面画白色
        for (int i = height/2 ; i < height; i++) {
            for (int j = 0; j < width; j++)
                bufferedImage.setRGB(j, i, Color.WHITE.getRGB());
        }
        return bufferedImage;
    }
}

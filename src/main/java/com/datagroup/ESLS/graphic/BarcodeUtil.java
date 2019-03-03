package com.datagroup.ESLS.graphic;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.code128.EAN128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;


public class BarcodeUtil {

    /**
     * 生成文件
     *
     * @param msg
     * @return
     */
    public static byte[] generateFile(String msg) {
        return  generate(msg);
    }
    public static File generateFile(String msg, String fileName) {
        File file = new File(fileName);
        try {
            generate(msg, new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    public static byte[] generate(String msg)  {
        Code128Bean bean = new Code128Bean ();
        // 精细度
        final int dpi = 150;
        // module宽度
        final double moduleWidth = UnitConv.in2mm(1.0f / dpi);
        // 配置对象
        bean.setModuleWidth(moduleWidth);
        bean.doQuietZone(false);
        String format = "image/png";
        ByteArrayOutputStream ous = new ByteArrayOutputStream();
        // 输出到流
        BitmapCanvasProvider canvas = new BitmapCanvasProvider(ous, format, dpi,
                BufferedImage.TYPE_BYTE_BINARY, false, 0);
        // 生成条形码
        bean.generateBarcode(canvas, msg);
        // 结束绘制
        try {
            canvas.finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ous.toByteArray();
    }
    public static void generate(String msg,FileOutputStream ous)  {
        Code128Bean bean = new Code128Bean ();
        // 精细度
        final int dpi = 150;
        // module宽度
        final double moduleWidth = UnitConv.in2mm(1.0f / dpi);
        // 配置对象
        bean.setModuleWidth(moduleWidth);
        bean.doQuietZone(false);
        String format = "image/png";
        // 输出到流
        BitmapCanvasProvider canvas = new BitmapCanvasProvider(ous, format, dpi,
                BufferedImage.TYPE_BYTE_BINARY, false, 0);
        // 生成条形码
        bean.generateBarcode(canvas, msg);
        // 结束绘制
        try {
            canvas.finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void getBarCodeWithDigit(){

    }
}
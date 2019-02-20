package com.datagroup.ESLS.utils;

import com.csvreader.CsvReader;
import com.datagroup.ESLS.common.constant.SqlConstant;
import com.datagroup.ESLS.common.exception.ResultEnum;
import com.datagroup.ESLS.common.exception.TagServiceException;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PoiUtil {
    /**
     * CSV文件列分隔符
     */
    private static final String CSV_COLUMN_SEPARATOR = ",";

    /**
     * CSV文件行分隔符
     */
    private static final String CSV_RN = "\r\n";

    public static HSSFWorkbook exportData2Excel(List dataList, List columns, String tableName) {
        //1.创建Excel文档
        HSSFWorkbook workbook = new HSSFWorkbook();
        //创建Excel表单
        HSSFSheet sheet = workbook.createSheet(tableName + "信息表");
        //创建标题的显示样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        // 设置表头
        HSSFRow headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.size(); i++) {
            HSSFCell cell0 = headerRow.createCell(i);
            cell0.setCellValue(columns.get(i).toString());
            cell0.setCellStyle(headerStyle);
        }
        // 设置数据
        for (int i = 0; i < dataList.size(); i++) {
            Object data = dataList.get(i);
            HSSFRow row = sheet.createRow(i + 1);
            for (int j = 0; j < columns.size(); j++) {
                try {
                    Cell cell = row.createCell(j);
                    String column = columns.get(j).toString();
                    boolean flag = false;
                    // 列名以id结尾且不是主键
                    if (column.length()>2 && column.charAt(column.length() - 2) == 'i' && column.charAt(column.length() - 1) == 'd' && column.length() > 2) {
                        column = column.substring(0, column.length() - 2);
                        flag = true;
                    }
                    Field field = data.getClass().getDeclaredField(column);
                    //设置对象的访问权限，保证对private的属性的访问
                    field.setAccessible(true);
                    if (field.get(data) != null && flag) {
                        Object o = field.get(data);
                        Field fieldItem = o.getClass().getDeclaredField("id");
                        fieldItem.setAccessible(true);
                        // id数据
                        cell.setCellValue(fieldItem.get(o).toString());
                        continue;
                    }
                    cell.setCellValue(field.get(data) != null ? field.get(data).toString() : "null");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        setSizeColumn(sheet, columns.size());
        return workbook;
    }


    public static void exportData2Csv(List dataList, List columns, OutputStream os) {
        StringBuffer buf = new StringBuffer();
        for (Object item : columns) {
            buf.append(item.toString()).append(CSV_COLUMN_SEPARATOR);
        }
        buf.append(CSV_RN);
        try {
            // 设置数据
            for (int i = 0; i < dataList.size(); i++) {
                Object data = dataList.get(i);
                for (int j = 0; j < columns.size(); j++) {
                    String column = columns.get(j).toString();
                    boolean flag = false;
                    // 列名以id结尾且不是主键
                    if (column.charAt(column.length() - 2) == 'i' && column.charAt(column.length() - 1) == 'd' && column.length() > 2) {
                        column = column.substring(0, column.length() - 2);
                        flag = true;
                    }
                    Field field = data.getClass().getDeclaredField(column);
                    //设置对象的访问权限，保证对private的属性的访问
                    field.setAccessible(true);
                    if (field.get(data) != null && flag) {
                        Object o = field.get(data);
                        Field fieldItem = o.getClass().getDeclaredField("id");
                        fieldItem.setAccessible(true);
                        buf.append(fieldItem.get(o).toString()).append(CSV_COLUMN_SEPARATOR);
                        continue;
                    }
                    buf.append(field.get(data) != null ? field.get(data).toString() : "null").append(CSV_COLUMN_SEPARATOR);
                }
                buf.append(CSV_RN);
            }
            // 写出响应
            os.write(buf.toString().getBytes("UTF-8"));
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void importExcelDataFile(MultipartFile file, List dataColumnList,String tableName) {
        try {
            HSSFWorkbook workbook = new HSSFWorkbook(file.getInputStream());
            HSSFSheet sheet = workbook.getSheetAt(0);
            for(int r = 1;r<=sheet.getLastRowNum();r++){
                Row row = sheet.getRow(r);
                if(row == null) continue;
                saveEntity(dataColumnList,tableName,row);
            }
        } catch (Exception e) {
           throw new TagServiceException(ResultEnum.FILE_ERROR);
        }
    }

    public static void importCsvDataFile(MultipartFile file, List dataColumnList,String tableName) {
        try {
            Reader reader = new InputStreamReader(file.getInputStream());
            CsvReader csvReader = new CsvReader(reader);
            // 读表头
            csvReader.readHeaders();
            while(csvReader.readRecord()) {
                // 读一整行 System.out.println(csvReader.getRawRecord());
                // 读这行的某一列  System.out.println(csvReader.get("id"));
                saveEntity(dataColumnList,tableName,csvReader);
            }
        } catch (Exception e) {
            throw new TagServiceException(ResultEnum.FILE_ERROR);
        }
    }
    public static void saveEntity(List dataColumnList,String tableName,Row row) throws Exception {
            // 根据类名实例化实体类
            Class clazz = Class.forName("com.datagroup.ESLS.entity." + SqlConstant.EntityToSqlMap.get(tableName));
            Object o = clazz.newInstance();
            //属性赋值
            for (int j = 0; j < dataColumnList.size(); j++) {
                if(j==0) continue;
                String column = dataColumnList.get(j).toString();
                String objectColumn = column;
                boolean flag = false;
                // 列名以id结尾且不是主键
                if (column.charAt(column.length() - 2) == 'i' && column.charAt(column.length() - 1) == 'd' && column.length() > 2) {
                    objectColumn = column.substring(0, column.length() - 2);
                    flag = true;
                }
                Field field = clazz.getDeclaredField(objectColumn);
                field.setAccessible(true);
                if(field.getType().getName().contains("[B"))
                    continue;
                // 是对象里面的对象
                if (flag && !StringUtil.isEmpty(row.getCell(j).getStringCellValue())) {
                    Class goodClazz = Class.forName(field.getType().getName());
                    Object good = goodClazz.newInstance();
                    Field fieldItem = good.getClass().getDeclaredField("id");
                    fieldItem.setAccessible(true);
                    fieldItem.set(good,converAttributeValue(fieldItem.getType().getName(),String.valueOf(getCellValueByType(row.getCell(j)))));
                    field.set(o,good);
                }
                // 不是对象的对象
                else if(!flag)
                    field.set(o,converAttributeValue(field.getType().getName(),String.valueOf(getCellValueByType(row.getCell(j)))));
            }
            // 根据类名实例化service类进行存储数据库操作
            // Class clazz = Class.forName(className);
            // Object obj = clazz.newInstance(); 无效
            Object serviceObj = SpringContextUtil.getBean(SqlConstant.EntityToSqlMap.get(tableName) + "Service");
            // 得到方法对象,有参的方法需要指定参数类型
            Method saveOne = serviceObj.getClass().getMethod("saveOne", clazz);
            // 执行存储方法，有参传参 结果为返回值
            saveOne.invoke(serviceObj, o);
    }
    public static void saveEntity(List dataColumnList,String tableName,CsvReader csvReader) throws Exception {
            // 根据类名实例化实体类
            Class clazz = Class.forName("com.datagroup.ESLS.entity." + SqlConstant.EntityToSqlMap.get(tableName));
            Object o = clazz.newInstance();
            //属性赋值
            for (int j = 0; j < dataColumnList.size(); j++) {
                if(j==0) continue;
                String column = dataColumnList.get(j).toString();
                String objectColumn = column;
                boolean flag = false;
                // 列名以id结尾且不是主键
                if (column.charAt(column.length() - 2) == 'i' && column.charAt(column.length() - 1) == 'd' && column.length() > 2) {
                    objectColumn = column.substring(0, column.length() - 2);
                    flag = true;
                }
                Field field = clazz.getDeclaredField(objectColumn);
                field.setAccessible(true);
                // 为字节数组类型 跳过无法处理
                if(field.getType().getName().contains("[B"))
                    continue;
                // 是对象里面的对象
                if (flag && !StringUtil.isEmpty(csvReader.get(column))) {
                    Class goodClazz = Class.forName(field.getType().getName());
                    Object good = goodClazz.newInstance();
                    Field fieldItem = good.getClass().getDeclaredField("id");
                    fieldItem.setAccessible(true);
                    fieldItem.set(good,converAttributeValue(fieldItem.getType().getName(),csvReader.get(column)));
                    field.set(o,good);
                }
                // 不是对象的对象
                else if(!flag)
                    field.set(o,converAttributeValue(field.getType().getName(),csvReader.get(column)));
            }
            System.out.println(o.toString());
            Object serviceObj = SpringContextUtil.getBean(SqlConstant.EntityToSqlMap.get(tableName) + "Service");
            Method saveOne = serviceObj.getClass().getMethod("saveOne", clazz);
            saveOne.invoke(serviceObj, o);
    }
    private static void setSizeColumn(Sheet sheet, int size) {
        for (int columnNum = 0; columnNum < size; columnNum++) {
            int columnWidth = sheet.getColumnWidth(columnNum) / 256;
            for (int rowNum = 0; rowNum < sheet.getLastRowNum(); rowNum++) {
                Row currentRow;
                // 当前行未被使用过
                if (sheet.getRow(rowNum) == null) {
                    currentRow = sheet.createRow(rowNum);
                } else {
                    currentRow = sheet.getRow(rowNum);
                }
                if (currentRow.getCell(columnNum) != null) {
                    Cell currentCell = currentRow.getCell(columnNum);
                    if (currentCell.getCellType() == Cell.CELL_TYPE_STRING) {
                        int length = currentCell.getStringCellValue().getBytes().length;
                        if (columnWidth < length) {
                            columnWidth = length;
                        }
                    }
                }
            }
            // Excel的长度为字节码长度*256,*1.3为了处理数字格式
            columnWidth = (int) Math.floor(columnWidth * 256 * 1.3);
            //单元格长度大于20000的话也不美观,设置个最大长度
            columnWidth = columnWidth >= 20000 ? 20000 : columnWidth;
            //设置每列长度
            sheet.setColumnWidth(columnNum, columnWidth);
        }
    }

    public static void writeToResponse(HSSFWorkbook workbook, HttpServletRequest request,
                                       HttpServletResponse response, String fileName) {
        try {
            String userAgent = request.getHeader("User-Agent");
            // 解决中文乱码问题
            String fileName1 = fileName + "-Excel" + ".xlsx";
            String newFilename = URLEncoder.encode(fileName1, "UTF8");
            // 如果没有userAgent，则默认使用IE的方式进行编码，因为毕竟IE还是占多数的
            String rtn = "filename=\"" + newFilename + "\"";
            if (userAgent != null) {
                userAgent = userAgent.toLowerCase();
                // IE浏览器，只能采用URLEncoder编码
                if (userAgent.indexOf("IE") != -1) {
                    rtn = "filename=\"" + newFilename + "\"";
                }
                // Opera浏览器只能采用filename*
                else if (userAgent.indexOf("OPERA") != -1) {
                    rtn = "filename*=UTF-8''" + newFilename;
                }
                // Safari浏览器，只能采用ISO编码的中文输出
                else if (userAgent.indexOf("SAFARI") != -1) {
                    rtn = "filename=\"" + new String(fileName1.getBytes("UTF-8"), "ISO8859-1")
                            + "\"";
                }
                // FireFox浏览器，可以使用MimeUtility或filename*或ISO编码的中文输出
                else if (userAgent.indexOf("FIREFOX") != -1) {
                    rtn = "filename*=UTF-8''" + newFilename;
                }
            }
            String headStr = "attachment;  " + rtn;
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", headStr);
            // 响应到客户端
            OutputStream os = response.getOutputStream();
            workbook.write(os);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void responseSetProperties(String fileName, HttpServletResponse response) throws UnsupportedEncodingException {
        // 设置文件后缀
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String fn = fileName + "-" + sdf.format(new Date()) + ".csv";
        // 读取字符编码
        String utf = "UTF-8";
        // 设置响应
        response.setCharacterEncoding(utf);
        response.setHeader("Pragma", "public");
        response.setHeader("Cache-Control", "max-age=30");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fn, utf));
    }
    // 自动匹配类型
    public  static Object converAttributeValue(String type,String value){
//        System.out.println(type+" "+value);
        if("long".equals(type)|| Long.class.getTypeName().equals(type)){
            return Long.parseLong(StringUtil.isEmpty(value)?"0":value);
        }else if("double".equals(type)|| Double.class.getTypeName().equals(type)){
            return Double.parseDouble(StringUtil.isEmpty(value)?"0":value);
        }else if(Timestamp.class.getTypeName().equals(type)){
            if(StringUtil.isEmpty(value) || !value.contains("-"))
                return new Timestamp(System.currentTimeMillis());
            return Timestamp.valueOf(value);
        }else if("int".equals(type)|| Integer.class.getTypeName().equals(type)){
            return Integer.valueOf(StringUtil.isEmpty(value)?"0":value);
        }
        else if("byte".equals(type) || Byte.class.getTypeName().equals(type)){
            if(!StringUtil.isEmpty(value) && value.contains("."))
                value = value.substring(0,value.indexOf("."));
            return Byte.valueOf(value);
        }
        else{
            return value;
        }
    }
    private static Object getCellValueByType(Cell cell) {
        Object cellValue = "";
        if (null != cell) {
            switch (cell.getCellType()) {
                // 数字
                case HSSFCell.CELL_TYPE_NUMERIC:
                    //判断单元格的类型是否则NUMERIC类型
                    if (0 == cell.getCellType()) {
                        // 判断是否为日期类型
                        if (HSSFDateUtil.isCellDateFormatted(cell)) {
                            Date date = cell.getDateCellValue();
                            DateFormat formater = new SimpleDateFormat(
                                    "yyyy-MM-dd HH:mm");
                            cellValue = formater.format(date);
                        } else {
                            cellValue = cell.getNumericCellValue() + "";
                        }
                    }
                    break;
                // 字符串
                case HSSFCell.CELL_TYPE_STRING:
                    cellValue = cell.getStringCellValue();
                    break;
                // Boolean
                case HSSFCell.CELL_TYPE_BOOLEAN:
                    cellValue = cell.getBooleanCellValue() + "";
                    break;
                // 公式
                case HSSFCell.CELL_TYPE_FORMULA:
                    cellValue = cell.getCellFormula() + "";
                    break;
                // 空值
                case HSSFCell.CELL_TYPE_BLANK:
                    cellValue = "";
                    break;
                // 故障
                case HSSFCell.CELL_TYPE_ERROR:
                    cellValue = "非法字符";
                    break;
                default:
                    cellValue = "未知类型";
                    break;
            }
        }
        return cellValue;
    }
}


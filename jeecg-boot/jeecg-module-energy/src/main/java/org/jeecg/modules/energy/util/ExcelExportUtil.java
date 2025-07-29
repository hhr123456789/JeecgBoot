package org.jeecg.modules.energy.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Excel导出工具类
 * 用于动态列的Excel导出
 */
@Slf4j
public class ExcelExportUtil {

    /**
     * 导出实时数据到Excel
     *
     * @param response HTTP响应
     * @param fileName 文件名
     * @param headers 表头列表
     * @param dataList 数据列表，每行数据是一个Map，key为列标识，value为值
     */
    public static void exportRealTimeData(HttpServletResponse response, String fileName, 
                                        List<String> headers, List<Map<String, Object>> dataList) {
        
        log.info("开始导出Excel，文件名：{}，表头数量：{}，数据行数：{}", fileName, headers.size(), dataList.size());
        
        // 创建工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("实时数据");
        
        // 创建样式
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle timeStyle = createTimeStyle(workbook);
        
        // 创建表头
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
            
            // 设置列宽
            if (i == 0) {
                // 时间列宽度
                sheet.setColumnWidth(i, 20 * 256);
            } else {
                // 数据列宽度
                sheet.setColumnWidth(i, 15 * 256);
            }
        }
        
        // 填充数据
        for (int rowIndex = 0; rowIndex < dataList.size(); rowIndex++) {
            Row dataRow = sheet.createRow(rowIndex + 1);
            Map<String, Object> rowData = dataList.get(rowIndex);
            
            for (int colIndex = 0; colIndex < headers.size(); colIndex++) {
                Cell cell = dataRow.createCell(colIndex);
                String columnKey = "col_" + colIndex; // 列标识
                Object value = rowData.get(columnKey);
                
                if (value != null) {
                    if (colIndex == 0) {
                        // 时间列
                        cell.setCellValue(value.toString());
                        cell.setCellStyle(timeStyle);
                    } else {
                        // 数据列
                        if (value instanceof BigDecimal) {
                            cell.setCellValue(((BigDecimal) value).doubleValue());
                        } else if (value instanceof Number) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else {
                            cell.setCellValue(value.toString());
                        }
                        cell.setCellStyle(dataStyle);
                    }
                } else {
                    // 空值显示为"-"
                    cell.setCellValue("-");
                    cell.setCellStyle(dataStyle);
                }
            }
        }
        
        // 设置响应头
        try {
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + ".xlsx\"");
            response.setHeader("Cache-Control", "no-cache");
            
            // 输出到响应流
            OutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
            workbook.close();
            
            log.info("Excel导出成功，文件名：{}", fileName);
            
        } catch (IOException e) {
            log.error("Excel导出失败", e);
            throw new RuntimeException("Excel导出失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建表头样式
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 设置背景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // 设置字体
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        
        // 设置对齐
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }
    
    /**
     * 创建数据样式
     */
    private static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // 设置对齐
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // 设置数字格式（保留2位小数）
        DataFormat dataFormat = workbook.createDataFormat();
        style.setDataFormat(dataFormat.getFormat("0.00"));
        
        return style;
    }
    
    /**
     * 创建时间样式
     */
    private static CellStyle createTimeStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // 设置对齐
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }
    
    /**
     * 生成带时间戳的文件名
     */
    public static String generateFileName(String baseName) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return baseName + "_" + sdf.format(new Date());
    }
}

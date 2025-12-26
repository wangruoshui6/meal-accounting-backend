package com.accounting.service;

import com.accounting.entity.MealRecord;
import com.accounting.mapper.MealRecordMapper;
import com.accounting.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);

    @Autowired
    private MealRecordMapper mealRecordMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 导出用户所有餐饮记录为 Excel
     */
    public byte[] exportToExcel() throws IOException {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录");
        }

        // 查询用户所有记录，按日期升序
        QueryWrapper<MealRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUserId)
                   .orderByAsc("record_date");
        List<MealRecord> records = mealRecordMapper.selectList(queryWrapper);

        logger.info("开始导出数据，用户ID: {}, 记录数量: {}", currentUserId, records.size());

        // 创建 Excel 工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("餐饮记账记录");

        // 创建样式
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle moneyStyle = createMoneyStyle(workbook);

        // 创建表头
        int rowNum = 0;
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"日期", "早餐(¥)", "午餐(¥)", "晚餐(¥)", "零食(¥)", "饮料(¥)", "自定义项目", "总计(¥)"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 填充数据
        BigDecimal totalSum = BigDecimal.ZERO;
        for (MealRecord record : records) {
            Row row = sheet.createRow(rowNum++);
            int colNum = 0;

            // 日期
            Cell dateCell = row.createCell(colNum++);
            dateCell.setCellValue(record.getRecordDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            dateCell.setCellStyle(dataStyle);

            // 早餐
            Cell breakfastCell = row.createCell(colNum++);
            breakfastCell.setCellValue(record.getBreakfast() != null ? record.getBreakfast().doubleValue() : 0.0);
            breakfastCell.setCellStyle(moneyStyle);

            // 午餐
            Cell lunchCell = row.createCell(colNum++);
            lunchCell.setCellValue(record.getLunch() != null ? record.getLunch().doubleValue() : 0.0);
            lunchCell.setCellStyle(moneyStyle);

            // 晚餐
            Cell dinnerCell = row.createCell(colNum++);
            dinnerCell.setCellValue(record.getDinner() != null ? record.getDinner().doubleValue() : 0.0);
            dinnerCell.setCellStyle(moneyStyle);

            // 零食
            Cell snackCell = row.createCell(colNum++);
            snackCell.setCellValue(record.getSnack() != null ? record.getSnack().doubleValue() : 0.0);
            snackCell.setCellStyle(moneyStyle);

            // 饮料
            Cell drinkCell = row.createCell(colNum++);
            drinkCell.setCellValue(record.getDrink() != null ? record.getDrink().doubleValue() : 0.0);
            drinkCell.setCellStyle(moneyStyle);

            // 自定义项目（格式化为字符串）
            Cell customCell = row.createCell(colNum++);
            String customItemsStr = formatCustomItems(record.getCustomItems());
            customCell.setCellValue(customItemsStr);
            customCell.setCellStyle(dataStyle);

            // 总计
            Cell totalCell = row.createCell(colNum++);
            BigDecimal total = record.getTotal() != null ? record.getTotal() : BigDecimal.ZERO;
            totalCell.setCellValue(total.doubleValue());
            totalCell.setCellStyle(moneyStyle);
            totalSum = totalSum.add(total);
        }

        // 添加汇总行
        if (records.size() > 0) {
            Row summaryRow = sheet.createRow(rowNum++);
            Cell summaryLabelCell = summaryRow.createCell(0);
            summaryLabelCell.setCellValue("合计");
            summaryLabelCell.setCellStyle(headerStyle);

            // 合并单元格
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 6));

            Cell summaryTotalCell = summaryRow.createCell(7);
            summaryTotalCell.setCellValue(totalSum.doubleValue());
            CellStyle summaryStyle = createSummaryStyle(workbook);
            summaryTotalCell.setCellStyle(summaryStyle);
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            // 设置最小列宽
            if (sheet.getColumnWidth(i) < 2000) {
                sheet.setColumnWidth(i, 2000);
            }
        }

        // 将工作簿写入字节数组
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        logger.info("Excel 导出完成，总记录数: {}, 总金额: {}", records.size(), totalSum);
        return outputStream.toByteArray();
    }

    /**
     * 格式化自定义项目为字符串
     */
    private String formatCustomItems(String customItemsJson) {
        if (customItemsJson == null || customItemsJson.trim().isEmpty()) {
            return "";
        }

        try {
            Map<String, BigDecimal> customItems = objectMapper.readValue(
                customItemsJson,
                new TypeReference<Map<String, BigDecimal>>() {}
            );

            if (customItems == null || customItems.isEmpty()) {
                return "";
            }

            List<String> items = new ArrayList<>();
            for (Map.Entry<String, BigDecimal> entry : customItems.entrySet()) {
                items.add(entry.getKey() + ": ¥" + entry.getValue());
            }
            return String.join("; ", items);
        } catch (Exception e) {
            logger.warn("解析自定义项目失败: {}", e.getMessage());
            return customItemsJson;
        }
    }

    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 创建金额样式
     */
    private CellStyle createMoneyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 创建汇总样式
     */
    private CellStyle createSummaryStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.RED.getIndex());
        style.setFont(font);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}


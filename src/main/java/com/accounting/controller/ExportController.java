package com.accounting.controller;

import com.accounting.service.ExportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/export")
@CrossOrigin(origins = "*")
public class ExportController {

    private static final Logger logger = LoggerFactory.getLogger(ExportController.class);

    @Autowired
    private ExportService exportService;

    /**
     * 导出餐饮记录为 Excel
     */
    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportToExcel() {
        logger.info("收到导出 Excel 请求");

        try {
            byte[] excelBytes = exportService.exportToExcel();

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            String fileName = "餐饮记账记录_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(excelBytes.length);

            logger.info("Excel 导出成功，文件名: {}, 大小: {} bytes", fileName, excelBytes.length);
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);

        } catch (RuntimeException e) {
            logger.error("导出失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IOException e) {
            logger.error("生成 Excel 文件失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            logger.error("导出过程中发生未知错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}


package com.splitwise.orderanalytics.controller;

import com.splitwise.orderanalytics.dto.OrderStatistics;
import com.splitwise.orderanalytics.entity.Order;
import com.splitwise.orderanalytics.service.FileUploadService;
import com.splitwise.orderanalytics.service.OrderAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Controller
public class OrderController {

    @Autowired
    private OrderAnalysisService orderAnalysisService;

    @Autowired
    private FileUploadService fileUploadService;

    @GetMapping("/")
    public String index(Model model) {
        Map<String, Object> dashboardData = orderAnalysisService.getDashboardData();
        model.addAttribute("dashboardData", dashboardData);
        return "index";
    }

    @GetMapping("/api/statistics")
    @ResponseBody
    public ResponseEntity<OrderStatistics> getStatistics() {
        OrderStatistics statistics = orderAnalysisService.getOverallStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/api/orders")
    @ResponseBody
    public ResponseEntity<List<Order>> getOrders(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String region) {
        
        List<Order> orders = orderAnalysisService.getFilteredOrders(
            startDate, endDate, status, productName, customerName, category, region);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/api/orders/{id}")
    @ResponseBody
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order updatedOrder) {
        try {
            Order savedOrder = orderAnalysisService.updateOrder(id, updatedOrder);
            return ResponseEntity.ok(savedOrder);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/api/orders/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        if (!orderAnalysisService.deleteOrder(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng chọn file để tải lên");
                return ResponseEntity.badRequest().body(response);
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null) {
                response.put("success", false);
                response.put("message", "Tên file không hợp lệ");
                return ResponseEntity.badRequest().body(response);
            }

            List<Order> orders;
            if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
                orders = fileUploadService.parseExcelFile(file);
            } else if (fileName.endsWith(".csv")) {
                try {
                    orders = fileUploadService.parseCsvFile(file);
                } catch (com.opencsv.exceptions.CsvValidationException e) {
                    response.put("success", false);
                    response.put("message", "Lỗi định dạng CSV: " + e.getMessage());
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                response.put("success", false);
                response.put("message", "Chỉ hỗ trợ file Excel (.xlsx, .xls) hoặc CSV (.csv)");
                return ResponseEntity.badRequest().body(response);
            }

            if (orders.isEmpty()) {
                response.put("success", false);
                response.put("message", "Không tìm thấy dữ liệu hợp lệ trong file");
                return ResponseEntity.badRequest().body(response);
            }

            fileUploadService.saveOrders(orders);

            response.put("success", true);
            response.put("message", "Tải lên và xử lý file thành công");
            response.put("ordersCount", orders.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi xử lý file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/api/dashboard")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> dashboardData = orderAnalysisService.getDashboardData();
        return ResponseEntity.ok(dashboardData);
    }

    @GetMapping("/orders")
    public String ordersPage(Model model) {
        return "orders";
    }

    @GetMapping("/upload")
    public String uploadPage() {
        return "upload";
    }
}

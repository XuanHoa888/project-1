package com.splitwise.orderanalytics.service;

import com.splitwise.orderanalytics.entity.Order;
import com.splitwise.orderanalytics.entity.OrderStatus;
import com.splitwise.orderanalytics.entity.Product;
import com.splitwise.orderanalytics.repository.OrderRepository;
import com.splitwise.orderanalytics.repository.ProductRepository;
import java.util.Optional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileUploadService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy")
    };

    public List<Order> parseExcelFile(MultipartFile file) throws IOException {
        List<Order> orders = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row))
                    continue;

                Order order = mapRowToOrder(row);
                if (order != null) {
                    orders.add(order);
                }
            }
        }

        return orders;
    }

    public List<Order> parseCsvFile(MultipartFile file)
            throws IOException, com.opencsv.exceptions.CsvValidationException {
        List<Order> orders = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream()) {
            com.opencsv.CSVReader reader = new com.opencsv.CSVReader(new java.io.InputStreamReader(inputStream));

            String[] headers = reader.readNext();

            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length == 0 || isEmptyRow(row))
                    continue;

                Order order = mapCsvRowToOrder(row, headers);
                if (order != null) {
                    orders.add(order);
                }
            }
        }

        return orders;
    }

    public void saveOrders(List<Order> orders) {
        for (Order order : orders) {
            updateProductStock(order);
        }
        orderRepository.saveAll(orders);
    }

    private void updateProductStock(Order order) {
        if (order.getProductName() == null)
            return;

        Optional<Product> productOpt = productRepository.findByName(order.getProductName());
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            int quantity = order.getQuantity() != null ? order.getQuantity() : 0;

            // If order is Returned or Cancelled, we add stock back
            if (order.getStatus() == OrderStatus.RETURNED || order.getStatus() == OrderStatus.CANCELLED) {
                product.setStockQuantity(product.getStockQuantity() + quantity);
            }
            // Otherwise (Pending, Confirmed, Shipped, Delivered), we deduct stock
            else {
                product.setStockQuantity(product.getStockQuantity() - quantity);
            }
            productRepository.save(product);
        }
    }

    private Order mapRowToOrder(Row row) {
        try {
            String orderCode = getCellValueAsString(row.getCell(0));
            String customerName = getCellValueAsString(row.getCell(1));
            String customerEmail = getCellValueAsString(row.getCell(2));
            String productName = getCellValueAsString(row.getCell(3));
            Integer quantity = getCellValueAsInteger(row.getCell(4));
            BigDecimal unitPrice = getCellValueAsBigDecimal(row.getCell(5));
            BigDecimal totalAmount = getCellValueAsBigDecimal(row.getCell(6));
            OrderStatus status = parseOrderStatus(getCellValueAsString(row.getCell(7)));
            LocalDateTime orderDate = parseDate(getCellValueAsString(row.getCell(8)));
            String category = getCellValueAsString(row.getCell(9));
            String region = getCellValueAsString(row.getCell(10));

            if (orderCode == null || customerName == null || productName == null ||
                    quantity == null || unitPrice == null || totalAmount == null || status == null) {
                return null;
            }

            return new Order(orderCode, customerName, customerEmail, productName,
                    quantity, unitPrice, totalAmount, status, orderDate, category, region);
        } catch (Exception e) {
            System.err.println("Error mapping row to order: " + e.getMessage());
            return null;
        }
    }

    private Order mapCsvRowToOrder(String[] row, String[] headers) {
        try {
            String orderCode = getValueByHeader(row, headers, "orderCode", "Mã đơn hàng");
            String customerName = getValueByHeader(row, headers, "customerName", "Tên khách hàng");
            String customerEmail = getValueByHeader(row, headers, "customerEmail", "Email");
            String productName = getValueByHeader(row, headers, "productName", "Sản phẩm");
            Integer quantity = getIntegerValueByHeader(row, headers, "quantity", "Số lượng");
            BigDecimal unitPrice = getBigDecimalValueByHeader(row, headers, "unitPrice", "Đơn giá");
            BigDecimal totalAmount = getBigDecimalValueByHeader(row, headers, "totalAmount", "Tổng tiền");
            OrderStatus status = parseOrderStatus(getValueByHeader(row, headers, "status", "Trạng thái"));
            LocalDateTime orderDate = parseDate(getValueByHeader(row, headers, "orderDate", "Ngày đặt hàng"));
            String category = getValueByHeader(row, headers, "category", "Danh mục");
            String region = getValueByHeader(row, headers, "region", "Khu vực");

            if (orderCode == null || customerName == null || productName == null ||
                    quantity == null || unitPrice == null || totalAmount == null || status == null) {
                return null;
            }

            return new Order(orderCode, customerName, customerEmail, productName,
                    quantity, unitPrice, totalAmount, status, orderDate, category, region);
        } catch (Exception e) {
            System.err.println("Error mapping CSV row to order: " + e.getMessage());
            return null;
        }
    }

    private String getValueByHeader(String[] row, String[] headers, String... possibleHeaders) {
        for (String header : possibleHeaders) {
            for (int i = 0; i < headers.length; i++) {
                if (headers[i] != null && headers[i].toLowerCase().contains(header.toLowerCase())) {
                    return row.length > i ? row[i] : null;
                }
            }
        }
        return null;
    }

    private Integer getIntegerValueByHeader(String[] row, String[] headers, String... possibleHeaders) {
        String value = getValueByHeader(row, headers, possibleHeaders);
        return value != null && !value.isEmpty() ? Integer.parseInt(value.trim()) : null;
    }

    private BigDecimal getBigDecimalValueByHeader(String[] row, String[] headers, String... possibleHeaders) {
        String value = getValueByHeader(row, headers, possibleHeaders);
        return value != null && !value.isEmpty() ? new BigDecimal(value.replaceAll("[,₫]", "").trim()) : null;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null)
            return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null)
            return null;

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return (int) cell.getNumericCellValue();
                case STRING:
                    String str = cell.getStringCellValue().trim();
                    return str.isEmpty() ? null : Integer.parseInt(str.replaceAll("[,]", ""));
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null)
            return null;

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return BigDecimal.valueOf(cell.getNumericCellValue());
                case STRING:
                    String str = cell.getStringCellValue().trim();
                    return str.isEmpty() ? null : new BigDecimal(str.replaceAll("[,₫]", ""));
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private OrderStatus parseOrderStatus(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty())
            return null;

        statusStr = statusStr.trim().toLowerCase();

        for (OrderStatus status : OrderStatus.values()) {
            if (status.name().toLowerCase().equals(statusStr) ||
                    status.getDisplayName().toLowerCase().equals(statusStr)) {
                return status;
            }
        }

        if (statusStr.contains("chờ") || statusStr.contains("pending"))
            return OrderStatus.PENDING;
        if (statusStr.contains("xác nhận") || statusStr.contains("confirmed"))
            return OrderStatus.CONFIRMED;
        if (statusStr.contains("giao") || statusStr.contains("shipped"))
            return OrderStatus.SHIPPED;
        if (statusStr.contains("đã giao") || statusStr.contains("delivered"))
            return OrderStatus.DELIVERED;
        if (statusStr.contains("hủy") || statusStr.contains("cancelled"))
            return OrderStatus.CANCELLED;
        if (statusStr.contains("trả") || statusStr.contains("returned"))
            return OrderStatus.RETURNED;

        return null;
    }

    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty())
            return LocalDateTime.now();

        dateStr = dateStr.trim();

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                if (formatter.toString().contains("HH:mm")) {
                    return LocalDateTime.parse(dateStr, formatter);
                } else {
                    return java.time.LocalDate.parse(dateStr, formatter).atStartOfDay();
                }
            } catch (Exception e) {
                continue;
            }
        }

        return LocalDateTime.now();
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isEmptyRow(String[] row) {
        for (String cell : row) {
            if (cell != null && !cell.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}

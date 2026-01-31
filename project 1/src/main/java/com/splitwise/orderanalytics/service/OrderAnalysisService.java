package com.splitwise.orderanalytics.service;

import com.splitwise.orderanalytics.dto.OrderStatistics;
import com.splitwise.orderanalytics.entity.Order;
import com.splitwise.orderanalytics.entity.OrderStatus;
import com.splitwise.orderanalytics.repository.OrderRepository;
import com.splitwise.orderanalytics.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderAnalysisService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    private boolean isStockDeducted(OrderStatus status) {
        return status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED;
    }

    private void updateProductStock(String productName, int quantityChange) {
        if (quantityChange == 0)
            return;
        productRepository.findByName(productName).ifPresent(product -> {
            product.setStockQuantity(product.getStockQuantity() + quantityChange);
            productRepository.save(product);
        });
    }

    public OrderStatistics getOverallStatistics() {
        Long totalOrders = orderRepository.getTotalOrders();
        if (totalOrders == null)
            totalOrders = 0L;

        BigDecimal totalRevenue = orderRepository.getTotalRevenue();
        if (totalRevenue == null)
            totalRevenue = BigDecimal.ZERO;

        BigDecimal averageOrderValue = orderRepository.getAverageOrderValue();
        if (averageOrderValue == null)
            averageOrderValue = BigDecimal.ZERO;

        Map<String, Long> ordersByStatus = getOrdersByStatus();
        Map<String, BigDecimal> revenueByStatus = getRevenueByStatus();
        List<OrderStatistics.TopProduct> topProducts = getTopProducts();
        List<OrderStatistics.TopCustomer> topCustomers = getTopCustomers();
        List<OrderStatistics.DailyRevenue> dailyRevenue = getDailyRevenue();
        List<OrderStatistics.MonthlyRevenue> monthlyRevenue = getMonthlyRevenue();

        return new OrderStatistics(totalOrders, totalRevenue, averageOrderValue,
                ordersByStatus, revenueByStatus, topProducts,
                topCustomers, dailyRevenue, monthlyRevenue);
    }

    public List<Order> getFilteredOrders(String startDate, String endDate, String status,
            String productName, String customerName,
            String category, String region) {
        LocalDateTime start = startDate != null && !startDate.isEmpty() ? LocalDateTime.parse(startDate + "T00:00:00")
                : null;
        LocalDateTime end = endDate != null && !endDate.isEmpty() ? LocalDateTime.parse(endDate + "T23:59:59") : null;

        List<Order> orders = orderRepository.findAll();

        if (start != null && end != null) {
            orders = orders.stream()
                    .filter(order -> !order.getOrderDate().isBefore(start) && !order.getOrderDate().isAfter(end))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.isEmpty()) {
            try {
                OrderStatus statusEnum = OrderStatus.valueOf(status.toUpperCase());
                orders = orders.stream()
                        .filter(order -> order.getStatus() == statusEnum)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore filter
            }
        }

        if (productName != null && !productName.isEmpty()) {
            orders = orders.stream()
                    .filter(order -> order.getProductName().toLowerCase().contains(productName.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (customerName != null && !customerName.isEmpty()) {
            orders = orders.stream()
                    .filter(order -> order.getCustomerName().toLowerCase().contains(customerName.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (category != null && !category.isEmpty()) {
            orders = orders.stream()
                    .filter(order -> order.getCategory() != null &&
                            order.getCategory().toLowerCase().contains(category.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (region != null && !region.isEmpty()) {
            orders = orders.stream()
                    .filter(order -> order.getRegion() != null &&
                            order.getRegion().toLowerCase().contains(region.toLowerCase()))
                    .collect(Collectors.toList());
        }

        return orders;
    }

    private Map<String, Long> getOrdersByStatus() {
        Map<String, Long> result = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            Long count = orderRepository.countByStatus(status);
            result.put(status.getDisplayName(), count != null ? count : 0L);
        }
        return result;
    }

    private Map<String, BigDecimal> getRevenueByStatus() {
        Map<String, BigDecimal> result = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            BigDecimal revenue = orderRepository.sumTotalAmountByStatus(status);
            result.put(status.getDisplayName(), revenue != null ? revenue : BigDecimal.ZERO);
        }
        return result;
    }

    private List<OrderStatistics.TopProduct> getTopProducts() {
        List<Object[]> results = orderRepository.findTopSellingProducts();
        return results.stream()
                .limit(10)
                .map(result -> new OrderStatistics.TopProduct(
                        (String) result[0],
                        ((Number) result[1]).longValue()))
                .collect(Collectors.toList());
    }

    private List<OrderStatistics.TopCustomer> getTopCustomers() {
        List<Object[]> results = orderRepository.findTopCustomersBySpending();
        return results.stream()
                .limit(10)
                .map(result -> new OrderStatistics.TopCustomer(
                        (String) result[0],
                        (BigDecimal) result[1]))
                .collect(Collectors.toList());
    }

    private List<OrderStatistics.DailyRevenue> getDailyRevenue() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30);

        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        Map<LocalDate, BigDecimal> totals = new HashMap<>();
        for (Order order : orders) {
            LocalDate date = order.getOrderDate().toLocalDate();
            totals.merge(date, order.getTotalAmount(), BigDecimal::add);
        }

        return totals.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new OrderStatistics.DailyRevenue(
                        entry.getKey().format(DateTimeFormatter.ISO_LOCAL_DATE),
                        entry.getValue()))
                .collect(Collectors.toList());
    }

    private List<OrderStatistics.MonthlyRevenue> getMonthlyRevenue() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(12);

        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        Map<YearMonth, BigDecimal> totals = new HashMap<>();
        for (Order order : orders) {
            YearMonth month = YearMonth.from(order.getOrderDate());
            totals.merge(month, order.getTotalAmount(), BigDecimal::add);
        }

        return totals.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new OrderStatistics.MonthlyRevenue(
                        entry.getKey().toString(),
                        entry.getValue()))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();

        OrderStatistics stats = getOverallStatistics();
        dashboardData.put("statistics", stats);

        List<Order> recentOrders = orderRepository.findAll();
        recentOrders.sort((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()));
        dashboardData.put("recentOrders", recentOrders.stream().limit(10).collect(Collectors.toList()));

        return dashboardData;
    }

    public Order updateOrder(Long id, Order updatedOrder) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found"));

        // Capture old state for inventory tracking
        OrderStatus oldStatus = existingOrder.getStatus();
        String oldProductName = existingOrder.getProductName();
        Integer oldQuantity = existingOrder.getQuantity();

        if (updatedOrder.getOrderCode() != null) {
            existingOrder.setOrderCode(updatedOrder.getOrderCode());
        }
        if (updatedOrder.getCustomerName() != null) {
            existingOrder.setCustomerName(updatedOrder.getCustomerName());
        }
        if (updatedOrder.getCustomerEmail() != null) {
            existingOrder.setCustomerEmail(updatedOrder.getCustomerEmail());
        }
        if (updatedOrder.getProductName() != null) {
            existingOrder.setProductName(updatedOrder.getProductName());
        }
        if (updatedOrder.getQuantity() != null) {
            existingOrder.setQuantity(updatedOrder.getQuantity());
        }
        if (updatedOrder.getUnitPrice() != null) {
            existingOrder.setUnitPrice(updatedOrder.getUnitPrice());
        }
        if (updatedOrder.getStatus() != null) {
            existingOrder.setStatus(updatedOrder.getStatus());
        }
        if (updatedOrder.getOrderDate() != null) {
            existingOrder.setOrderDate(updatedOrder.getOrderDate());
        }
        if (updatedOrder.getCategory() != null) {
            existingOrder.setCategory(updatedOrder.getCategory());
        }
        if (updatedOrder.getRegion() != null) {
            existingOrder.setRegion(updatedOrder.getRegion());
        }

        if (existingOrder.getUnitPrice() != null && existingOrder.getQuantity() != null) {
            existingOrder.setTotalAmount(existingOrder.getUnitPrice()
                    .multiply(BigDecimal.valueOf(existingOrder.getQuantity())));
        }

        // Logic to update inventory
        boolean isStockDeductedOld = isStockDeducted(oldStatus);
        boolean isStockDeductedNew = isStockDeducted(existingOrder.getStatus());

        if (!Objects.equals(oldProductName, existingOrder.getProductName())) {
            // Product changed
            if (isStockDeductedOld) {
                updateProductStock(oldProductName, oldQuantity);
            }
            if (isStockDeductedNew) {
                updateProductStock(existingOrder.getProductName(), -existingOrder.getQuantity());
            }
        } else {
            // Product same
            int stockChange = 0;
            if (isStockDeductedOld) {
                stockChange += oldQuantity;
            }
            if (isStockDeductedNew) {
                stockChange -= existingOrder.getQuantity();
            }
            updateProductStock(existingOrder.getProductName(), stockChange);
        }

        return orderRepository.save(existingOrder);
    }

    public boolean deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            return false;
        }
        orderRepository.deleteById(id);
        return true;
    }
}

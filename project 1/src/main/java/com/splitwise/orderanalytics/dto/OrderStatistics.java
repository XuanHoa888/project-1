package com.splitwise.orderanalytics.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class OrderStatistics {
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private Map<String, Long> ordersByStatus;
    private Map<String, BigDecimal> revenueByStatus;
    private List<TopProduct> topProducts;
    private List<TopCustomer> topCustomers;
    private List<DailyRevenue> dailyRevenue;
    private List<MonthlyRevenue> monthlyRevenue;

    public OrderStatistics() {}

    public OrderStatistics(Long totalOrders, BigDecimal totalRevenue, BigDecimal averageOrderValue,
                           Map<String, Long> ordersByStatus, Map<String, BigDecimal> revenueByStatus,
                           List<TopProduct> topProducts, List<TopCustomer> topCustomers,
                           List<DailyRevenue> dailyRevenue, List<MonthlyRevenue> monthlyRevenue) {
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
        this.averageOrderValue = averageOrderValue;
        this.ordersByStatus = ordersByStatus;
        this.revenueByStatus = revenueByStatus;
        this.topProducts = topProducts;
        this.topCustomers = topCustomers;
        this.dailyRevenue = dailyRevenue;
        this.monthlyRevenue = monthlyRevenue;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public Map<String, Long> getOrdersByStatus() {
        return ordersByStatus;
    }

    public void setOrdersByStatus(Map<String, Long> ordersByStatus) {
        this.ordersByStatus = ordersByStatus;
    }

    public Map<String, BigDecimal> getRevenueByStatus() {
        return revenueByStatus;
    }

    public void setRevenueByStatus(Map<String, BigDecimal> revenueByStatus) {
        this.revenueByStatus = revenueByStatus;
    }

    public List<TopProduct> getTopProducts() {
        return topProducts;
    }

    public void setTopProducts(List<TopProduct> topProducts) {
        this.topProducts = topProducts;
    }

    public List<TopCustomer> getTopCustomers() {
        return topCustomers;
    }

    public void setTopCustomers(List<TopCustomer> topCustomers) {
        this.topCustomers = topCustomers;
    }

    public List<DailyRevenue> getDailyRevenue() {
        return dailyRevenue;
    }

    public void setDailyRevenue(List<DailyRevenue> dailyRevenue) {
        this.dailyRevenue = dailyRevenue;
    }

    public List<MonthlyRevenue> getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public void setMonthlyRevenue(List<MonthlyRevenue> monthlyRevenue) {
        this.monthlyRevenue = monthlyRevenue;
    }

    public static class TopProduct {
        private String productName;
        private Long totalQuantity;

        public TopProduct() {}

        public TopProduct(String productName, Long totalQuantity) {
            this.productName = productName;
            this.totalQuantity = totalQuantity;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public Long getTotalQuantity() {
            return totalQuantity;
        }

        public void setTotalQuantity(Long totalQuantity) {
            this.totalQuantity = totalQuantity;
        }
    }

    public static class TopCustomer {
        private String customerName;
        private BigDecimal totalSpent;

        public TopCustomer() {}

        public TopCustomer(String customerName, BigDecimal totalSpent) {
            this.customerName = customerName;
            this.totalSpent = totalSpent;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public BigDecimal getTotalSpent() {
            return totalSpent;
        }

        public void setTotalSpent(BigDecimal totalSpent) {
            this.totalSpent = totalSpent;
        }
    }

    public static class DailyRevenue {
        private String date;
        private BigDecimal totalRevenue;

        public DailyRevenue() {}

        public DailyRevenue(String date, BigDecimal totalRevenue) {
            this.date = date;
            this.totalRevenue = totalRevenue;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public BigDecimal getTotalRevenue() {
            return totalRevenue;
        }

        public void setTotalRevenue(BigDecimal totalRevenue) {
            this.totalRevenue = totalRevenue;
        }
    }

    public static class MonthlyRevenue {
        private String month;
        private BigDecimal totalRevenue;

        public MonthlyRevenue() {}

        public MonthlyRevenue(String month, BigDecimal totalRevenue) {
            this.month = month;
            this.totalRevenue = totalRevenue;
        }

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }

        public BigDecimal getTotalRevenue() {
            return totalRevenue;
        }

        public void setTotalRevenue(BigDecimal totalRevenue) {
            this.totalRevenue = totalRevenue;
        }
    }
}

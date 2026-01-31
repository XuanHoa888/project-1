package com.splitwise.orderanalytics.entity;

public enum OrderStatus {
    PENDING("Chờ xử lý"),
    CONFIRMED("Đã xác nhận"),
    SHIPPED("Đang giao"),
    DELIVERED("Đã giao"),
    CANCELLED("Đã hủy"),
    RETURNED("Đã trả hàng");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

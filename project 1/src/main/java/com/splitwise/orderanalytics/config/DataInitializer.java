package com.splitwise.orderanalytics.config;

import com.splitwise.orderanalytics.entity.Order;
import com.splitwise.orderanalytics.entity.OrderStatus;
import com.splitwise.orderanalytics.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public void run(String... args) throws Exception {
        // Tạo dữ liệu mẫu nếu database trống
        try {
            if (orderRepository.count() == 0) {
                createSampleData();
                System.out.println("Đã tạo dữ liệu mẫu thành công!");
            }
        } catch (Exception e) {
            System.err.println("Không thể tạo dữ liệu mẫu: " + e.getMessage());
        }
    }

    private void createSampleData() {
        // Dữ liệu mẫu để test
        Order[] sampleOrders = {
        };

        for (Order order : sampleOrders) {
            orderRepository.save(order);
        }
    }
}

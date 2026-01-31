package com.splitwise.orderanalytics.repository;

import com.splitwise.orderanalytics.entity.Order;
import com.splitwise.orderanalytics.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);
    
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Order> findByCustomerNameContainingIgnoreCase(String customerName);
    
    List<Order> findByProductNameContainingIgnoreCase(String productName);
    
    List<Order> findByCategoryContainingIgnoreCase(String category);
    
    List<Order> findByRegionContainingIgnoreCase(String region);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") OrderStatus status);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = :status")
    BigDecimal sumTotalAmountByStatus(@Param("status") OrderStatus status);
    
    @Query("SELECT o.productName, SUM(o.quantity) as totalQuantity FROM Order o GROUP BY o.productName ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProducts();
    
    @Query("SELECT o.customerName, SUM(o.totalAmount) as totalSpent FROM Order o GROUP BY o.customerName ORDER BY totalSpent DESC")
    List<Object[]> findTopCustomersBySpending();
    
    @Query("SELECT FORMATDATETIME(o.orderDate, 'yyyy-MM-dd'), SUM(o.totalAmount) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate GROUP BY FORMATDATETIME(o.orderDate, 'yyyy-MM-dd') ORDER BY FORMATDATETIME(o.orderDate, 'yyyy-MM-dd')")
    List<Object[]> findDailyRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT YEAR(o.orderDate), MONTH(o.orderDate), SUM(o.totalAmount) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate GROUP BY YEAR(o.orderDate), MONTH(o.orderDate) ORDER BY YEAR(o.orderDate), MONTH(o.orderDate)")
    List<Object[]> findMonthlyRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(o) FROM Order o")
    Long getTotalOrders();
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o")
    BigDecimal getTotalRevenue();
    
    @Query("SELECT AVG(o.totalAmount) FROM Order o")
    BigDecimal getAverageOrderValue();
}

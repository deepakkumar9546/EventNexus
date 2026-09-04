package com.eventbooking.payment.repository;

import com.eventbooking.payment.entity.Order;
import com.eventbooking.payment.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    Page<Order> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    
    List<Order> findByUserIdAndEventId(UUID userId, UUID eventId);
    
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.paymentStatus = :status ORDER BY o.createdAt DESC")
    Page<Order> findByUserIdAndStatus(@Param("userId") UUID userId, 
                                       @Param("status") PaymentStatus status, 
                                       Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.paymentStatus = :status AND o.expiresAt < :now")
    List<Order> findExpiredOrders(@Param("status") PaymentStatus status, 
                                   @Param("now") Instant now);
    
    boolean existsByOrderNumber(String orderNumber);
}

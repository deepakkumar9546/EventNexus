package com.eventbooking.payment.mapper;

import com.eventbooking.payment.dto.OrderDto;
import com.eventbooking.payment.dto.OrderItemDto;
import com.eventbooking.payment.entity.Order;
import com.eventbooking.payment.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {
    
    public OrderDto toDto(Order order) {
        if (order == null) {
            return null;
        }
        
        return OrderDto.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .eventId(order.getEventId())
                .orderNumber(order.getOrderNumber())
                .subtotalAmount(order.getSubtotalAmount())
                .serviceFee(order.getServiceFee())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .currency(order.getCurrency())
                .reservationId(order.getReservationId())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .expiresAt(order.getExpiresAt())
                .orderItems(toOrderItemDtoList(order.getOrderItems()))
                .build();
    }
    
    public List<OrderDto> toDtoList(List<Order> orders) {
        if (orders == null) {
            return null;
        }
        
        return orders.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    public OrderItemDto toOrderItemDto(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }
        
        return OrderItemDto.builder()
                .id(orderItem.getId())
                .ticketTypeId(orderItem.getTicketTypeId())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .subtotal(orderItem.getSubtotal())
                .fees(orderItem.getFees())
                .totalPrice(orderItem.getTotalPrice())
                .status(orderItem.getStatus())
                .createdAt(orderItem.getCreatedAt())
                .build();
    }
    
    public List<OrderItemDto> toOrderItemDtoList(List<OrderItem> orderItems) {
        if (orderItems == null) {
            return null;
        }
        
        return orderItems.stream()
                .map(this::toOrderItemDto)
                .collect(Collectors.toList());
    }
}

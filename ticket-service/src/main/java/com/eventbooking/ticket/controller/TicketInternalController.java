package com.eventbooking.ticket.controller;

import com.eventbooking.common.dto.ApiResponse;
import com.eventbooking.ticket.dto.GenerateTicketsRequest;
import com.eventbooking.ticket.dto.TicketDto;
import com.eventbooking.ticket.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/internal")
public class TicketInternalController {

    private final TicketService ticketService;

    public TicketInternalController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<List<TicketDto>>> generateTickets(
            @Valid @RequestBody GenerateTicketsRequest request) {

        List<TicketDto> tickets = ticketService.generateTickets(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tickets generated successfully", tickets));
    }
}
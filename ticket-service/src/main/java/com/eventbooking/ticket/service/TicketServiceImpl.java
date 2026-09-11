package com.eventbooking.ticket.service;

import com.eventbooking.ticket.dto.GenerateTicketsRequest;
import com.eventbooking.ticket.dto.TicketDto;
import com.eventbooking.ticket.entity.Ticket;
import com.eventbooking.ticket.entity.TicketType;
import com.eventbooking.ticket.exception.TicketNotFoundException;
import com.eventbooking.ticket.exception.TicketTypeNotFoundException;
import com.eventbooking.ticket.mapper.TicketMapper;
import com.eventbooking.ticket.repository.TicketRepository;
import com.eventbooking.ticket.repository.TicketTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.eventbooking.common.client.EventServiceClient;
import com.eventbooking.common.dto.EventDto;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements TicketService {
    
    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);
    private static final DateTimeFormatter TICKET_NUMBER_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    private final TicketRepository ticketRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketMapper ticketMapper;
    private final QRCodeService qrCodeService;
    private final TicketEventPublisher eventPublisher;
    private final EventServiceClient eventServiceClient;
    
   public TicketServiceImpl(TicketRepository ticketRepository,
                         TicketTypeRepository ticketTypeRepository,
                         TicketMapper ticketMapper,
                         QRCodeService qrCodeService,
                         TicketEventPublisher eventPublisher,
                         EventServiceClient eventServiceClient) {
        this.ticketRepository = ticketRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.ticketMapper = ticketMapper;
        this.qrCodeService = qrCodeService;
        this.eventPublisher = eventPublisher;
        this.eventServiceClient = eventServiceClient;
    }

    @Override
    @Transactional
    public List<TicketDto> generateTickets(GenerateTicketsRequest request) {
        logger.info("Generating {} tickets for order: {}", request.getQuantity(), request.getOrderId());
        
        // Validate ticket type exists
        TicketType ticketType = ticketTypeRepository.findById(request.getTicketTypeId())
                .orElseThrow(() -> new TicketTypeNotFoundException(
                        "Ticket type not found: " + request.getTicketTypeId()));
        
        List<TicketDto> generatedTickets = new ArrayList<>();
        
        for (int i = 0; i < request.getQuantity(); i++) {
            Ticket ticket = new Ticket();
            ticket.setTicketTypeId(request.getTicketTypeId());
            ticket.setOrderId(request.getOrderId());
            ticket.setHolderName(request.getHolderName());
            ticket.setUserId(request.getUserId());
            ticket.setStatus(Ticket.TicketStatus.ACTIVE);

            String ticketNumber = generateUniqueTicketNumber(ticketType.getEventId());
            ticket.setTicketNumber(ticketNumber);

            // Generate the ID before saving so the QR code can be generated
            // without inserting a ticket with a null qr_code.
            UUID ticketId = UUID.randomUUID();
            ticket.setId(ticketId);

            String qrCode = qrCodeService.generateQRCode(
                    ticketId.toString(),
                    ticketNumber);

            ticket.setQrCode(qrCode);

            ticket = ticketRepository.save(ticket);
            
            generatedTickets.add(ticketMapper.toDto(ticket));
            logger.debug("Generated ticket: {}", ticket.getTicketNumber());
        }
        
        logger.info("Successfully generated {} tickets for order: {}", 
                generatedTickets.size(), request.getOrderId());
        
        // Publish tickets generated event
        // Note: userId and holderEmail would need to be passed in the request in a real implementation
        eventPublisher.publishTicketsGenerated(
                request.getOrderId(),
                null, // userId - would need to be added to request
                ticketType.getEventId(),
                generatedTickets,
                request.getHolderName(),
                null // holderEmail - would need to be added to request
        );
        
        return generatedTickets;
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDto getTicketById(UUID ticketId) {
        logger.debug("Retrieving ticket by ID: {}", ticketId);
        
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found: " + ticketId));
        
        return enrichTickets(List.of(ticket)).get(0);
    }
    
    @Override
    public TicketDto getTicketByNumber(String ticketNumber) {
        logger.debug("Retrieving ticket by number: {}", ticketNumber);
        
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found: " + ticketNumber));
        
        return ticketMapper.toDto(ticket);
    }
    
   @Override
    @Transactional(readOnly = true)
    public List<TicketDto> getTicketsByOrderId(UUID orderId) {
        logger.debug("Retrieving tickets for order: {}", orderId);

        List<Ticket> tickets = ticketRepository.findByOrderId(orderId);

        if (tickets.isEmpty()) {
            return new ArrayList<>();
        }

        return enrichTickets(tickets);
    }
    
   @Override
    @Transactional(readOnly = true)
    public List<TicketDto> getTicketsByUserId(UUID userId) {
        logger.debug("Retrieving tickets for user: {}", userId);

        List<Ticket> tickets = ticketRepository.findByUserId(userId);

        if (tickets.isEmpty()) {
            return new ArrayList<>();
        }

        return enrichTickets(tickets);
    }

    @Override
    @Transactional
    public void cancelTicket(UUID ticketId) {
        logger.info("Cancelling ticket: {}", ticketId);
        
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found: " + ticketId));
        
        if (ticket.getStatus() == Ticket.TicketStatus.CANCELLED) {
            logger.warn("Ticket already cancelled: {}", ticketId);
            return;
        }
        
        ticket.setStatus(Ticket.TicketStatus.CANCELLED);
        ticketRepository.save(ticket);
        
        // Publish ticket cancelled event
        eventPublisher.publishTicketCancelled(ticketId, ticket.getOrderId(), null);
        
        logger.info("Successfully cancelled ticket: {}", ticketId);
    }
    
    @Override
    public TicketDto validateTicket(String qrCode) {
        logger.debug("Validating ticket with QR code");
        
        if (!qrCodeService.validateQRCode(qrCode)) {
            throw new TicketNotFoundException("Invalid QR code format");
        }
        
        Ticket ticket = ticketRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found for QR code"));
        
        if (ticket.getStatus() != Ticket.TicketStatus.ACTIVE) {
            logger.warn("Ticket validation failed - status: {}", ticket.getStatus());
        }
        
        return ticketMapper.toDto(ticket);
    }

    private List<TicketDto> enrichTickets(List<Ticket> tickets) {
        Map<UUID, TicketType> ticketTypes = ticketTypeRepository
                .findAllById(
                        tickets.stream()
                                .map(Ticket::getTicketTypeId)
                                .distinct()
                                .collect(Collectors.toList())
                )
                .stream()
                .collect(Collectors.toMap(TicketType::getId, Function.identity()));

        List<UUID> eventIds = ticketTypes.values().stream()
                .map(TicketType::getEventId)
                .distinct()
                .collect(Collectors.toList());

        Map<UUID, EventDto> events = eventServiceClient.getEventsByIds(eventIds)
                .stream()
                .collect(Collectors.toMap(EventDto::getId, Function.identity()));

        return tickets.stream()
                .map(ticket -> {
                    TicketDto dto = ticketMapper.toDto(ticket);

                    TicketType ticketType = ticketTypes.get(ticket.getTicketTypeId());

                    if (ticketType != null) {
                        dto.setTicketTypeName(ticketType.getName());
                        dto.setVenueZone(ticketType.getVenueZone());

                        EventDto event = events.get(ticketType.getEventId());

                        if (event != null) {
                            dto.setEventName(event.getName());

                            if (event.getEventDate() != null) {
                                dto.setEventDate(event.getEventDate().toString());
                            }

                            dto.setVenueName(event.getVenue() != null ? event.getVenue().getName() : null);
                            dto.setVenueAddress(event.getVenue() != null ? event.getVenue().getAddress() : null);
                        }
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Generate a unique ticket number
     * Format: EVT-{eventId-first8}-{timestamp}-{random4}
     */
    private String generateUniqueTicketNumber(UUID eventId) {
        String eventPrefix = eventId.toString().substring(0, 8).toUpperCase();
        String timestamp = LocalDateTime.now().format(TICKET_NUMBER_FORMATTER);
        String random = String.format("%04d", (int) (Math.random() * 10000));
        
        String ticketNumber;
        int attempts = 0;
        int maxAttempts = 10;
        
        do {
            ticketNumber = String.format("TKT-%s-%s-%s", eventPrefix, timestamp, random);
            attempts++;
            
            if (attempts >= maxAttempts) {
                // Add additional randomness if we've tried too many times
                random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                ticketNumber = String.format("TKT-%s-%s-%s", eventPrefix, timestamp, random);
            }
            
        } while (ticketRepository.existsByTicketNumber(ticketNumber) && attempts < maxAttempts * 2);
        
        if (ticketRepository.existsByTicketNumber(ticketNumber)) {
            throw new RuntimeException("Failed to generate unique ticket number after multiple attempts");
        }
        
        return ticketNumber;
    }
}

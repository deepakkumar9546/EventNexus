package com.eventbooking.notification.service;

import com.eventbooking.notification.dto.TicketDeliveryRequest;
import com.eventbooking.notification.dto.TicketDeliveryResponse;
import com.eventbooking.notification.entity.NotificationChannel;
import com.eventbooking.notification.entity.NotificationTemplate;
import com.eventbooking.notification.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import com.eventbooking.notification.entity.NotificationTemplate;
import com.eventbooking.notification.repository.NotificationRepository;
import com.eventbooking.notification.repository.NotificationTemplateRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketDeliveryServiceImplTest {

    @Mock
    private EmailService emailService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private TicketDeliveryServiceImpl ticketDeliveryService;

    private UUID userId;
    private UUID orderId;
    private List<UUID> ticketIds;
    private TicketDeliveryRequest deliveryRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        ticketIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());

        deliveryRequest = new TicketDeliveryRequest();
        deliveryRequest.setUserId(userId);
        deliveryRequest.setOrderId(orderId);
        deliveryRequest.setRecipientEmail("user@example.com");
        deliveryRequest.setTicketIds(ticketIds);
        deliveryRequest.setChannel(NotificationChannel.EMAIL);
        deliveryRequest.setIncludeCalendarEvent(true);
        deliveryRequest.setGenerateWebLink(true);

        ReflectionTestUtils.setField(
            ticketDeliveryService,
            "ticketServiceUrl",
            "http://localhost:8083"
        );

        ReflectionTestUtils.setField(
            ticketDeliveryService,
            "baseUrl",
            "https://eventnexus.example.com"
        );
    }

    private void mockTicketDetails() {
        for (UUID ticketId : ticketIds) {
            Map<String, Object> ticketData = new HashMap<>();
            ticketData.put("eventName", "Summer Music Festival");
            ticketData.put("eventDate", "2026-10-01T19:00:00");
            ticketData.put("venueName", "EventNexus Arena");
            ticketData.put("venueAddress", "123 Main Street");
            ticketData.put("ticketNumber", "TICKET-" + ticketId);
            ticketData.put("holderName", "Deepak");

            Map<String, Object> response = new HashMap<>();
            response.put("data", ticketData);

            when(restTemplate.getForObject(
                "http://localhost:8083/api/tickets/" + ticketId,
                Map.class
            )).thenReturn(response);
        }
    }

    private void mockTicketTemplate() {
        NotificationTemplate ticketTemplate = new NotificationTemplate();
        ticketTemplate.setId(UUID.randomUUID());
        ticketTemplate.setName("TICKET_DELIVERY");

        when(templateRepository.findByName("TICKET_DELIVERY"))
            .thenReturn(Optional.of(ticketTemplate));
    }

    @Test
    void deliverTickets_EmailChannel_Success() {
        mockTicketDetails();
        mockTicketTemplate();

        when(notificationRepository.save(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(emailService.sendEmail(any()))
            .thenReturn(true);

        TicketDeliveryResponse response =
            ticketDeliveryService.deliverTickets(deliveryRequest);

        assertNotNull(response);
        assertNotNull(response.getDeliveryStatus());
        assertNotNull(response.getTicketWebLinks());
        assertEquals(ticketIds.size(), response.getTicketWebLinks().size());
        assertNotNull(response.getCalendarEventLink());
    }

    @Test
    void deliverTickets_WithMobileChannel() {
        deliveryRequest.setChannel(NotificationChannel.MOBILE);

        mockTicketDetails();

        when(notificationRepository.save(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        TicketDeliveryResponse response =
            ticketDeliveryService.deliverTickets(deliveryRequest);

        assertNotNull(response);
        assertNotNull(response.getDeliveryStatus());
        assertNotNull(response.getTicketWebLinks());
    }

    @Test
    void generateTicketWebLink_Success() {
        UUID ticketId = UUID.randomUUID();

        String webLink =
            ticketDeliveryService.generateTicketWebLink(ticketId);

        assertNotNull(webLink);
        assertTrue(webLink.contains(ticketId.toString()));
        assertTrue(webLink.startsWith("https://"));
    }

    @Test
    void generateCalendarEventLink_Success() {
        String eventName = "Summer Music Festival";
        String eventDate = "2024-07-15T18:00:00";
        String venueName = "Central Park";
        String venueAddress = "123 Park Ave, New York, NY";

        String calendarLink =
            ticketDeliveryService.generateCalendarEventLink(
                eventName,
                eventDate,
                venueName,
                venueAddress
            );

        assertNotNull(calendarLink);
        assertTrue(
            calendarLink.startsWith(
                "https://calendar.google.com/calendar/render"
            )
        );
        assertTrue(calendarLink.contains("Summer+Music+Festival"));
        assertTrue(calendarLink.contains("Central+Park"));
    }

    @Test
    void generateCalendarEventLink_HandlesSpecialCharacters() {
        String eventName = "Rock & Roll Festival";
        String eventDate = "2024-07-15T18:00:00";
        String venueName = "O'Brien's Venue";
        String venueAddress = "123 Main St, City";

        String calendarLink =
            ticketDeliveryService.generateCalendarEventLink(
                eventName,
                eventDate,
                venueName,
                venueAddress
            );

        assertNotNull(calendarLink);
        assertTrue(
            calendarLink.startsWith(
                "https://calendar.google.com/calendar/render"
            )
        );
        assertTrue(
            calendarLink.contains("Rock+%26+Roll+Festival")
        );
        assertTrue(
            calendarLink.contains("O%27Brien%27s+Venue")
        );
    }

    @Test
    void deliverTickets_GeneratesWebLinksForAllTickets() {
        mockTicketDetails();
        mockTicketTemplate();

        when(notificationRepository.save(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(emailService.sendEmail(any()))
            .thenReturn(true);

        TicketDeliveryResponse response =
            ticketDeliveryService.deliverTickets(deliveryRequest);

        assertNotNull(response.getTicketWebLinks());
        assertEquals(ticketIds.size(), response.getTicketWebLinks().size());

        for (String webLink : response.getTicketWebLinks()) {
            assertNotNull(webLink);
            assertTrue(webLink.startsWith("https://"));
        }
    }

    @Test
    void deliverTickets_WithoutCalendarEvent() {
        deliveryRequest.setIncludeCalendarEvent(false);

        mockTicketDetails();
        mockTicketTemplate();

        when(notificationRepository.save(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(emailService.sendEmail(any()))
            .thenReturn(true);

        TicketDeliveryResponse response =
            ticketDeliveryService.deliverTickets(deliveryRequest);

        assertNotNull(response);
        assertNotNull(response.getTicketWebLinks());
    }

    @Test
    void deliverTickets_WithoutWebLinks() {
        deliveryRequest.setGenerateWebLink(false);

        mockTicketDetails();
        mockTicketTemplate();

        when(notificationRepository.save(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(emailService.sendEmail(any()))
            .thenReturn(true);

        TicketDeliveryResponse response =
            ticketDeliveryService.deliverTickets(deliveryRequest);

        assertNotNull(response);
        assertNotNull(response.getDeliveryStatus());
    }
}
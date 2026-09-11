package com.eventbooking.payment.saga;

import com.eventbooking.common.saga.SagaContext;
import com.eventbooking.common.saga.SagaStep;
import com.eventbooking.payment.client.TicketServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GenerateTicketsStep implements SagaStep {

    private static final Logger logger =
            LoggerFactory.getLogger(GenerateTicketsStep.class);

    private final TicketServiceClient ticketServiceClient;

    public GenerateTicketsStep(TicketServiceClient ticketServiceClient) {
        this.ticketServiceClient = ticketServiceClient;
    }

    @Override
    public boolean execute(SagaContext context) {

        logger.info(
                "Generating tickets for saga: {}",
                context.getSagaId()
        );

        try {
            UUID orderId = context.get("orderId", UUID.class);
            UUID userId = context.get("userId", UUID.class);
            UUID ticketTypeId = context.get("ticketTypeId", UUID.class);
            Integer quantity = context.get("quantity", Integer.class);
            String holderName = context.get("holderName", String.class);

            if (orderId == null || userId == null || ticketTypeId == null || quantity == null) {
                logger.error("Missing required parameters for ticket generation");
                context.setErrorMessage(
                        "Missing required parameters for ticket generation"
                );
                return false;
            }

            boolean generated = ticketServiceClient.generateTickets(
                    orderId,
                    userId,
                    ticketTypeId,
                    quantity,
                    holderName
            );

            if (!generated) {
                logger.error(
                        "Ticket generation failed for order: {}",
                        orderId
                );

                context.setErrorMessage(
                        "Ticket generation failed"
                );

                return false;
            }

            context.put("ticketsGenerated", true);

            logger.info(
                    "Tickets generated successfully for order: {}",
                    orderId
            );

            return true;

        } catch (Exception e) {
            logger.error("Failed to generate tickets", e);

            context.setErrorMessage(
                    "Ticket generation failed: " + e.getMessage()
            );

            return false;
        }
    }

    @Override
    public void compensate(SagaContext context) {
        /*
         * Ticket generation is the final step of the current purchase saga.
         * The generated tickets are transactional within Ticket Service.
         * If this step fails, no successful ticket generation should be
         * recorded in the saga.
         */
        logger.info(
                "Compensating ticket generation for saga: {}",
                context.getSagaId()
        );
    }

    @Override
    public String getStepName() {
        return "GenerateTickets";
    }

    @Override
    public int getOrder() {
        return 5;
    }
}
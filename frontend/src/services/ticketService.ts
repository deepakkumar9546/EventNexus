import { apiRequest } from './api';

export enum TicketStatus {
  ACTIVE = 'ACTIVE',
  USED = 'USED',
  CANCELLED = 'CANCELLED',
  EXPIRED = 'EXPIRED',
}

export interface Ticket {
  id: string;
  ticketTypeId: string;
  orderId: string;
  ticketNumber: string;
  qrCode: string;
  holderName: string;
  status: TicketStatus;
  createdAt: string;
  updatedAt: string;
  // Event and ticket type details
  eventName?: string;
  eventDate?: string;
  venueName?: string;
  venueAddress?: string;
  ticketTypeName?: string;
  venueZone?: string;
}

export const ticketService = {
  // Get ticket by ID
  async getTicketById(ticketId: string): Promise<Ticket> {
    const response = await apiRequest<{ data: Ticket }>(
      `/tickets/${ticketId}`
    );

    return response.data;
  },

  // Get ticket by ticket number
  async getTicketByNumber(ticketNumber: string): Promise<Ticket> {
    const response = await apiRequest<{ data: Ticket }>(
      `/tickets/number/${ticketNumber}`
    );

    return response.data;
  },

  // Get tickets by order ID
  async getTicketsByOrderId(orderId: string): Promise<Ticket[]> {
    const response = await apiRequest<{ data: Ticket[] }>(
      `/tickets/order/${orderId}`
    );

    return response.data;
  },

  // Get all tickets for a user
  async getTicketsByUserId(userId: string): Promise<Ticket[]> {
    const response = await apiRequest<{ data: Ticket[] }>(
      `/tickets/user/${userId}`
    );

    return response.data;
  },

  // Cancel a ticket
  async cancelTicket(ticketId: string): Promise<string> {
    const response = await apiRequest<{ data: string }>(
      `/tickets/${ticketId}/cancel`,
      {
        method: 'POST',
      }
    );

    return response.data;
  },

  // Validate a ticket
  async validateTicket(qrCode: string): Promise<Ticket> {
    const response = await apiRequest<{ data: Ticket }>(
      '/tickets/validate',
      {
        method: 'POST',
        body: JSON.stringify(qrCode),
      }
    );

    return response.data;
  },
};

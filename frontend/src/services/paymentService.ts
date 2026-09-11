import { apiRequest } from './api';

export interface OrderItem {
  ticketTypeId: string;
  quantity: number;
  unitPrice: number;
}

export interface CreateOrderRequest {
  eventId: string;
  reservationId?: string;
  items: OrderItem[];
}

export interface OrderItemDto {
  id: string;
  ticketTypeId: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
  fees: number;
  totalPrice: number;
  status: string;
  createdAt: string;
}

export interface Order {
  id: string;
  userId: string;
  eventId: string;
  orderNumber: string;
  subtotalAmount: number;
  serviceFee: number;
  taxAmount: number;
  totalAmount: number;
  paymentStatus: string;
  paymentMethod?: string;
  currency: string;
  reservationId?: string;
  createdAt: string;
  updatedAt: string;
  expiresAt?: string;
  orderItems: OrderItemDto[];
}

export interface TicketPurchaseRequest {
  eventId: string;
  ticketTypeId: string;
  quantity: number;
  unitPrice: number;
  paymentMethodId: string;
  holderName?: string;
  reservationId?: string;
}

export interface TicketPurchaseResponse {
  sagaId: string;
  orderId: string;
  orderNumber: string;
  transactionId: string;
  status: string;
  message: string;
}

export interface PaymentResponse {
  transactionId: string;
  status: string;
  clientSecret?: string;
  requiresAction?: boolean;
  errorMessage?: string;
}

export const paymentService = {
  async createOrder(request: CreateOrderRequest): Promise<Order> {
    const response = await apiRequest<{ data: Order }>('/payments/orders', {
      method: 'POST',
      body: JSON.stringify(request),
    });

    return response.data;
  },

  async getOrderById(orderId: string): Promise<Order> {
    const response = await apiRequest<{ data: Order }>(
      `/payments/orders/${orderId}`
    );

    return response.data;
  },

  async getOrderByNumber(orderNumber: string): Promise<Order> {
    const response = await apiRequest<{ data: Order }>(
      `/payments/orders/number/${orderNumber}`
    );

    return response.data;
  },

  async getUserOrders(
    userId: string,
    page: number = 0,
    size: number = 10
  ): Promise<{
    content: Order[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
  }> {
    const response = await apiRequest<{
      data: {
        content: Order[];
        totalElements: number;
        totalPages: number;
        size: number;
        number: number;
      };
    }>(`/payments/orders/user/${userId}?page=${page}&size=${size}`);

    return response.data;
  },

  async purchaseTickets(
    request: TicketPurchaseRequest,
    userId: string
  ): Promise<TicketPurchaseResponse> {
    const response = await apiRequest<{ data: TicketPurchaseResponse }>(
      '/payments/purchase-tickets',
      {
        method: 'POST',
        headers: {
          'X-User-Id': userId,
        },
        body: JSON.stringify(request),
      }
    );

    return response.data;
  },

  async cancelOrder(orderId: string): Promise<Order> {
    const response = await apiRequest<{ data: Order }>(
      `/payments/orders/${orderId}/cancel`,
      {
        method: 'POST',
      }
    );

    return response.data;
  },

  async confirmOrder(
    orderId: string,
    paymentIntentId?: string
  ): Promise<Order> {
    const url = paymentIntentId
      ? `/payments/orders/${orderId}/confirm?paymentIntentId=${paymentIntentId}`
      : `/payments/orders/${orderId}/confirm`;

    const response = await apiRequest<{ data: Order }>(url, {
      method: 'POST',
    });

    return response.data;
  },
};

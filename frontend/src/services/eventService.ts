import { apiRequest } from './api';

export interface Venue {
  id: string;
  name: string;
  address: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
  latitude?: number;
  longitude?: number;
  maxCapacity?: number;
  venueType?: string;
  createdAt?: string;
}


export interface Event {
  id: string;
  name: string;
  description: string;
  eventDate: string;
  venue: Venue;
  category: Category;
  imageUrl?: string;
  status: string;
  organizerId: string;
  createdAt: string;
  updatedAt: string;
}

export interface Category {
  id: string;
  name: string;
  description: string;
  iconUrl?: string;
  displayOrder: number;
  isActive: boolean;
  createdAt?: string;
}

export interface TicketType {
  id: string;
  eventId: string;
  name: string;
  description: string;
  price: number;
  quantityAvailable: number;
  quantitySold: number;
  saleStartDate?: string;
  saleEndDate?: string;
  perPersonLimit: number;
  venueZone?: string;
}

export interface SearchCriteria {
  query?: string;
  category?: string;
  city?: string;
  startDate?: string;
  endDate?: string;
  sortBy?: 'date' | 'price' | 'popularity';
  page?: number;
  size?: number;
}

export interface SearchSuggestion {
  type: 'event' | 'venue' | 'category';
  value: string;
  label: string;
}

export interface EventSearchResponse {
  content: Event[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const eventService = {
  // Get all events with optional filters
  async getEvents(criteria: SearchCriteria = {}): Promise<EventSearchResponse> {
    const params = new URLSearchParams();

    if (criteria.query) params.append('query', criteria.query);
    if (criteria.category) params.append('category', criteria.category);
    if (criteria.city) params.append('city', criteria.city);
    if (criteria.startDate) params.append('startDate', criteria.startDate);
    if (criteria.endDate) params.append('endDate', criteria.endDate);
    if (criteria.sortBy) params.append('sortBy', criteria.sortBy);
    if (criteria.page !== undefined) params.append('page', criteria.page.toString());
    if (criteria.size !== undefined) params.append('size', criteria.size.toString());

    const queryString = params.toString();
    const endpoint = queryString ? `/events?${queryString}` : '/events';

    const response = await apiRequest<{ data: EventSearchResponse }>(endpoint);
    return response.data;
  },

  // Search events with criteria
  async searchEvents(criteria: SearchCriteria): Promise<EventSearchResponse> {
  const response = await apiRequest<{ data: EventSearchResponse }>('/events/search', {
    method: 'POST',
    body: JSON.stringify(criteria),
  });
  return response.data;
},

  // Get search suggestions
  async getSearchSuggestions(query: string): Promise<SearchSuggestion[]> {
    return apiRequest<SearchSuggestion[]>(`/events/search/suggestions?query=${encodeURIComponent(query)}`);
  },

  // Get event by ID
  async getEventById(id: string): Promise<Event> {
    const response = await apiRequest<{ data: Event }>(`/events/${id}`);
    return response.data;
},

  // Get ticket types for an event
  async getTicketTypes(eventId: string): Promise<TicketType[]> {
    const response = await apiRequest<{ data: TicketType[] }>(
    `/ticket-types/event/${eventId}/available`
  );
    return response.data;
},
  // Get all categories
  async getCategories(): Promise<Category[]> {
    const response = await apiRequest<{ data: Category[] }>('/categories');
    return response.data;
  },
};

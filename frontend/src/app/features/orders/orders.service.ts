import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page } from '../../core/models/page.model';
import {
  Order,
  OrderCreateRequest,
  OrderHistoryEntry,
  OrderStatus,
  OrderTransitionRequest,
} from './models/order.model';

export interface OrderSearchParams {
  page?: number;
  size?: number;
  customerId?: number | null;
  status?: OrderStatus | null;
  assignedDriverId?: number | null;
}

@Injectable({ providedIn: 'root' })
export class OrdersService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  search(params: OrderSearchParams): Observable<Page<Order>> {
    let httpParams = new HttpParams()
      .set('page', params.page ?? 0)
      .set('size', params.size ?? 20);

    if (params.customerId != null) httpParams = httpParams.set('customerId', params.customerId);
    if (params.status) httpParams = httpParams.set('status', params.status);
    if (params.assignedDriverId != null) httpParams = httpParams.set('assignedDriverId', params.assignedDriverId);

    return this.http.get<Page<Order>>(`${this.baseUrl}/orders`, { params: httpParams });
  }

  getById(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.baseUrl}/orders/${id}`);
  }

  getHistory(id: number): Observable<OrderHistoryEntry[]> {
    return this.http.get<OrderHistoryEntry[]>(`${this.baseUrl}/orders/${id}/history`);
  }

  create(request: OrderCreateRequest): Observable<Order> {
    return this.http.post<Order>(`${this.baseUrl}/orders`, request);
  }

  transition(id: number, request: OrderTransitionRequest): Observable<Order> {
    return this.http.post<Order>(`${this.baseUrl}/orders/${id}/transition`, request);
  }
}

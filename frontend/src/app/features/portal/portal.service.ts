import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page } from '../../core/models/page.model';
import { Invoice } from '../billing/models/invoice.model';
import { Order } from '../orders/models/order.model';
import { PortalSummary } from './models/portal.model';

@Injectable({ providedIn: 'root' })
export class PortalService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/portal`;

  summary(): Observable<PortalSummary> {
    return this.http.get<PortalSummary>(`${this.baseUrl}/summary`);
  }

  orders(page = 0, size = 20): Observable<Page<Order>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<Order>>(`${this.baseUrl}/orders`, { params });
  }

  invoices(page = 0, size = 20): Observable<Page<Invoice>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<Invoice>>(`${this.baseUrl}/invoices`, { params });
  }

  repeatLastOrder(): Observable<Order> {
    return this.http.post<Order>(`${this.baseUrl}/orders/repeat-last`, {});
  }
}

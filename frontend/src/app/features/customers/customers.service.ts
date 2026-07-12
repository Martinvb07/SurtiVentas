import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page } from '../../core/models/page.model';
import {
  Customer,
  CustomerClassification,
  CustomerCreateRequest,
  CustomerUpdateRequest,
  DebtMovement,
  DebtMovementRequest,
} from './models/customer.model';

export interface CustomerSearchParams {
  page?: number;
  size?: number;
  classification?: CustomerClassification | null;
  active?: boolean | null;
  search?: string | null;
}

@Injectable({ providedIn: 'root' })
export class CustomersService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  search(params: CustomerSearchParams): Observable<Page<Customer>> {
    let httpParams = new HttpParams()
      .set('page', params.page ?? 0)
      .set('size', params.size ?? 20);

    if (params.classification) httpParams = httpParams.set('classification', params.classification);
    if (params.active != null) httpParams = httpParams.set('active', params.active);
    if (params.search) httpParams = httpParams.set('search', params.search);

    return this.http.get<Page<Customer>>(`${this.baseUrl}/customers`, { params: httpParams });
  }

  create(request: CustomerCreateRequest): Observable<Customer> {
    return this.http.post<Customer>(`${this.baseUrl}/customers`, request);
  }

  update(id: number, request: CustomerUpdateRequest): Observable<Customer> {
    return this.http.put<Customer>(`${this.baseUrl}/customers/${id}`, request);
  }

  deactivate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/customers/${id}`);
  }

  adjustDebt(id: number, request: DebtMovementRequest): Observable<Customer> {
    return this.http.post<Customer>(`${this.baseUrl}/customers/${id}/debt-movements`, request);
  }

  getDebtMovements(id: number): Observable<DebtMovement[]> {
    return this.http.get<DebtMovement[]>(`${this.baseUrl}/customers/${id}/debt-movements`);
  }
}

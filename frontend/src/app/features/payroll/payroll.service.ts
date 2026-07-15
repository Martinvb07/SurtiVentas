import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Employee,
  EmployeeRequest,
  EmployeeUpdateRequest,
  PayrollPayment,
  PayrollPaymentRequest,
} from './models/payroll.model';

@Injectable({ providedIn: 'root' })
export class PayrollService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/employees`;

  employees(): Observable<Employee[]> {
    return this.http.get<Employee[]>(this.baseUrl);
  }

  create(request: EmployeeRequest): Observable<Employee> {
    return this.http.post<Employee>(this.baseUrl, request);
  }

  update(id: number, request: EmployeeUpdateRequest): Observable<Employee> {
    return this.http.put<Employee>(`${this.baseUrl}/${id}`, request);
  }

  deactivate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  registerPayment(id: number, request: PayrollPaymentRequest): Observable<PayrollPayment> {
    return this.http.post<PayrollPayment>(`${this.baseUrl}/${id}/payments`, request);
  }

  payments(id: number): Observable<PayrollPayment[]> {
    return this.http.get<PayrollPayment[]>(`${this.baseUrl}/${id}/payments`);
  }
}

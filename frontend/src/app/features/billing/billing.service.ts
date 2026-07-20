import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page } from '../../core/models/page.model';
import {
  BillableOrder,
  GenerateInvoiceRequest,
  Invoice,
  InvoiceLineReview,
  InvoiceStatus,
  PaymentReceipt,
  RegisterPaymentRequest,
} from './models/invoice.model';

export interface InvoiceSearchParams {
  page?: number;
  size?: number;
  status?: InvoiceStatus | null;
  customerId?: number | null;
  overdue?: boolean | null;
}

@Injectable({ providedIn: 'root' })
export class BillingService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/invoices`;

  search(params: InvoiceSearchParams): Observable<Page<Invoice>> {
    let httpParams = new HttpParams()
      .set('page', params.page ?? 0)
      .set('size', params.size ?? 20);

    if (params.status) httpParams = httpParams.set('status', params.status);
    if (params.customerId != null) httpParams = httpParams.set('customerId', params.customerId);
    if (params.overdue) httpParams = httpParams.set('overdue', true);

    return this.http.get<Page<Invoice>>(this.baseUrl, { params: httpParams });
  }

  getById(id: number): Observable<Invoice> {
    return this.http.get<Invoice>(`${this.baseUrl}/${id}`);
  }

  billableOrders(): Observable<BillableOrder[]> {
    return this.http.get<BillableOrder[]>(`${this.baseUrl}/billable-orders`);
  }

  reviewOrder(orderId: number): Observable<InvoiceLineReview[]> {
    return this.http.get<InvoiceLineReview[]>(`${this.baseUrl}/order/${orderId}/review`);
  }

  generate(request: GenerateInvoiceRequest): Observable<Invoice> {
    return this.http.post<Invoice>(this.baseUrl, request);
  }

  registerPayment(id: number, request: RegisterPaymentRequest): Observable<Invoice> {
    return this.http.post<Invoice>(`${this.baseUrl}/${id}/payments`, request);
  }

  // ---- Payment-receipt OCR + reconciliation ----

  /** The attached receipt, or null (204) when none has been scanned yet. */
  getReceipt(paymentId: number): Observable<PaymentReceipt | null> {
    return this.http.get<PaymentReceipt | null>(`${environment.apiUrl}/payments/${paymentId}/receipt`);
  }

  /** Scans (OCRs) and attaches the payment receipt, returning the reconciliation. */
  uploadReceipt(paymentId: number, file: File): Observable<PaymentReceipt> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<PaymentReceipt>(`${environment.apiUrl}/payments/${paymentId}/receipt`, formData);
  }

  /** The original scanned file, as a Blob (fetched with the auth header). */
  getReceiptFile(paymentId: number): Observable<Blob> {
    return this.http.get(`${environment.apiUrl}/payments/${paymentId}/receipt/file`, { responseType: 'blob' });
  }
}

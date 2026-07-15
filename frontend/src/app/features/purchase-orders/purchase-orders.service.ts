import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page } from '../../core/models/page.model';
import {
  PurchaseOrder,
  PurchaseOrderCreateRequest,
  PurchaseOrderHistoryEntry,
  PurchaseOrderStatus,
  PurchaseOrderTransitionRequest,
  SupplierInvoice,
} from './models/purchase-order.model';

export interface PurchaseOrderSearchParams {
  page?: number;
  size?: number;
  supplierId?: number | null;
  status?: PurchaseOrderStatus | null;
}

@Injectable({ providedIn: 'root' })
export class PurchaseOrdersService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  search(params: PurchaseOrderSearchParams): Observable<Page<PurchaseOrder>> {
    let httpParams = new HttpParams()
      .set('page', params.page ?? 0)
      .set('size', params.size ?? 20);

    if (params.supplierId != null) httpParams = httpParams.set('supplierId', params.supplierId);
    if (params.status) httpParams = httpParams.set('status', params.status);

    return this.http.get<Page<PurchaseOrder>>(`${this.baseUrl}/purchase-orders`, { params: httpParams });
  }

  getById(id: number): Observable<PurchaseOrder> {
    return this.http.get<PurchaseOrder>(`${this.baseUrl}/purchase-orders/${id}`);
  }

  getHistory(id: number): Observable<PurchaseOrderHistoryEntry[]> {
    return this.http.get<PurchaseOrderHistoryEntry[]>(`${this.baseUrl}/purchase-orders/${id}/history`);
  }

  create(request: PurchaseOrderCreateRequest): Observable<PurchaseOrder> {
    return this.http.post<PurchaseOrder>(`${this.baseUrl}/purchase-orders`, request);
  }

  transition(id: number, request: PurchaseOrderTransitionRequest): Observable<PurchaseOrder> {
    return this.http.post<PurchaseOrder>(`${this.baseUrl}/purchase-orders/${id}/transition`, request);
  }

  // ---- Supplier-invoice OCR + reconciliation ----

  /** The attached invoice, or null (204) when none has been scanned yet. */
  getInvoice(id: number): Observable<SupplierInvoice | null> {
    return this.http.get<SupplierInvoice | null>(`${this.baseUrl}/purchase-orders/${id}/invoice`);
  }

  /** Scans (OCRs) and attaches the supplier invoice, returning the reconciliation. */
  uploadInvoice(id: number, file: File): Observable<SupplierInvoice> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<SupplierInvoice>(`${this.baseUrl}/purchase-orders/${id}/invoice`, formData);
  }

  /** The original scanned file, as a Blob (fetched with the auth header). */
  getInvoiceFile(id: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/purchase-orders/${id}/invoice/file`, { responseType: 'blob' });
  }
}

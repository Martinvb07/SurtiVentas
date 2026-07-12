import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page } from '../../core/models/page.model';
import {
  Supplier,
  SupplierCreateRequest,
  SupplierProduct,
  SupplierProductRequest,
  SupplierUpdateRequest,
} from './models/supplier.model';

export interface SupplierSearchParams {
  page?: number;
  size?: number;
  active?: boolean | null;
  search?: string | null;
}

@Injectable({ providedIn: 'root' })
export class SuppliersService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  search(params: SupplierSearchParams): Observable<Page<Supplier>> {
    let httpParams = new HttpParams()
      .set('page', params.page ?? 0)
      .set('size', params.size ?? 20);

    if (params.active != null) httpParams = httpParams.set('active', params.active);
    if (params.search) httpParams = httpParams.set('search', params.search);

    return this.http.get<Page<Supplier>>(`${this.baseUrl}/suppliers`, { params: httpParams });
  }

  getById(id: number): Observable<Supplier> {
    return this.http.get<Supplier>(`${this.baseUrl}/suppliers/${id}`);
  }

  create(request: SupplierCreateRequest): Observable<Supplier> {
    return this.http.post<Supplier>(`${this.baseUrl}/suppliers`, request);
  }

  update(id: number, request: SupplierUpdateRequest): Observable<Supplier> {
    return this.http.put<Supplier>(`${this.baseUrl}/suppliers/${id}`, request);
  }

  deactivate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/suppliers/${id}`);
  }

  getProducts(supplierId: number): Observable<SupplierProduct[]> {
    return this.http.get<SupplierProduct[]>(`${this.baseUrl}/suppliers/${supplierId}/products`);
  }

  addProduct(supplierId: number, request: SupplierProductRequest): Observable<SupplierProduct> {
    return this.http.post<SupplierProduct>(`${this.baseUrl}/suppliers/${supplierId}/products`, request);
  }

  updateProduct(supplierId: number, supplierProductId: number, request: SupplierProductRequest): Observable<SupplierProduct> {
    return this.http.put<SupplierProduct>(`${this.baseUrl}/suppliers/${supplierId}/products/${supplierProductId}`, request);
  }

  removeProduct(supplierId: number, supplierProductId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/suppliers/${supplierId}/products/${supplierProductId}`);
  }
}

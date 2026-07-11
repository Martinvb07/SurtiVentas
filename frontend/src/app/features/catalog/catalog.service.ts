import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page } from '../../core/models/page.model';
import { Category, CategoryRequest } from './models/category.model';
import {
  Product,
  ProductCreateRequest,
  ProductUpdateRequest,
  StockMovement,
  StockMovementRequest,
} from './models/product.model';
import { UnitOfMeasure, UnitOfMeasureRequest } from './models/unit-of-measure.model';

export interface ProductSearchParams {
  page?: number;
  size?: number;
  categoryId?: number | null;
  active?: boolean | null;
  lowStock?: boolean | null;
  search?: string | null;
}

@Injectable({ providedIn: 'root' })
export class CatalogService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  searchProducts(params: ProductSearchParams): Observable<Page<Product>> {
    let httpParams = new HttpParams()
      .set('page', params.page ?? 0)
      .set('size', params.size ?? 20);

    if (params.categoryId != null) httpParams = httpParams.set('categoryId', params.categoryId);
    if (params.active != null) httpParams = httpParams.set('active', params.active);
    if (params.lowStock != null) httpParams = httpParams.set('lowStock', params.lowStock);
    if (params.search) httpParams = httpParams.set('search', params.search);

    return this.http.get<Page<Product>>(`${this.baseUrl}/products`, { params: httpParams });
  }

  createProduct(request: ProductCreateRequest): Observable<Product> {
    return this.http.post<Product>(`${this.baseUrl}/products`, request);
  }

  updateProduct(id: number, request: ProductUpdateRequest): Observable<Product> {
    return this.http.put<Product>(`${this.baseUrl}/products/${id}`, request);
  }

  deactivateProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/products/${id}`);
  }

  adjustStock(id: number, request: StockMovementRequest): Observable<Product> {
    return this.http.post<Product>(`${this.baseUrl}/products/${id}/stock-movements`, request);
  }

  getStockMovements(id: number): Observable<StockMovement[]> {
    return this.http.get<StockMovement[]>(`${this.baseUrl}/products/${id}/stock-movements`);
  }

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.baseUrl}/categories`);
  }

  createCategory(request: CategoryRequest): Observable<Category> {
    return this.http.post<Category>(`${this.baseUrl}/categories`, request);
  }

  getUnits(): Observable<UnitOfMeasure[]> {
    return this.http.get<UnitOfMeasure[]>(`${this.baseUrl}/units`);
  }

  createUnit(request: UnitOfMeasureRequest): Observable<UnitOfMeasure> {
    return this.http.post<UnitOfMeasure>(`${this.baseUrl}/units`, request);
  }
}

import { inject, Injectable } from '@angular/core';
import { catchError, map, Observable, of, tap } from 'rxjs';
import { CatalogService } from '../../features/catalog/catalog.service';
import { Product } from '../../features/catalog/models/product.model';
import { CustomersService } from '../../features/customers/customers.service';
import { Customer } from '../../features/customers/models/customer.model';
import { ConnectivityService } from './connectivity.service';
import { OfflineStorageService } from './offline-storage.service';

const PRODUCTS_KEY = 'presale.cache.products';
const CUSTOMERS_KEY = 'presale.cache.customers';

/**
 * Serves the data the pre-sale cart needs (active products + customers) with an
 * offline fallback: when online it fetches and refreshes a localStorage cache;
 * when offline (or the request fails) it returns the last cached snapshot so the
 * seller can keep building orders in the field.
 */
@Injectable({ providedIn: 'root' })
export class PresaleCacheService {
  private readonly catalog = inject(CatalogService);
  private readonly customersService = inject(CustomersService);
  private readonly connectivity = inject(ConnectivityService);
  private readonly storage = inject(OfflineStorageService);

  getProducts(): Observable<Product[]> {
    if (!this.connectivity.online()) {
      return of(this.cachedProducts());
    }
    return this.catalog.searchProducts({ active: true, size: 500 }).pipe(
      map((page) => page.content),
      tap((products) => this.storage.set(PRODUCTS_KEY, products)),
      catchError(() => of(this.cachedProducts())),
    );
  }

  getCustomers(): Observable<Customer[]> {
    if (!this.connectivity.online()) {
      return of(this.cachedCustomers());
    }
    return this.customersService.search({ active: true, size: 500 }).pipe(
      map((page) => page.content),
      tap((customers) => this.storage.set(CUSTOMERS_KEY, customers)),
      catchError(() => of(this.cachedCustomers())),
    );
  }

  /** Whether there is enough cached data to build an order fully offline. */
  hasCache(): boolean {
    return this.cachedProducts().length > 0 && this.cachedCustomers().length > 0;
  }

  private cachedProducts(): Product[] {
    return this.storage.get<Product[]>(PRODUCTS_KEY, []);
  }

  private cachedCustomers(): Customer[] {
    return this.storage.get<Customer[]>(CUSTOMERS_KEY, []);
  }
}

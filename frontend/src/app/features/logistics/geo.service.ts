import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DeliveryPoint, StorePoint } from './models/geo.model';

@Injectable({ providedIn: 'root' })
export class GeoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/geo`;

  stores(): Observable<StorePoint[]> {
    return this.http.get<StorePoint[]>(`${this.baseUrl}/customers`);
  }

  deliveries(): Observable<DeliveryPoint[]> {
    return this.http.get<DeliveryPoint[]>(`${this.baseUrl}/deliveries`);
  }
}

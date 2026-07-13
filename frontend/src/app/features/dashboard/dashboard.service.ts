import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AdminDashboard,
  BillingDashboard,
  DriverDashboard,
  SellerDashboard,
  WarehouseDashboard,
} from './models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/dashboard`;

  admin(): Observable<AdminDashboard> {
    return this.http.get<AdminDashboard>(`${this.baseUrl}/admin`);
  }

  seller(): Observable<SellerDashboard> {
    return this.http.get<SellerDashboard>(`${this.baseUrl}/seller`);
  }

  warehouse(): Observable<WarehouseDashboard> {
    return this.http.get<WarehouseDashboard>(`${this.baseUrl}/warehouse`);
  }

  driver(): Observable<DriverDashboard> {
    return this.http.get<DriverDashboard>(`${this.baseUrl}/driver`);
  }

  billing(): Observable<BillingDashboard> {
    return this.http.get<BillingDashboard>(`${this.baseUrl}/billing`);
  }
}

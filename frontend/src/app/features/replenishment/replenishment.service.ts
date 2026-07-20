import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SupplierReplenishment } from './models/replenishment.model';

@Injectable({ providedIn: 'root' })
export class ReplenishmentService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/replenishment`;

  getSuggestions(): Observable<SupplierReplenishment[]> {
    return this.http.get<SupplierReplenishment[]>(`${this.baseUrl}/suggestions`);
  }
}

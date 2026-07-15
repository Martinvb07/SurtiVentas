import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { IncomeReport } from './models/income.model';

@Injectable({ providedIn: 'root' })
export class FinanceService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/finance`;

  income(): Observable<IncomeReport> {
    return this.http.get<IncomeReport>(`${this.baseUrl}/income`);
  }
}

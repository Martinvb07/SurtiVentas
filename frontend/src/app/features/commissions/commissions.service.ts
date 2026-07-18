import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Commission, SalesGoal, SalesGoalRequest } from './models/commission.model';

@Injectable({ providedIn: 'root' })
export class CommissionsService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  private monthParams(month: string | null): { params?: HttpParams } {
    return month ? { params: new HttpParams().set('month', month) } : {};
  }

  /** Commission report for every salesperson (admin). */
  getCommissions(month: string | null): Observable<Commission[]> {
    return this.http.get<Commission[]>(`${this.baseUrl}/commissions`, this.monthParams(month));
  }

  /** The current salesperson's own commission. */
  getMyCommission(month: string | null): Observable<Commission> {
    return this.http.get<Commission>(`${this.baseUrl}/commissions/me`, this.monthParams(month));
  }

  getGoals(month: string | null): Observable<SalesGoal[]> {
    return this.http.get<SalesGoal[]>(`${this.baseUrl}/sales-goals`, this.monthParams(month));
  }

  upsertGoal(request: SalesGoalRequest): Observable<SalesGoal> {
    return this.http.post<SalesGoal>(`${this.baseUrl}/sales-goals`, request);
  }

  deleteGoal(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/sales-goals/${id}`);
  }
}

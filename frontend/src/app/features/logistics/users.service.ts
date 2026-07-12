import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Driver } from './models/driver.model';

@Injectable({ providedIn: 'root' })
export class UsersService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  searchByRole(role: string): Observable<Driver[]> {
    const params = new HttpParams().set('role', role);
    return this.http.get<Driver[]>(`${this.baseUrl}/users`, { params });
  }
}

import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserAccount, UserCreateRequest, UserUpdateRequest } from './models/user-account.model';

@Injectable({ providedIn: 'root' })
export class UsersService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/users`;

  list(): Observable<UserAccount[]> {
    return this.http.get<UserAccount[]>(this.baseUrl);
  }

  create(request: UserCreateRequest): Observable<UserAccount> {
    return this.http.post<UserAccount>(this.baseUrl, request);
  }

  update(id: number, request: UserUpdateRequest): Observable<UserAccount> {
    return this.http.put<UserAccount>(`${this.baseUrl}/${id}`, request);
  }

  resetPassword(id: number, password: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${id}/reset-password`, { password });
  }
}

import { HttpClient } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest } from './models/auth-response.model';
import { User } from './models/user.model';
import { TokenStorageService } from './token-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly currentUserSignal = signal<User | null>(null);

  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);

  constructor(
    private readonly http: HttpClient,
    private readonly tokenStorage: TokenStorageService,
  ) {
    this.restoreSession();
  }

  restoreSession(): void {
    const user = this.tokenStorage.getUser();
    const accessToken = this.tokenStorage.getAccessToken();
    this.currentUserSignal.set(user && accessToken ? user : null);
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, request).pipe(
      tap((response) => this.applySession(response)),
    );
  }

  refreshToken(): Observable<AuthResponse> {
    const refreshToken = this.tokenStorage.getRefreshToken();
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/refresh`, { refreshToken }).pipe(
      tap((response) => this.applySession(response)),
    );
  }

  logout(): void {
    const refreshToken = this.tokenStorage.getRefreshToken();
    if (refreshToken) {
      this.http.post(`${environment.apiUrl}/auth/logout`, { refreshToken }).subscribe({
        error: () => undefined,
      });
    }
    this.tokenStorage.clear();
    this.currentUserSignal.set(null);
  }

  private applySession(response: AuthResponse): void {
    this.tokenStorage.setSession(response.accessToken, response.refreshToken, response.user);
    this.currentUserSignal.set(response.user);
  }
}

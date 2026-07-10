import { Injectable } from '@angular/core';
import { User } from './models/user.model';

const ACCESS_TOKEN_KEY = 'sv_access_token';
const REFRESH_TOKEN_KEY = 'sv_refresh_token';
const USER_KEY = 'sv_user';

/**
 * Tokens live in localStorage. Trade-off vs an httpOnly refresh-token
 * cookie: simpler (no CSRF handling needed for a bearer-token API) but more
 * exposed to XSS. Acceptable for this capstone's scope given short-lived
 * access tokens + rotating refresh tokens; flagged as a future hardening
 * item rather than a silent decision.
 */
@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  getUser(): User | null {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as User) : null;
  }

  setSession(accessToken: string, refreshToken: string, user: User): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  setAccessToken(accessToken: string): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  }

  clear(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }
}

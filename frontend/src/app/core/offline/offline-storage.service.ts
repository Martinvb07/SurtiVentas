import { Injectable } from '@angular/core';

/**
 * Thin, defensive wrapper over localStorage used by the offline-first pre-sale
 * features (cached catalog/customers, cart draft, and the order outbox). Never
 * throws: bad JSON or a full quota degrades to the fallback rather than
 * breaking the app.
 */
@Injectable({ providedIn: 'root' })
export class OfflineStorageService {
  get<T>(key: string, fallback: T): T {
    try {
      const raw = localStorage.getItem(key);
      return raw ? (JSON.parse(raw) as T) : fallback;
    } catch {
      return fallback;
    }
  }

  set(key: string, value: unknown): void {
    try {
      localStorage.setItem(key, JSON.stringify(value));
    } catch {
      // Quota exceeded or storage unavailable — nothing we can do here.
    }
  }

  remove(key: string): void {
    try {
      localStorage.removeItem(key);
    } catch {
      // ignore
    }
  }
}

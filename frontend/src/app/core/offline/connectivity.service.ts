import { Injectable, signal } from '@angular/core';

/**
 * Tracks browser connectivity as a signal. Backed by the navigator's online
 * state plus the window online/offline events, so the UI (and the order
 * outbox) can react to going offline and coming back.
 */
@Injectable({ providedIn: 'root' })
export class ConnectivityService {
  readonly online = signal(navigator.onLine);

  constructor() {
    window.addEventListener('online', () => this.online.set(true));
    window.addEventListener('offline', () => this.online.set(false));
  }
}

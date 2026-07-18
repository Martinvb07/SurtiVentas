import { Injectable, computed, inject, signal } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import { AuthService } from '../auth/auth.service';
import { TokenStorageService } from '../auth/token-storage.service';
import { AppNotification } from './notification.model';

/**
 * Real-time notifications over STOMP/WebSocket. On connect it subscribes to the
 * current user's role topic; each order handoff for that role arrives live and
 * feeds the topbar bell.
 */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly auth = inject(AuthService);
  private readonly tokenStorage = inject(TokenStorageService);

  private client?: Client;

  private readonly items = signal<AppNotification[]>([]);
  readonly notifications = this.items.asReadonly();
  readonly unreadCount = computed(() => this.items().filter((n) => !n.read).length);

  connect(): void {
    const user = this.auth.currentUser();
    const token = this.tokenStorage.getAccessToken();
    if (!user || !token || this.client?.active) {
      return;
    }

    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
    const brokerURL = `${protocol}://${window.location.host}/ws`;

    this.client = new Client({
      brokerURL,
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      onConnect: () => {
        this.client?.subscribe(`/topic/notifications.${user.role}`, (message: IMessage) => {
          this.push(JSON.parse(message.body) as AppNotification);
        });
      },
    });
    this.client.activate();
  }

  disconnect(): void {
    void this.client?.deactivate();
    this.client = undefined;
    this.items.set([]);
  }

  markAllRead(): void {
    this.items.update((list) => list.map((n) => ({ ...n, read: true })));
  }

  private push(notification: AppNotification): void {
    this.items.update((list) => [{ ...notification, read: false }, ...list].slice(0, 30));
  }
}

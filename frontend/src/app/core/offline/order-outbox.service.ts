import { HttpErrorResponse } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { Order, OrderCreateRequest } from '../../features/orders/models/order.model';
import { OrdersService } from '../../features/orders/orders.service';
import { ConnectivityService } from './connectivity.service';
import { OfflineStorageService } from './offline-storage.service';

/** A pre-sale order queued on the device, waiting to sync to the backend. */
export interface OutboxOrder {
  clientRequestId: string;
  request: OrderCreateRequest;
  customerName: string;
  total: number;
  createdAt: string;
  lastError?: string;
}

export interface SubmitResult {
  /** true = created on the server now; false = queued for later sync. */
  synced: boolean;
  order?: Order;
}

const OUTBOX_KEY = 'presale.outbox';

/**
 * Offline-first submission queue for pre-sale orders. When online, orders are
 * POSTed immediately; when offline (or the POST fails on the network) they are
 * stored locally and re-sent automatically when connectivity returns. Every
 * queued order carries a client-generated UUID so the backend deduplicates
 * retries — a flaky sync never creates a duplicate order.
 */
@Injectable({ providedIn: 'root' })
export class OrderOutboxService {
  private readonly orders = inject(OrdersService);
  private readonly connectivity = inject(ConnectivityService);
  private readonly storage = inject(OfflineStorageService);

  readonly pending = signal<OutboxOrder[]>(this.storage.get<OutboxOrder[]>(OUTBOX_KEY, []));
  readonly pendingCount = computed(() => this.pending().length);
  readonly syncing = signal(false);

  constructor() {
    // Flush automatically when the network comes back, and once on startup.
    window.addEventListener('online', () => void this.flush());
    if (this.connectivity.online()) {
      queueMicrotask(() => void this.flush());
    }
  }

  /**
   * Creates the order online, or queues it for later when offline. Real
   * server-side rejections (e.g. an inactive customer) are thrown so the caller
   * can show them; only genuine network failures fall back to the queue.
   */
  async submit(
    base: Pick<OrderCreateRequest, 'customerId' | 'lines'>,
    meta: { customerName: string; total: number },
  ): Promise<SubmitResult> {
    const clientRequestId = crypto.randomUUID();
    const request: OrderCreateRequest = { ...base, clientRequestId };
    const entry: OutboxOrder = {
      clientRequestId,
      request,
      customerName: meta.customerName,
      total: meta.total,
      createdAt: new Date().toISOString(),
    };

    if (!this.connectivity.online()) {
      this.enqueue(entry);
      return { synced: false };
    }

    try {
      const order = await firstValueFrom(this.orders.create(request));
      return { synced: true, order };
    } catch (error) {
      if (this.isNetworkError(error)) {
        this.enqueue(entry);
        return { synced: false };
      }
      throw error;
    }
  }

  /** Re-sends every queued order. Safe to call repeatedly (server dedupes). */
  async flush(): Promise<void> {
    if (this.syncing() || !this.connectivity.online() || this.pending().length === 0) {
      return;
    }
    this.syncing.set(true);
    try {
      for (const entry of [...this.pending()]) {
        try {
          await firstValueFrom(this.orders.create(entry.request));
          this.remove(entry.clientRequestId);
        } catch (error) {
          if (this.isNetworkError(error)) {
            break; // still offline — stop and retry on the next reconnect
          }
          this.markError(entry.clientRequestId, this.errorMessage(error));
        }
      }
    } finally {
      this.syncing.set(false);
    }
  }

  /** Manual trigger for the "sync now" action in the UI. */
  syncNow(): Promise<void> {
    return this.flush();
  }

  /** Drops a queued order (e.g. one stuck on a permanent server error). */
  discard(clientRequestId: string): void {
    this.remove(clientRequestId);
  }

  private enqueue(entry: OutboxOrder): void {
    this.pending.update((list) => [...list, entry]);
    this.persist();
  }

  private remove(clientRequestId: string): void {
    this.pending.update((list) => list.filter((e) => e.clientRequestId !== clientRequestId));
    this.persist();
  }

  private markError(clientRequestId: string, message: string): void {
    this.pending.update((list) =>
      list.map((e) => (e.clientRequestId === clientRequestId ? { ...e, lastError: message } : e)),
    );
    this.persist();
  }

  private persist(): void {
    this.storage.set(OUTBOX_KEY, this.pending());
  }

  private isNetworkError(error: unknown): boolean {
    // Angular reports a failed/unreachable request as an HttpErrorResponse with
    // status 0 (network error / no connection).
    return error instanceof HttpErrorResponse && error.status === 0;
  }

  private errorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      return error.error?.message ?? `Error ${error.status}`;
    }
    return 'Error desconocido';
  }
}

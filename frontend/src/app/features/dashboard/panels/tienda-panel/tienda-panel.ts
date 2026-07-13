import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../../../core/auth/auth.service';
import { Order } from '../../../orders/models/order.model';
import { PortalSummary } from '../../../portal/models/portal.model';
import { PortalService } from '../../../portal/portal.service';
import { orderStatusLabel, orderStatusTone } from '../../status-ui';
import { KpiCard } from '../../ui/kpi-card/kpi-card';
import { QuickAction } from '../../ui/quick-action/quick-action';

/**
 * Self-service portal home for the store owner (COMPRADOR): account statement,
 * recent orders and a one-click "repeat last order".
 */
@Component({
  selector: 'app-tienda-panel',
  imports: [CurrencyPipe, DatePipe, MatButtonModule, MatIconModule, KpiCard, QuickAction],
  templateUrl: './tienda-panel.html',
})
export class TiendaPanel {
  private readonly portalService = inject(PortalService);
  private readonly snackBar = inject(MatSnackBar);
  protected readonly auth = inject(AuthService);
  protected readonly statusLabel = orderStatusLabel;
  protected readonly statusTone = orderStatusTone;

  protected readonly summary = signal<PortalSummary | null>(null);
  protected readonly recentOrders = signal<Order[]>([]);
  protected readonly loading = signal(true);
  protected readonly repeating = signal(false);

  constructor() {
    this.load();
  }

  protected repeatLast(): void {
    this.repeating.set(true);
    this.portalService.repeatLastOrder().subscribe({
      next: (order) => {
        this.repeating.set(false);
        this.snackBar.open(`Pedido ${order.orderNumber} creado`, 'Cerrar', { duration: 3500 });
        this.load();
      },
      error: (error) => {
        this.repeating.set(false);
        this.snackBar.open(error.error?.message ?? 'No se pudo repetir el pedido.', 'Cerrar', {
          duration: 4000,
        });
      },
    });
  }

  private load(): void {
    this.loading.set(true);
    this.portalService.summary().subscribe({
      next: (summary) => {
        this.summary.set(summary);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
    this.portalService.orders(0, 6).subscribe((page) => this.recentOrders.set(page.content));
  }
}

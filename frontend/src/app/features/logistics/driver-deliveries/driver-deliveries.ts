import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { AuthService } from '../../../core/auth/auth.service';
import { Order, OrderStatus } from '../../orders/models/order.model';
import { OrderDetailDialog } from '../../orders/order-detail-dialog/order-detail-dialog';
import { OrderTransitionDialog } from '../../orders/order-transition-dialog/order-transition-dialog';
import { OrdersService } from '../../orders/orders.service';

@Component({
  selector: 'app-driver-deliveries',
  imports: [CurrencyPipe, DatePipe, MatTableModule, MatButtonModule, MatIconModule, MatMenuModule],
  templateUrl: './driver-deliveries.html',
  styleUrl: './driver-deliveries.scss',
})
export class DriverDeliveries {
  private readonly ordersService = inject(OrdersService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly displayedColumns = ['orderNumber', 'customerName', 'totalAmount', 'createdAt', 'actions'];
  protected readonly orders = signal<Order[]>([]);
  protected readonly loading = signal(false);

  constructor() {
    this.refresh();
  }

  protected viewDetail(order: Order): void {
    this.ordersService.getById(order.id).subscribe((full) => {
      this.dialog.open(OrderDetailDialog, { width: '560px', data: { order: full } });
    });
  }

  protected markDelivered(order: Order): void {
    this.openTransitionDialog(order, 'ENTREGADO', 'Marcar como entregado', 'Confirmar entrega');
  }

  protected reportIncident(order: Order): void {
    this.openTransitionDialog(order, 'NOVEDAD', 'Reportar novedad', 'Reportar novedad');
  }

  private openTransitionDialog(order: Order, targetStatus: OrderStatus, title: string, confirmLabel: string): void {
    const ref = this.dialog.open(OrderTransitionDialog, {
      width: '420px',
      data: { order, targetStatus, title, confirmLabel },
    });
    ref.afterClosed().subscribe((updated) => {
      if (updated) {
        this.refresh();
        this.snackBar.open('Entrega actualizada', 'Cerrar', { duration: 3000 });
      }
    });
  }

  private refresh(): void {
    const driverId = this.authService.currentUser()?.id;
    if (!driverId) {
      return;
    }
    this.loading.set(true);
    this.ordersService.search({ assignedDriverId: driverId, status: 'ASIGNADO_RUTA', size: 50 }).subscribe((page) => {
      this.orders.set(page.content);
      this.loading.set(false);
    });
  }
}

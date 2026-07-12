import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { Role } from '../../../core/auth/models/role.enum';
import { Order, OrderStatus } from '../../orders/models/order.model';
import { OrdersService } from '../../orders/orders.service';
import { AssignDriverDialog } from '../assign-driver-dialog/assign-driver-dialog';

@Component({
  selector: 'app-warehouse-kanban',
  imports: [CurrencyPipe, MatButtonModule, MatIconModule],
  templateUrl: './warehouse-kanban.html',
  styleUrl: './warehouse-kanban.scss',
})
export class WarehouseKanban {
  private readonly ordersService = inject(OrdersService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly approved = signal<Order[]>([]);
  protected readonly picking = signal<Order[]>([]);
  protected readonly packed = signal<Order[]>([]);
  protected readonly dispatched = signal<Order[]>([]);
  protected readonly incidents = signal<Order[]>([]);
  protected readonly loading = signal(false);

  protected readonly canCancel = computed(() => this.authService.currentUser()?.role === Role.ADMINISTRADOR);

  constructor() {
    this.refresh();
  }

  protected startPicking(order: Order): void {
    this.transition(order, 'EN_ALISTAMIENTO');
  }

  protected finishPicking(order: Order): void {
    this.transition(order, 'ALISTADO');
  }

  protected openAssignDriver(order: Order): void {
    const ref = this.dialog.open(AssignDriverDialog, { width: '440px', data: { order } });
    ref.afterClosed().subscribe((updated) => {
      if (updated) {
        this.refresh();
        this.snackBar.open('Pedido asignado a ruta', 'Cerrar', { duration: 3000 });
      }
    });
  }

  protected cancelIncident(order: Order): void {
    this.transition(order, 'CANCELADO');
  }

  private transition(order: Order, targetStatus: OrderStatus): void {
    this.ordersService.transition(order.id, { targetStatus, note: null }).subscribe({
      next: () => {
        this.refresh();
        this.snackBar.open('Pedido actualizado', 'Cerrar', { duration: 3000 });
      },
      error: (error) => {
        this.snackBar.open(error.error?.message ?? 'No se pudo actualizar el pedido.', 'Cerrar', { duration: 4000 });
      },
    });
  }

  private refresh(): void {
    this.loading.set(true);
    forkJoin({
      approved: this.ordersService.search({ status: 'APROBADO', size: 50 }),
      picking: this.ordersService.search({ status: 'EN_ALISTAMIENTO', size: 50 }),
      packed: this.ordersService.search({ status: 'ALISTADO', size: 50 }),
      dispatched: this.ordersService.search({ status: 'ASIGNADO_RUTA', size: 50 }),
      incidents: this.ordersService.search({ status: 'NOVEDAD', size: 50 }),
    }).subscribe((result) => {
      this.approved.set(result.approved.content);
      this.picking.set(result.picking.content);
      this.packed.set(result.packed.content);
      this.dispatched.set(result.dispatched.content);
      this.incidents.set(result.incidents.content);
      this.loading.set(false);
    });
  }
}

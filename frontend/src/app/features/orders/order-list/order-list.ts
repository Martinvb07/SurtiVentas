import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { AuthService } from '../../../core/auth/auth.service';
import { Role } from '../../../core/auth/models/role.enum';
import { OrdersService } from '../orders.service';
import { Order, OrderStatus } from '../models/order.model';
import { OrderDetailDialog } from '../order-detail-dialog/order-detail-dialog';
import { OrderForm, OrderFormResult } from '../order-form/order-form';
import { OrderTransitionDialog } from '../order-transition-dialog/order-transition-dialog';

@Component({
  selector: 'app-order-list',
  imports: [
    CurrencyPipe,
    DatePipe,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
  ],
  templateUrl: './order-list.html',
  styleUrl: './order-list.scss',
})
export class OrderList {
  private readonly ordersService = inject(OrdersService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly displayedColumns = ['orderNumber', 'customerName', 'status', 'totalAmount', 'createdAt', 'actions'];

  protected readonly statusOptions: OrderStatus[] = [
    'CREADO', 'PENDIENTE_APROBACION', 'APROBADO', 'EN_ALISTAMIENTO', 'ALISTADO',
    'ASIGNADO_RUTA', 'ENTREGADO', 'NOVEDAD', 'FACTURADO', 'PAGADO', 'CARTERA_PENDIENTE', 'CANCELADO',
  ];

  protected readonly status = signal<OrderStatus | null>(null);
  protected readonly pageIndex = signal(0);
  protected readonly pageSize = signal(20);

  protected readonly orders = signal<Order[]>([]);
  protected readonly totalElements = signal(0);
  protected readonly loading = signal(false);

  protected readonly canCreate = computed(() => {
    const role = this.authService.currentUser()?.role;
    return role === Role.ADMINISTRADOR || role === Role.VENDEDOR;
  });

  constructor() {
    effect(() => {
      const params = { page: this.pageIndex(), size: this.pageSize(), status: this.status() };
      this.loading.set(true);
      this.ordersService.search(params).subscribe({
        next: (page) => {
          this.orders.set(page.content);
          this.totalElements.set(page.totalElements);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
    });
  }

  protected onPage(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
  }

  protected onStatusChange(value: OrderStatus | null): void {
    this.status.set(value);
    this.pageIndex.set(0);
  }

  protected openCreateDialog(): void {
    const ref = this.dialog.open(OrderForm, { width: '720px', maxWidth: '720px' });
    ref.afterClosed().subscribe((result: OrderFormResult | undefined) => {
      if (!result) {
        return;
      }
      if (result.queued) {
        this.snackBar.open('Sin conexión: el pedido se guardó y se enviará al reconectar', 'Cerrar', { duration: 5000 });
      } else {
        this.refresh();
        this.snackBar.open('Pedido creado', 'Cerrar', { duration: 3000 });
      }
    });
  }

  protected viewDetail(order: Order): void {
    this.ordersService.getById(order.id).subscribe((full) => {
      this.dialog.open(OrderDetailDialog, { width: '560px', data: { order: full } });
    });
  }

  protected cancel(order: Order): void {
    this.openTransitionDialog(order, 'CANCELADO', 'Cancelar pedido', 'Cancelar pedido');
  }

  private openTransitionDialog(order: Order, targetStatus: OrderStatus, title: string, confirmLabel: string): void {
    const ref = this.dialog.open(OrderTransitionDialog, {
      width: '420px',
      data: { order, targetStatus, title, confirmLabel },
    });
    ref.afterClosed().subscribe((updated) => {
      if (updated) {
        this.refresh();
        this.snackBar.open('Pedido actualizado', 'Cerrar', { duration: 3000 });
      }
    });
  }

  private refresh(): void {
    this.ordersService
      .search({ page: this.pageIndex(), size: this.pageSize(), status: this.status() })
      .subscribe((page) => {
        this.orders.set(page.content);
        this.totalElements.set(page.totalElements);
      });
  }
}

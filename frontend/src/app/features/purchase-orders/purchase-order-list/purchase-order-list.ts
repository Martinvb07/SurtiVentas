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
import { PurchaseOrdersService } from '../purchase-orders.service';
import { PurchaseOrder, PurchaseOrderStatus } from '../models/purchase-order.model';
import { PurchaseOrderDetailDialog } from '../purchase-order-detail-dialog/purchase-order-detail-dialog';
import { PurchaseOrderForm } from '../purchase-order-form/purchase-order-form';
import { TransitionDialog } from '../transition-dialog/transition-dialog';

@Component({
  selector: 'app-purchase-order-list',
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
  templateUrl: './purchase-order-list.html',
  styleUrl: './purchase-order-list.scss',
})
export class PurchaseOrderList {
  private readonly purchaseOrdersService = inject(PurchaseOrdersService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly displayedColumns = ['orderNumber', 'supplierName', 'status', 'totalAmount', 'createdAt', 'actions'];

  protected readonly status = signal<PurchaseOrderStatus | null>(null);
  protected readonly pageIndex = signal(0);
  protected readonly pageSize = signal(20);

  protected readonly purchaseOrders = signal<PurchaseOrder[]>([]);
  protected readonly totalElements = signal(0);
  protected readonly loading = signal(false);

  // Only the biller (and admin) orders/sends/cancels purchase orders.
  protected readonly canManageOrder = computed(() => {
    const role = this.authService.currentUser()?.role;
    return role === Role.ADMINISTRADOR || role === Role.FACTURADOR;
  });
  // The warehouse marks physical arrival (no stock change).
  protected readonly canReceive = computed(() => {
    const role = this.authService.currentUser()?.role;
    return role === Role.ADMINISTRADOR || role === Role.BODEGUERO;
  });
  // Only the admin enters the goods into inventory (stock increase).
  protected readonly canIngest = computed(() => this.authService.currentUser()?.role === Role.ADMINISTRADOR);

  constructor() {
    effect(() => {
      const params = { page: this.pageIndex(), size: this.pageSize(), status: this.status() };
      this.loading.set(true);
      this.purchaseOrdersService.search(params).subscribe({
        next: (page) => {
          this.purchaseOrders.set(page.content);
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

  protected onStatusChange(value: PurchaseOrderStatus | null): void {
    this.status.set(value);
    this.pageIndex.set(0);
  }

  protected openCreateDialog(): void {
    const ref = this.dialog.open(PurchaseOrderForm, { width: '720px', maxWidth: '720px' });
    ref.afterClosed().subscribe((created) => {
      if (created) {
        this.refresh();
        this.snackBar.open('Orden de compra creada', 'Cerrar', { duration: 3000 });
      }
    });
  }

  protected viewDetail(purchaseOrder: PurchaseOrder): void {
    this.purchaseOrdersService.getById(purchaseOrder.id).subscribe((full) => {
      this.dialog.open(PurchaseOrderDetailDialog, { width: '560px', data: { purchaseOrder: full } });
    });
  }

  protected send(purchaseOrder: PurchaseOrder): void {
    this.openTransitionDialog(purchaseOrder, 'ENVIADA', 'Enviar orden al proveedor', 'Enviar');
  }

  protected receive(purchaseOrder: PurchaseOrder): void {
    this.openTransitionDialog(purchaseOrder, 'RECIBIDA', 'Confirmar llegada de mercancía', 'Marcar recibida');
  }

  protected ingest(purchaseOrder: PurchaseOrder): void {
    this.openTransitionDialog(purchaseOrder, 'INGRESADA', 'Ingresar al inventario', 'Ingresar al inventario');
  }

  protected cancel(purchaseOrder: PurchaseOrder): void {
    this.openTransitionDialog(purchaseOrder, 'CANCELADA', 'Cancelar orden de compra', 'Cancelar orden');
  }

  private openTransitionDialog(purchaseOrder: PurchaseOrder, targetStatus: PurchaseOrderStatus, title: string, confirmLabel: string): void {
    const ref = this.dialog.open(TransitionDialog, {
      width: '420px',
      data: { purchaseOrder, targetStatus, title, confirmLabel },
    });
    ref.afterClosed().subscribe((updated) => {
      if (updated) {
        this.refresh();
        this.snackBar.open('Orden de compra actualizada', 'Cerrar', { duration: 3000 });
      }
    });
  }

  private refresh(): void {
    this.purchaseOrdersService
      .search({ page: this.pageIndex(), size: this.pageSize(), status: this.status() })
      .subscribe((page) => {
        this.purchaseOrders.set(page.content);
        this.totalElements.set(page.totalElements);
      });
  }
}

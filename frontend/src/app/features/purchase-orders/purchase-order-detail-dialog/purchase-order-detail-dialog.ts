import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { AuthService } from '../../../core/auth/auth.service';
import { Role } from '../../../core/auth/models/role.enum';
import { PurchaseOrdersService } from '../purchase-orders.service';
import { PurchaseOrder, PurchaseOrderHistoryEntry, SupplierInvoice } from '../models/purchase-order.model';

export interface PurchaseOrderDetailDialogData {
  purchaseOrder: PurchaseOrder;
}

@Component({
  selector: 'app-purchase-order-detail-dialog',
  imports: [
    CurrencyPipe,
    DatePipe,
    MatDialogModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './purchase-order-detail-dialog.html',
  styleUrl: './purchase-order-detail-dialog.scss',
})
export class PurchaseOrderDetailDialog {
  protected readonly data = inject<PurchaseOrderDetailDialogData>(MAT_DIALOG_DATA);
  private readonly purchaseOrdersService = inject(PurchaseOrdersService);
  private readonly authService = inject(AuthService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly displayedColumns = ['productName', 'quantity', 'unitCost', 'subtotal'];
  protected readonly history = signal<PurchaseOrderHistoryEntry[]>([]);

  protected readonly invoice = signal<SupplierInvoice | null>(null);
  protected readonly loadingInvoice = signal(true);
  protected readonly uploading = signal(false);
  protected readonly showExtractedText = signal(false);

  // The paper invoice arrives with the goods, so it can be scanned once the
  // order is received (and still while it is being entered into inventory).
  protected readonly canScan = computed(() => {
    const role = this.authService.currentUser()?.role;
    const scanRoles = role === Role.ADMINISTRADOR || role === Role.FACTURADOR || role === Role.BODEGUERO;
    const scanStatus = this.data.purchaseOrder.status === 'RECIBIDA' || this.data.purchaseOrder.status === 'INGRESADA';
    return scanRoles && scanStatus;
  });

  constructor() {
    const id = this.data.purchaseOrder.id;
    this.purchaseOrdersService.getHistory(id).subscribe((history) => this.history.set(history));
    this.purchaseOrdersService.getInvoice(id).subscribe({
      next: (invoice) => {
        this.invoice.set(invoice);
        this.loadingInvoice.set(false);
      },
      error: () => this.loadingInvoice.set(false),
    });
  }

  protected onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }
    this.uploading.set(true);
    this.purchaseOrdersService.uploadInvoice(this.data.purchaseOrder.id, file).subscribe({
      next: (invoice) => {
        this.invoice.set(invoice);
        this.uploading.set(false);
        input.value = '';
        this.snackBar.open(
          invoice.matched ? 'Factura conciliada: coincide con la orden' : 'Factura escaneada: revisar diferencia',
          'Cerrar',
          { duration: 4000 },
        );
      },
      error: (err) => {
        this.uploading.set(false);
        input.value = '';
        const message = err?.error?.message ?? 'No se pudo procesar la factura';
        this.snackBar.open(message, 'Cerrar', { duration: 5000 });
      },
    });
  }

  protected viewFile(): void {
    this.purchaseOrdersService.getInvoiceFile(this.data.purchaseOrder.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        window.open(url, '_blank');
        // Give the new tab time to load before releasing the object URL.
        setTimeout(() => URL.revokeObjectURL(url), 60000);
      },
      error: () => this.snackBar.open('No se pudo abrir el archivo', 'Cerrar', { duration: 4000 }),
    });
  }

  protected toggleExtractedText(): void {
    this.showExtractedText.update((v) => !v);
  }
}

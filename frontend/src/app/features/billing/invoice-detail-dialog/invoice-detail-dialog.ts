import { CurrencyPipe, DatePipe, TitleCasePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../../core/auth/auth.service';
import { Role } from '../../../core/auth/models/role.enum';
import { BillingService } from '../billing.service';
import { Invoice, PaymentReceipt } from '../models/invoice.model';

export interface InvoiceDetailDialogData {
  invoice: Invoice;
}

@Component({
  selector: 'app-invoice-detail-dialog',
  imports: [
    CurrencyPipe,
    DatePipe,
    TitleCasePipe,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './invoice-detail-dialog.html',
  styleUrl: './invoice-detail-dialog.scss',
})
export class InvoiceDetailDialog {
  protected readonly data = inject<InvoiceDetailDialogData>(MAT_DIALOG_DATA);
  private readonly billingService = inject(BillingService);
  private readonly authService = inject(AuthService);
  private readonly snackBar = inject(MatSnackBar);

  /** paymentId -> receipt (null once loaded with none, undefined while loading). */
  protected readonly receipts = signal<Record<number, PaymentReceipt | null>>({});
  protected readonly uploading = signal<number | null>(null);

  protected readonly canScan = computed(() => {
    const role = this.authService.currentUser()?.role;
    return role === Role.FACTURADOR || role === Role.ADMINISTRADOR;
  });

  constructor() {
    for (const payment of this.data.invoice.payments) {
      this.billingService.getReceipt(payment.id).subscribe((receipt) =>
        this.receipts.update((map) => ({ ...map, [payment.id]: receipt })),
      );
    }
  }

  protected receiptFor(paymentId: number): PaymentReceipt | null | undefined {
    return this.receipts()[paymentId];
  }

  protected onFileSelected(event: Event, paymentId: number): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }
    this.uploading.set(paymentId);
    this.billingService.uploadReceipt(paymentId, file).subscribe({
      next: (receipt) => {
        this.receipts.update((map) => ({ ...map, [paymentId]: receipt }));
        this.uploading.set(null);
        input.value = '';
        this.snackBar.open(
          receipt.matched ? 'Comprobante conciliado: coincide con el abono' : 'Comprobante escaneado: revisar diferencia',
          'Cerrar',
          { duration: 4000 },
        );
      },
      error: (err) => {
        this.uploading.set(null);
        input.value = '';
        this.snackBar.open(err?.error?.message ?? 'No se pudo procesar el comprobante', 'Cerrar', { duration: 5000 });
      },
    });
  }

  protected viewReceipt(paymentId: number): void {
    this.billingService.getReceiptFile(paymentId).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        window.open(url, '_blank');
        setTimeout(() => URL.revokeObjectURL(url), 60000);
      },
      error: () => this.snackBar.open('No se pudo abrir el comprobante', 'Cerrar', { duration: 4000 }),
    });
  }
}

import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { BillingService } from '../billing.service';
import { GenerateInvoiceDialog } from '../generate-invoice-dialog/generate-invoice-dialog';
import { BillableOrder } from '../models/invoice.model';

@Component({
  selector: 'app-billable-orders',
  imports: [CurrencyPipe, DatePipe, MatTableModule, MatButtonModule, MatIconModule],
  templateUrl: './billable-orders.html',
  styleUrl: './billable-orders.scss',
})
export class BillableOrders {
  private readonly billingService = inject(BillingService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly displayedColumns = ['orderNumber', 'customerName', 'totalAmount', 'createdAt', 'actions'];
  protected readonly orders = signal<BillableOrder[]>([]);
  protected readonly loading = signal(false);

  constructor() {
    this.refresh();
  }

  protected generate(order: BillableOrder): void {
    const ref = this.dialog.open(GenerateInvoiceDialog, {
      width: '780px',
      maxWidth: '780px',
      data: { order },
    });
    ref.afterClosed().subscribe((created) => {
      if (created) {
        this.refresh();
        this.snackBar.open(`Factura ${created.invoiceNumber} generada`, 'Cerrar', { duration: 3500 });
      }
    });
  }

  private refresh(): void {
    this.loading.set(true);
    this.billingService.billableOrders().subscribe({
      next: (orders) => {
        this.orders.set(orders);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}

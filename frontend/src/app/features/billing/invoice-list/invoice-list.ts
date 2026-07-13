import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, effect, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { BillingService } from '../billing.service';
import { InvoiceDetailDialog } from '../invoice-detail-dialog/invoice-detail-dialog';
import { Invoice, InvoiceStatus } from '../models/invoice.model';
import { RegisterPaymentDialog } from '../register-payment-dialog/register-payment-dialog';

@Component({
  selector: 'app-invoice-list',
  imports: [
    CurrencyPipe,
    DatePipe,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
  ],
  templateUrl: './invoice-list.html',
  styleUrl: './invoice-list.scss',
})
export class InvoiceList {
  private readonly billingService = inject(BillingService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly displayedColumns = [
    'invoiceNumber',
    'customerName',
    'totalAmount',
    'balance',
    'dueDate',
    'status',
    'actions',
  ];

  protected readonly status = signal<InvoiceStatus | null>(null);
  protected readonly overdue = signal(false);
  protected readonly pageIndex = signal(0);
  protected readonly pageSize = signal(20);

  protected readonly invoices = signal<Invoice[]>([]);
  protected readonly totalElements = signal(0);
  protected readonly loading = signal(false);

  constructor() {
    effect(() => {
      const params = {
        page: this.pageIndex(),
        size: this.pageSize(),
        status: this.status(),
        overdue: this.overdue(),
      };
      this.loading.set(true);
      this.billingService.search(params).subscribe({
        next: (page) => {
          this.invoices.set(page.content);
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

  protected onStatusChange(value: InvoiceStatus | null): void {
    this.status.set(value);
    this.pageIndex.set(0);
  }

  protected onOverdueChange(value: boolean): void {
    this.overdue.set(value);
    this.pageIndex.set(0);
  }

  protected statusLabel(status: InvoiceStatus): string {
    return { PENDIENTE: 'Pendiente', PARCIAL: 'Parcial', PAGADA: 'Pagada' }[status];
  }

  protected statusTone(invoice: Invoice): 'success' | 'warning' | 'danger' {
    if (invoice.overdue) return 'danger';
    if (invoice.status === 'PAGADA') return 'success';
    return 'warning';
  }

  protected viewDetail(invoice: Invoice): void {
    this.billingService.getById(invoice.id).subscribe((full) => {
      this.dialog.open(InvoiceDetailDialog, { width: '520px', data: { invoice: full } });
    });
  }

  protected registerPayment(invoice: Invoice): void {
    const ref = this.dialog.open(RegisterPaymentDialog, { width: '440px', data: { invoice } });
    ref.afterClosed().subscribe((updated) => {
      if (updated) {
        this.refresh();
        this.snackBar.open('Abono registrado', 'Cerrar', { duration: 3000 });
      }
    });
  }

  private refresh(): void {
    this.billingService
      .search({
        page: this.pageIndex(),
        size: this.pageSize(),
        status: this.status(),
        overdue: this.overdue(),
      })
      .subscribe((page) => {
        this.invoices.set(page.content);
        this.totalElements.set(page.totalElements);
      });
  }
}

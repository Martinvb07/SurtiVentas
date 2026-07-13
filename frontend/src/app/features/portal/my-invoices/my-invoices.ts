import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { Invoice, InvoiceStatus } from '../../billing/models/invoice.model';
import { PortalService } from '../portal.service';

@Component({
  selector: 'app-my-invoices',
  imports: [CurrencyPipe, DatePipe, MatTableModule, MatPaginatorModule, MatIconModule],
  templateUrl: './my-invoices.html',
  styleUrl: './my-invoices.scss',
})
export class MyInvoices {
  private readonly portalService = inject(PortalService);

  protected readonly displayedColumns = ['invoiceNumber', 'totalAmount', 'balance', 'dueDate', 'status'];
  protected readonly invoices = signal<Invoice[]>([]);
  protected readonly totalElements = signal(0);
  protected readonly pageIndex = signal(0);
  protected readonly pageSize = signal(20);
  protected readonly loading = signal(false);

  constructor() {
    this.load();
  }

  protected onPage(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.load();
  }

  protected statusLabel(status: InvoiceStatus): string {
    return { PENDIENTE: 'Pendiente', PARCIAL: 'Parcial', PAGADA: 'Pagada' }[status];
  }

  protected statusTone(invoice: Invoice): 'success' | 'warning' | 'danger' {
    if (invoice.overdue) return 'danger';
    if (invoice.status === 'PAGADA') return 'success';
    return 'warning';
  }

  private load(): void {
    this.loading.set(true);
    this.portalService.invoices(this.pageIndex(), this.pageSize()).subscribe({
      next: (page) => {
        this.invoices.set(page.content);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}

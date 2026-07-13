import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { orderStatusLabel, orderStatusTone } from '../../dashboard/status-ui';
import { Order } from '../../orders/models/order.model';
import { PortalService } from '../portal.service';

@Component({
  selector: 'app-my-orders',
  imports: [CurrencyPipe, DatePipe, MatTableModule, MatPaginatorModule, MatIconModule],
  templateUrl: './my-orders.html',
  styleUrl: './my-orders.scss',
})
export class MyOrders {
  private readonly portalService = inject(PortalService);
  protected readonly statusLabel = orderStatusLabel;
  protected readonly statusTone = orderStatusTone;

  protected readonly displayedColumns = ['orderNumber', 'status', 'totalAmount', 'createdAt'];
  protected readonly orders = signal<Order[]>([]);
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

  private load(): void {
    this.loading.set(true);
    this.portalService.orders(this.pageIndex(), this.pageSize()).subscribe({
      next: (page) => {
        this.orders.set(page.content);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}

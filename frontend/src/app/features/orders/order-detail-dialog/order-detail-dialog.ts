import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatTableModule } from '@angular/material/table';
import { OrdersService } from '../orders.service';
import { Order, OrderHistoryEntry } from '../models/order.model';

export interface OrderDetailDialogData {
  order: Order;
}

@Component({
  selector: 'app-order-detail-dialog',
  imports: [CurrencyPipe, DatePipe, MatDialogModule, MatTableModule, MatButtonModule],
  templateUrl: './order-detail-dialog.html',
  styleUrl: './order-detail-dialog.scss',
})
export class OrderDetailDialog {
  protected readonly data = inject<OrderDetailDialogData>(MAT_DIALOG_DATA);
  private readonly ordersService = inject(OrdersService);

  protected readonly displayedColumns = ['productName', 'quantity', 'unitPrice', 'subtotal'];
  protected readonly history = signal<OrderHistoryEntry[]>([]);

  constructor() {
    this.ordersService.getHistory(this.data.order.id).subscribe((history) => this.history.set(history));
  }
}

import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatTableModule } from '@angular/material/table';
import { PurchaseOrdersService } from '../purchase-orders.service';
import { PurchaseOrder, PurchaseOrderHistoryEntry } from '../models/purchase-order.model';

export interface PurchaseOrderDetailDialogData {
  purchaseOrder: PurchaseOrder;
}

@Component({
  selector: 'app-purchase-order-detail-dialog',
  imports: [CurrencyPipe, DatePipe, MatDialogModule, MatTableModule, MatButtonModule],
  templateUrl: './purchase-order-detail-dialog.html',
  styleUrl: './purchase-order-detail-dialog.scss',
})
export class PurchaseOrderDetailDialog {
  protected readonly data = inject<PurchaseOrderDetailDialogData>(MAT_DIALOG_DATA);
  private readonly purchaseOrdersService = inject(PurchaseOrdersService);

  protected readonly displayedColumns = ['productName', 'quantity', 'unitCost', 'subtotal'];
  protected readonly history = signal<PurchaseOrderHistoryEntry[]>([]);

  constructor() {
    this.purchaseOrdersService.getHistory(this.data.purchaseOrder.id).subscribe((history) => this.history.set(history));
  }
}

import { CurrencyPipe, DatePipe, TitleCasePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { Invoice } from '../models/invoice.model';

export interface InvoiceDetailDialogData {
  invoice: Invoice;
}

@Component({
  selector: 'app-invoice-detail-dialog',
  imports: [CurrencyPipe, DatePipe, TitleCasePipe, MatDialogModule],
  templateUrl: './invoice-detail-dialog.html',
  styleUrl: './invoice-detail-dialog.scss',
})
export class InvoiceDetailDialog {
  protected readonly data = inject<InvoiceDetailDialogData>(MAT_DIALOG_DATA);
}

import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { BillingService } from '../billing.service';
import { BillableOrder, Invoice } from '../models/invoice.model';

export interface GenerateInvoiceDialogData {
  order: BillableOrder;
}

@Component({
  selector: 'app-generate-invoice-dialog',
  imports: [
    CurrencyPipe,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatButtonModule,
  ],
  templateUrl: './generate-invoice-dialog.html',
  styleUrl: './generate-invoice-dialog.scss',
})
export class GenerateInvoiceDialog {
  private readonly fb = inject(FormBuilder);
  private readonly billingService = inject(BillingService);
  private readonly dialogRef = inject(MatDialogRef<GenerateInvoiceDialog>);
  protected readonly data = inject<GenerateInvoiceDialogData>(MAT_DIALOG_DATA);

  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly minDate = new Date();

  protected readonly form = this.fb.nonNullable.group({
    dueDate: [this.defaultDueDate(), Validators.required],
  });

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);
    const dueDate = this.form.getRawValue().dueDate;

    this.billingService
      .generate({ orderId: this.data.order.orderId, dueDate: this.formatDate(dueDate) })
      .subscribe({
        next: (invoice: Invoice) => {
          this.saving.set(false);
          this.dialogRef.close(invoice);
        },
        error: (error) => {
          this.saving.set(false);
          this.errorMessage.set(error.error?.message ?? 'No se pudo generar la factura.');
        },
      });
  }

  private defaultDueDate(): Date {
    const date = new Date();
    date.setDate(date.getDate() + 30);
    return date;
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}

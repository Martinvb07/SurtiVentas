import { CurrencyPipe, TitleCasePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { BillingService } from '../billing.service';
import { Invoice, PaymentMethod } from '../models/invoice.model';

export interface RegisterPaymentDialogData {
  invoice: Invoice;
}

@Component({
  selector: 'app-register-payment-dialog',
  imports: [
    CurrencyPipe,
    TitleCasePipe,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
  ],
  templateUrl: './register-payment-dialog.html',
  styleUrl: './register-payment-dialog.scss',
})
export class RegisterPaymentDialog {
  private readonly fb = inject(FormBuilder);
  private readonly billingService = inject(BillingService);
  private readonly dialogRef = inject(MatDialogRef<RegisterPaymentDialog>);
  protected readonly data = inject<RegisterPaymentDialogData>(MAT_DIALOG_DATA);

  protected readonly methods: PaymentMethod[] = ['EFECTIVO', 'TRANSFERENCIA', 'TARJETA', 'OTRO'];
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    amount: [
      this.data.invoice.balance,
      [Validators.required, Validators.min(0.01), Validators.max(this.data.invoice.balance)],
    ],
    method: ['EFECTIVO' as PaymentMethod, Validators.required],
    reference: [''],
  });

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);
    const value = this.form.getRawValue();

    this.billingService
      .registerPayment(this.data.invoice.id, {
        amount: value.amount,
        method: value.method,
        reference: value.reference.trim() || null,
      })
      .subscribe({
        next: (invoice) => {
          this.saving.set(false);
          this.dialogRef.close(invoice);
        },
        error: (error) => {
          this.saving.set(false);
          this.errorMessage.set(error.error?.message ?? 'No se pudo registrar el abono.');
        },
      });
  }
}

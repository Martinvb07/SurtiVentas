import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Employee } from '../models/payroll.model';
import { PayrollService } from '../payroll.service';

export interface PaymentDialogData {
  employee: Employee;
}

@Component({
  selector: 'app-payment-dialog',
  imports: [CurrencyPipe, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './payment-dialog.html',
  styleUrl: './payment-dialog.scss',
})
export class PaymentDialog {
  private readonly fb = inject(FormBuilder);
  private readonly payroll = inject(PayrollService);
  private readonly dialogRef = inject(MatDialogRef<PaymentDialog>);
  protected readonly data = inject<PaymentDialogData>(MAT_DIALOG_DATA);

  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    amount: [this.data.employee.salary, [Validators.required, Validators.min(0.01)]],
    period: [this.currentPeriod(), [Validators.required, Validators.maxLength(30)]],
    note: [''],
  });

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.errorMessage.set(null);
    const value = this.form.getRawValue();

    this.payroll
      .registerPayment(this.data.employee.id, {
        amount: value.amount,
        period: value.period,
        note: value.note.trim() || null,
      })
      .subscribe({
        next: (payment) => {
          this.saving.set(false);
          this.dialogRef.close(payment);
        },
        error: (error) => {
          this.saving.set(false);
          this.errorMessage.set(error.error?.message ?? 'No se pudo registrar el pago.');
        },
      });
  }

  private currentPeriod(): string {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
  }
}

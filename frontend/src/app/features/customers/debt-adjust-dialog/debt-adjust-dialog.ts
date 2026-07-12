import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { CustomersService } from '../customers.service';
import { Customer } from '../models/customer.model';

export interface DebtAdjustDialogData {
  customer: Customer;
}

@Component({
  selector: 'app-debt-adjust-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatButtonToggleModule,
  ],
  templateUrl: './debt-adjust-dialog.html',
  styleUrl: './debt-adjust-dialog.scss',
})
export class DebtAdjustDialog {
  private readonly fb = inject(FormBuilder);
  private readonly customersService = inject(CustomersService);
  private readonly dialogRef = inject(MatDialogRef<DebtAdjustDialog>);
  protected readonly data = inject<DebtAdjustDialogData>(MAT_DIALOG_DATA);

  protected readonly direction = signal<'charge' | 'payment'>('charge');
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    amount: [1, [Validators.required, Validators.min(1)]],
    reason: ['', [Validators.required, Validators.maxLength(255)]],
  });

  protected get resultingDebt(): number {
    const amount = this.form.controls.amount.value ?? 0;
    const delta = this.direction() === 'charge' ? amount : -amount;
    return this.data.customer.currentDebt + delta;
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);
    const value = this.form.getRawValue();
    const amountDelta = this.direction() === 'charge' ? value.amount : -value.amount;

    this.customersService.adjustDebt(this.data.customer.id, { amountDelta, reason: value.reason }).subscribe({
      next: (customer) => {
        this.saving.set(false);
        this.dialogRef.close(customer);
      },
      error: (error) => {
        this.saving.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudo ajustar la cartera.');
      },
    });
  }
}

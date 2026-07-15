import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { Employee, PayrollPayment } from '../models/payroll.model';
import { PayrollService } from '../payroll.service';

export interface PaymentsDialogData {
  employee: Employee;
}

@Component({
  selector: 'app-payments-dialog',
  imports: [CurrencyPipe, DatePipe, MatDialogModule],
  templateUrl: './payments-dialog.html',
  styleUrl: './payments-dialog.scss',
})
export class PaymentsDialog {
  private readonly payroll = inject(PayrollService);
  protected readonly data = inject<PaymentsDialogData>(MAT_DIALOG_DATA);

  protected readonly payments = signal<PayrollPayment[]>([]);
  protected readonly loading = signal(true);

  constructor() {
    this.payroll.payments(this.data.employee.id).subscribe({
      next: (payments) => {
        this.payments.set(payments);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}

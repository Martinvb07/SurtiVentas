import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { EmployeeForm } from '../employee-form/employee-form';
import { Employee } from '../models/payroll.model';
import { PaymentDialog } from '../payment-dialog/payment-dialog';
import { PaymentsDialog } from '../payments-dialog/payments-dialog';
import { PayrollService } from '../payroll.service';

@Component({
  selector: 'app-employee-list',
  imports: [CurrencyPipe, MatTableModule, MatButtonModule, MatIconModule, MatMenuModule],
  templateUrl: './employee-list.html',
  styleUrl: './employee-list.scss',
})
export class EmployeeList {
  private readonly payroll = inject(PayrollService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly displayedColumns = ['fullName', 'position', 'salary', 'status', 'actions'];
  protected readonly employees = signal<Employee[]>([]);
  protected readonly loading = signal(false);

  constructor() {
    this.refresh();
  }

  protected openCreate(): void {
    this.dialog
      .open(EmployeeForm, { width: '460px' })
      .afterClosed()
      .subscribe((created) => {
        if (created) {
          this.refresh();
          this.snackBar.open('Empleado creado', 'Cerrar', { duration: 3000 });
        }
      });
  }

  protected edit(employee: Employee): void {
    this.dialog
      .open(EmployeeForm, { width: '460px', data: { employee } })
      .afterClosed()
      .subscribe((updated) => {
        if (updated) {
          this.refresh();
          this.snackBar.open('Empleado actualizado', 'Cerrar', { duration: 3000 });
        }
      });
  }

  protected registerPayment(employee: Employee): void {
    this.dialog
      .open(PaymentDialog, { width: '440px', data: { employee } })
      .afterClosed()
      .subscribe((paid) => {
        if (paid) {
          this.snackBar.open('Pago de nómina registrado', 'Cerrar', { duration: 3000 });
        }
      });
  }

  protected viewPayments(employee: Employee): void {
    this.dialog.open(PaymentsDialog, { width: '540px', data: { employee } });
  }

  protected deactivate(employee: Employee): void {
    this.payroll.deactivate(employee.id).subscribe(() => {
      this.refresh();
      this.snackBar.open('Empleado desactivado', 'Cerrar', { duration: 3000 });
    });
  }

  private refresh(): void {
    this.loading.set(true);
    this.payroll.employees().subscribe({
      next: (employees) => {
        this.employees.set(employees);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}

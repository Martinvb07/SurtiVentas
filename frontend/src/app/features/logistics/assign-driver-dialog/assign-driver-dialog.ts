import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Order } from '../../orders/models/order.model';
import { OrdersService } from '../../orders/orders.service';
import { Driver } from '../models/driver.model';
import { UsersService } from '../users.service';

export interface AssignDriverDialogData {
  order: Order;
}

@Component({
  selector: 'app-assign-driver-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
  templateUrl: './assign-driver-dialog.html',
  styleUrl: './assign-driver-dialog.scss',
})
export class AssignDriverDialog {
  private readonly fb = inject(FormBuilder);
  private readonly usersService = inject(UsersService);
  private readonly ordersService = inject(OrdersService);
  private readonly dialogRef = inject(MatDialogRef<AssignDriverDialog>);
  protected readonly data = inject<AssignDriverDialogData>(MAT_DIALOG_DATA);

  protected readonly drivers = signal<Driver[]>([]);
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    driverId: [null as number | null, Validators.required],
    note: [''],
  });

  constructor() {
    this.usersService.searchByRole('CONDUCTOR').subscribe((drivers) => this.drivers.set(drivers));
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);
    const value = this.form.getRawValue();

    this.ordersService
      .transition(this.data.order.id, { targetStatus: 'ASIGNADO_RUTA', note: value.note || null, driverId: value.driverId })
      .subscribe({
        next: (order) => {
          this.saving.set(false);
          this.dialogRef.close(order);
        },
        error: (error) => {
          this.saving.set(false);
          this.errorMessage.set(error.error?.message ?? 'No se pudo asignar el conductor.');
        },
      });
  }
}

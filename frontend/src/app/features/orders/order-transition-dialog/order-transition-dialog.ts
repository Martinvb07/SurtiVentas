import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { OrdersService } from '../orders.service';
import { Order, OrderStatus } from '../models/order.model';

export interface OrderTransitionDialogData {
  order: Order;
  targetStatus: OrderStatus;
  title: string;
  confirmLabel: string;
}

@Component({
  selector: 'app-order-transition-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './order-transition-dialog.html',
  styleUrl: './order-transition-dialog.scss',
})
export class OrderTransitionDialog {
  private readonly fb = inject(FormBuilder);
  private readonly ordersService = inject(OrdersService);
  private readonly dialogRef = inject(MatDialogRef<OrderTransitionDialog>);
  protected readonly data = inject<OrderTransitionDialogData>(MAT_DIALOG_DATA);

  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    note: [''],
  });

  protected submit(): void {
    this.saving.set(true);
    this.errorMessage.set(null);
    const value = this.form.getRawValue();

    this.ordersService
      .transition(this.data.order.id, { targetStatus: this.data.targetStatus, note: value.note || null })
      .subscribe({
        next: (order) => {
          this.saving.set(false);
          this.dialogRef.close(order);
        },
        error: (error) => {
          this.saving.set(false);
          this.errorMessage.set(error.error?.message ?? 'No se pudo actualizar el estado del pedido.');
        },
      });
  }
}

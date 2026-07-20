import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PurchaseOrdersService } from '../purchase-orders.service';
import { PurchaseOrder, PurchaseOrderStatus } from '../models/purchase-order.model';

export interface TransitionDialogData {
  purchaseOrder: PurchaseOrder;
  targetStatus: PurchaseOrderStatus;
  title: string;
  confirmLabel: string;
}

const STATUS_LABELS: Record<PurchaseOrderStatus, string> = {
  BORRADOR: 'Borrador',
  ENVIADA: 'Enviada',
  RECIBIDA: 'Recibida',
  INGRESADA: 'Ingresada',
  CANCELADA: 'Cancelada',
};

const STATUS_ICONS: Record<PurchaseOrderStatus, string> = {
  BORRADOR: 'edit_note',
  ENVIADA: 'send',
  RECIBIDA: 'inventory_2',
  INGRESADA: 'add_box',
  CANCELADA: 'block',
};

@Component({
  selector: 'app-transition-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './transition-dialog.html',
  styleUrl: './transition-dialog.scss',
})
export class TransitionDialog {
  private readonly fb = inject(FormBuilder);
  private readonly purchaseOrdersService = inject(PurchaseOrdersService);
  private readonly dialogRef = inject(MatDialogRef<TransitionDialog>);
  protected readonly data = inject<TransitionDialogData>(MAT_DIALOG_DATA);

  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly destructive = computed(() => this.data.targetStatus === 'CANCELADA');
  protected readonly icon = computed(() => STATUS_ICONS[this.data.targetStatus]);

  protected readonly form = this.fb.nonNullable.group({
    note: [''],
  });

  protected label(status: PurchaseOrderStatus): string {
    return STATUS_LABELS[status];
  }

  protected submit(): void {
    this.saving.set(true);
    this.errorMessage.set(null);
    const value = this.form.getRawValue();

    this.purchaseOrdersService
      .transition(this.data.purchaseOrder.id, { targetStatus: this.data.targetStatus, note: value.note || null })
      .subscribe({
        next: (purchaseOrder) => {
          this.saving.set(false);
          this.dialogRef.close(purchaseOrder);
        },
        error: (error) => {
          this.saving.set(false);
          this.errorMessage.set(error.error?.message ?? 'No se pudo actualizar el estado de la orden.');
        },
      });
  }
}

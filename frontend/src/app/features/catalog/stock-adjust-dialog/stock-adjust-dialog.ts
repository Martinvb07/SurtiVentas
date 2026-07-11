import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { CatalogService } from '../catalog.service';
import { Product } from '../models/product.model';

export interface StockAdjustDialogData {
  product: Product;
}

@Component({
  selector: 'app-stock-adjust-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatButtonToggleModule,
  ],
  templateUrl: './stock-adjust-dialog.html',
  styleUrl: './stock-adjust-dialog.scss',
})
export class StockAdjustDialog {
  private readonly fb = inject(FormBuilder);
  private readonly catalogService = inject(CatalogService);
  private readonly dialogRef = inject(MatDialogRef<StockAdjustDialog>);
  protected readonly data = inject<StockAdjustDialogData>(MAT_DIALOG_DATA);

  protected readonly direction = signal<'in' | 'out'>('in');
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    quantity: [1, [Validators.required, Validators.min(1)]],
    reason: ['', [Validators.required, Validators.maxLength(255)]],
  });

  protected get resultingStock(): number {
    const qty = this.form.controls.quantity.value ?? 0;
    const delta = this.direction() === 'in' ? qty : -qty;
    return this.data.product.stock + delta;
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);
    const value = this.form.getRawValue();
    const quantityDelta = this.direction() === 'in' ? value.quantity : -value.quantity;

    this.catalogService.adjustStock(this.data.product.id, { quantityDelta, reason: value.reason }).subscribe({
      next: (product) => {
        this.saving.set(false);
        this.dialogRef.close(product);
      },
      error: (error) => {
        this.saving.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudo ajustar el stock.');
      },
    });
  }
}

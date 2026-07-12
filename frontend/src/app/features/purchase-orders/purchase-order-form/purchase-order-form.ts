import { Component, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { SuppliersService } from '../../suppliers/suppliers.service';
import { Supplier, SupplierProduct } from '../../suppliers/models/supplier.model';
import { PurchaseOrdersService } from '../purchase-orders.service';

@Component({
  selector: 'app-purchase-order-form',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './purchase-order-form.html',
  styleUrl: './purchase-order-form.scss',
})
export class PurchaseOrderForm {
  private readonly fb = inject(FormBuilder);
  private readonly suppliersService = inject(SuppliersService);
  private readonly purchaseOrdersService = inject(PurchaseOrdersService);
  private readonly dialogRef = inject(MatDialogRef<PurchaseOrderForm>);

  protected readonly suppliers = signal<Supplier[]>([]);
  protected readonly supplierCatalog = signal<SupplierProduct[]>([]);
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    supplierId: [null as number | null, Validators.required],
    expectedDate: [null as Date | null],
    lines: this.fb.array([this.buildLineGroup()]),
  });

  protected get lines(): FormArray {
    return this.form.controls.lines;
  }

  constructor() {
    this.suppliersService.search({ active: true, size: 100 }).subscribe((page) => this.suppliers.set(page.content));
  }

  protected onSupplierChange(supplierId: number): void {
    this.suppliersService.getProducts(supplierId).subscribe((products) => this.supplierCatalog.set(products));
  }

  protected onProductChange(lineIndex: number, productId: number): void {
    const supplierProduct = this.supplierCatalog().find((sp) => sp.productId === productId);
    if (supplierProduct) {
      this.lines.at(lineIndex).get('unitCost')?.setValue(supplierProduct.cost);
    }
  }

  protected addLine(): void {
    this.lines.push(this.buildLineGroup());
  }

  protected removeLine(index: number): void {
    if (this.lines.length > 1) {
      this.lines.removeAt(index);
    }
  }

  protected get total(): number {
    return this.lines.controls.reduce((sum, control) => {
      const quantity = control.get('quantity')?.value ?? 0;
      const unitCost = control.get('unitCost')?.value ?? 0;
      return sum + quantity * unitCost;
    }, 0);
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);
    const value = this.form.getRawValue();

    this.purchaseOrdersService
      .create({
        supplierId: value.supplierId!,
        expectedDate: value.expectedDate ? this.formatDate(value.expectedDate) : null,
        lines: value.lines.map((line) => ({
          productId: line.productId!,
          quantity: line.quantity,
          unitCost: line.unitCost,
        })),
      })
      .subscribe({
        next: (purchaseOrder) => {
          this.saving.set(false);
          this.dialogRef.close(purchaseOrder);
        },
        error: (error) => {
          this.saving.set(false);
          this.errorMessage.set(error.error?.message ?? 'No se pudo crear la orden de compra.');
        },
      });
  }

  private buildLineGroup() {
    return this.fb.nonNullable.group({
      productId: [null as number | null, Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
      unitCost: [0, [Validators.required, Validators.min(0)]],
    });
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}

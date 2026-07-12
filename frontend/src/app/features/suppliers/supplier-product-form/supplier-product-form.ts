import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { CatalogService } from '../../catalog/catalog.service';
import { Product } from '../../catalog/models/product.model';
import { SuppliersService } from '../suppliers.service';
import { SupplierProduct } from '../models/supplier.model';

export interface SupplierProductFormData {
  supplierId: number;
  supplierProduct?: SupplierProduct;
}

@Component({
  selector: 'app-supplier-product-form',
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
  templateUrl: './supplier-product-form.html',
  styleUrl: './supplier-product-form.scss',
})
export class SupplierProductForm {
  private readonly fb = inject(FormBuilder);
  private readonly catalogService = inject(CatalogService);
  private readonly suppliersService = inject(SuppliersService);
  private readonly dialogRef = inject(MatDialogRef<SupplierProductForm>);
  protected readonly data = inject<SupplierProductFormData>(MAT_DIALOG_DATA);

  protected readonly products = signal<Product[]>([]);
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly isEdit = !!this.data.supplierProduct;

  protected readonly form = this.fb.nonNullable.group({
    productId: [this.data.supplierProduct?.productId ?? null, Validators.required],
    supplierSku: [this.data.supplierProduct?.supplierSku ?? ''],
    cost: [this.data.supplierProduct?.cost ?? 0, [Validators.required, Validators.min(0)]],
  });

  constructor() {
    this.catalogService.searchProducts({ active: true, size: 200 }).subscribe((page) => this.products.set(page.content));
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);
    const value = this.form.getRawValue();
    const request = { productId: value.productId!, supplierSku: value.supplierSku || null, cost: value.cost };

    const request$ = this.isEdit
      ? this.suppliersService.updateProduct(this.data.supplierId, this.data.supplierProduct!.id, request)
      : this.suppliersService.addProduct(this.data.supplierId, request);

    request$.subscribe({
      next: (supplierProduct) => {
        this.saving.set(false);
        this.dialogRef.close(supplierProduct);
      },
      error: (error) => {
        this.saving.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudo guardar el producto del proveedor.');
      },
    });
  }
}

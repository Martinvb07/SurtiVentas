import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { CatalogService } from '../catalog.service';
import { Category } from '../models/category.model';
import { Product } from '../models/product.model';
import { UnitOfMeasure } from '../models/unit-of-measure.model';

export interface ProductFormData {
  product?: Product;
  categories: Category[];
}

@Component({
  selector: 'app-product-form',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatButtonModule,
  ],
  templateUrl: './product-form.html',
  styleUrl: './product-form.scss',
})
export class ProductForm {
  private readonly fb = inject(FormBuilder);
  private readonly catalogService = inject(CatalogService);
  private readonly dialogRef = inject(MatDialogRef<ProductForm>);
  protected readonly data = inject<ProductFormData>(MAT_DIALOG_DATA);

  protected readonly units = signal<UnitOfMeasure[]>([]);
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly isEdit = computed(() => !!this.data.product);

  protected readonly form = this.fb.nonNullable.group({
    sku: [this.data.product?.sku ?? '', [Validators.required, Validators.maxLength(40)]],
    name: [this.data.product?.name ?? '', [Validators.required, Validators.maxLength(150)]],
    description: [this.data.product?.description ?? ''],
    categoryId: [this.data.product?.category.id ?? null, Validators.required],
    unitOfMeasureId: [this.data.product?.unitOfMeasure.id ?? null, Validators.required],
    price: [this.data.product?.price ?? 0, [Validators.required, Validators.min(0)]],
    initialStock: [0, [Validators.required, Validators.min(0)]],
    minStock: [this.data.product?.minStock ?? 0, [Validators.required, Validators.min(0)]],
    batchTracked: [this.data.product?.batchTracked ?? false],
    active: [this.data.product?.active ?? true],
  });

  constructor() {
    this.catalogService.getUnits().subscribe((units) => this.units.set(units));
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);
    const value = this.form.getRawValue();

    const request$ = this.isEdit()
      ? this.catalogService.updateProduct(this.data.product!.id, {
          name: value.name,
          description: value.description || null,
          categoryId: value.categoryId!,
          unitOfMeasureId: value.unitOfMeasureId!,
          price: value.price,
          minStock: value.minStock,
          batchTracked: value.batchTracked,
          active: value.active,
        })
      : this.catalogService.createProduct({
          sku: value.sku,
          name: value.name,
          description: value.description || null,
          categoryId: value.categoryId!,
          unitOfMeasureId: value.unitOfMeasureId!,
          price: value.price,
          initialStock: value.initialStock,
          minStock: value.minStock,
          batchTracked: value.batchTracked,
        });

    request$.subscribe({
      next: (product) => {
        this.saving.set(false);
        this.dialogRef.close(product);
      },
      error: (error) => {
        this.saving.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudo guardar el producto.');
      },
    });
  }
}

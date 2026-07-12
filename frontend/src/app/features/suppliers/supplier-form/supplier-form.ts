import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { SuppliersService } from '../suppliers.service';
import { Supplier } from '../models/supplier.model';

export interface SupplierFormData {
  supplier?: Supplier;
}

@Component({
  selector: 'app-supplier-form',
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatCheckboxModule, MatButtonModule],
  templateUrl: './supplier-form.html',
  styleUrl: './supplier-form.scss',
})
export class SupplierForm {
  private readonly fb = inject(FormBuilder);
  private readonly suppliersService = inject(SuppliersService);
  private readonly dialogRef = inject(MatDialogRef<SupplierForm>);
  protected readonly data = inject<SupplierFormData>(MAT_DIALOG_DATA, { optional: true }) ?? {};

  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly isEdit = computed(() => !!this.data.supplier);

  protected readonly form = this.fb.nonNullable.group({
    name: [this.data.supplier?.name ?? '', [Validators.required, Validators.maxLength(150)]],
    contactName: [this.data.supplier?.contactName ?? '', [Validators.required, Validators.maxLength(150)]],
    phone: [this.data.supplier?.phone ?? ''],
    email: [this.data.supplier?.email ?? '', [Validators.email]],
    address: [this.data.supplier?.address ?? ''],
    active: [this.data.supplier?.active ?? true],
  });

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);
    const value = this.form.getRawValue();

    const request$ = this.isEdit()
      ? this.suppliersService.update(this.data.supplier!.id, {
          name: value.name,
          contactName: value.contactName,
          phone: value.phone || null,
          email: value.email || null,
          address: value.address || null,
          active: value.active,
        })
      : this.suppliersService.create({
          name: value.name,
          contactName: value.contactName,
          phone: value.phone || null,
          email: value.email || null,
          address: value.address || null,
        });

    request$.subscribe({
      next: (supplier) => {
        this.saving.set(false);
        this.dialogRef.close(supplier);
      },
      error: (error) => {
        this.saving.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudo guardar el proveedor.');
      },
    });
  }
}

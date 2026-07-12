import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { CustomersService } from '../customers.service';
import { Customer } from '../models/customer.model';

export interface CustomerFormData {
  customer?: Customer;
}

@Component({
  selector: 'app-customer-form',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatButtonModule,
  ],
  templateUrl: './customer-form.html',
  styleUrl: './customer-form.scss',
})
export class CustomerForm {
  private readonly fb = inject(FormBuilder);
  private readonly customersService = inject(CustomersService);
  private readonly dialogRef = inject(MatDialogRef<CustomerForm>);
  protected readonly data = inject<CustomerFormData>(MAT_DIALOG_DATA, { optional: true }) ?? {};

  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly isEdit = computed(() => !!this.data.customer);

  protected readonly form = this.fb.nonNullable.group({
    storeName: [this.data.customer?.storeName ?? '', [Validators.required, Validators.maxLength(150)]],
    ownerName: [this.data.customer?.ownerName ?? '', [Validators.required, Validators.maxLength(150)]],
    phone: [this.data.customer?.phone ?? ''],
    email: [this.data.customer?.email ?? '', [Validators.email]],
    address: [this.data.customer?.address ?? '', [Validators.required, Validators.maxLength(255)]],
    creditLimit: [this.data.customer?.creditLimit ?? 0, [Validators.required, Validators.min(0)]],
    classification: [this.data.customer?.classification ?? 'C', Validators.required],
    active: [this.data.customer?.active ?? true],
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
      ? this.customersService.update(this.data.customer!.id, {
          storeName: value.storeName,
          ownerName: value.ownerName,
          phone: value.phone || null,
          email: value.email || null,
          address: value.address,
          latitude: null,
          longitude: null,
          creditLimit: value.creditLimit,
          classification: value.classification,
          active: value.active,
        })
      : this.customersService.create({
          storeName: value.storeName,
          ownerName: value.ownerName,
          phone: value.phone || null,
          email: value.email || null,
          address: value.address,
          latitude: null,
          longitude: null,
          creditLimit: value.creditLimit,
          classification: value.classification,
        });

    request$.subscribe({
      next: (customer) => {
        this.saving.set(false);
        this.dialogRef.close(customer);
      },
      error: (error) => {
        this.saving.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudo guardar el cliente.');
      },
    });
  }
}

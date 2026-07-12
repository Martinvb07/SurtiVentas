import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { CatalogService } from '../../catalog/catalog.service';
import { Product } from '../../catalog/models/product.model';
import { CustomersService } from '../../customers/customers.service';
import { Customer } from '../../customers/models/customer.model';
import { OrdersService } from '../orders.service';

@Component({
  selector: 'app-order-form',
  imports: [
    CurrencyPipe,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './order-form.html',
  styleUrl: './order-form.scss',
})
export class OrderForm {
  private readonly fb = inject(FormBuilder);
  private readonly customersService = inject(CustomersService);
  private readonly catalogService = inject(CatalogService);
  private readonly ordersService = inject(OrdersService);
  private readonly dialogRef = inject(MatDialogRef<OrderForm>);

  protected readonly customers = signal<Customer[]>([]);
  protected readonly products = signal<Product[]>([]);
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    customerId: [null as number | null, Validators.required],
    lines: this.fb.array([this.buildLineGroup()]),
  });

  protected get lines(): FormArray {
    return this.form.controls.lines;
  }

  constructor() {
    this.customersService.search({ active: true, size: 200 }).subscribe((page) => this.customers.set(page.content));
    this.catalogService.searchProducts({ active: true, size: 200 }).subscribe((page) => this.products.set(page.content));
  }

  protected addLine(): void {
    this.lines.push(this.buildLineGroup());
  }

  protected removeLine(index: number): void {
    if (this.lines.length > 1) {
      this.lines.removeAt(index);
    }
  }

  protected unitPriceFor(lineIndex: number): number {
    const productId = this.lines.at(lineIndex).get('productId')?.value;
    return this.products().find((product) => product.id === productId)?.price ?? 0;
  }

  protected subtotalFor(lineIndex: number): number {
    const quantity = this.lines.at(lineIndex).get('quantity')?.value ?? 0;
    return this.unitPriceFor(lineIndex) * quantity;
  }

  protected get total(): number {
    return this.lines.controls.reduce((sum: number, _control, index) => sum + this.subtotalFor(index), 0);
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
      .create({
        customerId: value.customerId!,
        lines: value.lines.map((line) => ({
          productId: line.productId!,
          quantity: line.quantity,
        })),
      })
      .subscribe({
        next: (order) => {
          this.saving.set(false);
          this.dialogRef.close(order);
        },
        error: (error) => {
          this.saving.set(false);
          this.errorMessage.set(error.error?.message ?? 'No se pudo crear el pedido.');
        },
      });
  }

  private buildLineGroup() {
    return this.fb.nonNullable.group({
      productId: [null as number | null, Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
    });
  }
}

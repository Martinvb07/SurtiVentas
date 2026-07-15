import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ConnectivityService } from '../../../core/offline/connectivity.service';
import { OfflineStorageService } from '../../../core/offline/offline-storage.service';
import { OrderOutboxService } from '../../../core/offline/order-outbox.service';
import { PresaleCacheService } from '../../../core/offline/presale-cache.service';
import { Product } from '../../catalog/models/product.model';
import { Customer } from '../../customers/models/customer.model';

const DRAFT_KEY = 'presale.cart.draft';

interface CartDraft {
  customerId: number | null;
  lines: { productId: number | null; quantity: number }[];
}

/** What the dialog resolves with, so the list can react appropriately. */
export interface OrderFormResult {
  queued: boolean;
}

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
  private readonly presaleCache = inject(PresaleCacheService);
  private readonly outbox = inject(OrderOutboxService);
  private readonly connectivity = inject(ConnectivityService);
  private readonly storage = inject(OfflineStorageService);
  private readonly dialogRef = inject(MatDialogRef<OrderForm, OrderFormResult>);

  protected readonly customers = signal<Customer[]>([]);
  protected readonly products = signal<Product[]>([]);
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly offline = computed(() => !this.connectivity.online());

  protected readonly form = this.fb.nonNullable.group({
    customerId: [null as number | null, Validators.required],
    lines: this.fb.array([this.buildLineGroup()]),
  });

  protected get lines(): FormArray {
    return this.form.controls.lines;
  }

  constructor() {
    // Cached catalog/customers keep the cart usable with no connection.
    this.presaleCache.getCustomers().subscribe((customers) => this.customers.set(customers));
    this.presaleCache.getProducts().subscribe((products) => this.products.set(products));

    this.restoreDraft();
    // Persist the in-progress cart so it survives a reload or the app closing.
    this.form.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => this.saveDraft());
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

  protected async submit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);
    const value = this.form.getRawValue();
    const customerName = this.customers().find((c) => c.id === value.customerId)?.storeName ?? '';

    try {
      const result = await this.outbox.submit(
        {
          customerId: value.customerId!,
          lines: value.lines.map((line) => ({ productId: line.productId!, quantity: line.quantity })),
        },
        { customerName, total: this.total },
      );
      this.clearDraft();
      this.dialogRef.close({ queued: !result.synced });
    } catch (error: unknown) {
      this.saving.set(false);
      this.errorMessage.set(this.messageFrom(error));
    }
  }

  private buildLineGroup(productId: number | null = null, quantity = 1) {
    return this.fb.nonNullable.group({
      productId: [productId, Validators.required],
      quantity: [quantity, [Validators.required, Validators.min(1)]],
    });
  }

  private restoreDraft(): void {
    const draft = this.storage.get<CartDraft | null>(DRAFT_KEY, null);
    if (!draft || !draft.lines?.length) {
      return;
    }
    this.form.controls.customerId.setValue(draft.customerId);
    this.lines.clear();
    for (const line of draft.lines) {
      this.lines.push(this.buildLineGroup(line.productId, line.quantity));
    }
  }

  private saveDraft(): void {
    this.storage.set(DRAFT_KEY, this.form.getRawValue());
  }

  private clearDraft(): void {
    this.storage.remove(DRAFT_KEY);
  }

  private messageFrom(error: unknown): string {
    const err = error as { error?: { message?: string } };
    return err?.error?.message ?? 'No se pudo crear el pedido.';
  }
}

import { Component, computed, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { BillingService } from '../billing.service';
import { BillableOrder, Invoice, InvoiceLineReview } from '../models/invoice.model';

export interface GenerateInvoiceDialogData {
  order: BillableOrder;
}

/**
 * Split modal: on the left the products the customer ordered (with current
 * stock), on the right the biller digitalizes them by hand. The invoice can
 * only be generated once the typed lines match the order and there is stock.
 */
@Component({
  selector: 'app-generate-invoice-dialog',
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
  templateUrl: './generate-invoice-dialog.html',
  styleUrl: './generate-invoice-dialog.scss',
})
export class GenerateInvoiceDialog {
  private readonly fb = inject(FormBuilder);
  private readonly billingService = inject(BillingService);
  private readonly dialogRef = inject(MatDialogRef<GenerateInvoiceDialog>);
  protected readonly data = inject<GenerateInvoiceDialogData>(MAT_DIALOG_DATA);

  protected readonly orderedLines = signal<InvoiceLineReview[]>([]);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly minDate = new Date();

  protected readonly stockOk = computed(() => this.orderedLines().every((l) => l.sufficient));

  protected readonly form = this.fb.nonNullable.group({
    dueDate: [this.defaultDueDate(), Validators.required],
    lines: this.fb.array<FormGroup>([]),
  });

  constructor() {
    this.billingService.reviewOrder(this.data.order.orderId).subscribe({
      next: (lines) => {
        this.orderedLines.set(lines);
        lines.forEach(() => this.addLine());
        if (!lines.length) this.addLine();
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected get lines(): FormArray {
    return this.form.controls.lines;
  }

  protected addLine(): void {
    this.lines.push(this.buildLine());
  }

  protected removeLine(index: number): void {
    if (this.lines.length > 1) {
      this.lines.removeAt(index);
    }
  }

  /** True when the digitalized lines exactly match the order (products + quantities). */
  protected get matchesOrder(): boolean {
    const ordered = this.orderedLines();
    if (!ordered.length) return false;

    const want = new Map<number, number>();
    ordered.forEach((l) => want.set(l.productId, l.quantity));

    const got = new Map<number, number>();
    for (const control of this.lines.controls) {
      const productId = control.get('productId')?.value as number | null;
      const quantity = Number(control.get('quantity')?.value);
      if (productId == null || !quantity) return false;
      got.set(productId, (got.get(productId) ?? 0) + quantity);
    }

    if (got.size !== want.size) return false;
    for (const [productId, quantity] of want) {
      if (got.get(productId) !== quantity) return false;
    }
    return true;
  }

  protected get canGenerate(): boolean {
    return this.form.valid && this.matchesOrder && this.stockOk();
  }

  protected submit(): void {
    if (!this.canGenerate) {
      return;
    }
    this.saving.set(true);
    this.errorMessage.set(null);
    const dueDate = this.form.getRawValue().dueDate;

    this.billingService
      .generate({ orderId: this.data.order.orderId, dueDate: this.formatDate(dueDate) })
      .subscribe({
        next: (invoice: Invoice) => {
          this.saving.set(false);
          this.dialogRef.close(invoice);
        },
        error: (error) => {
          this.saving.set(false);
          this.errorMessage.set(error.error?.message ?? 'No se pudo generar la factura.');
        },
      });
  }

  private buildLine(): FormGroup {
    return this.fb.nonNullable.group({
      productId: [null as number | null, Validators.required],
      quantity: [null as number | null, [Validators.required, Validators.min(1)]],
    });
  }

  private defaultDueDate(): Date {
    const date = new Date();
    date.setDate(date.getDate() + 30);
    return date;
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}

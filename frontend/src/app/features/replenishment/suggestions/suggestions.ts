import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PurchaseOrdersService } from '../../purchase-orders/purchase-orders.service';
import { ReplenishmentService } from '../replenishment.service';
import { ReplenishmentItem, SupplierReplenishment } from '../models/replenishment.model';

@Component({
  selector: 'app-suggestions',
  imports: [CurrencyPipe, FormsModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './suggestions.html',
  styleUrl: './suggestions.scss',
})
export class SuggestionsPage {
  private readonly replenishmentService = inject(ReplenishmentService);
  private readonly purchaseOrdersService = inject(PurchaseOrdersService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly groups = signal<SupplierReplenishment[]>([]);
  protected readonly loading = signal(true);
  protected readonly generating = signal<number | null>(null);

  constructor() {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.replenishmentService.getSuggestions().subscribe({
      next: (groups) => {
        this.groups.set(groups);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected lineSubtotal(item: ReplenishmentItem): number {
    return (item.unitCost ?? 0) * (item.suggestedQty || 0);
  }

  protected groupTotal(group: SupplierReplenishment): number {
    return group.items.reduce((sum, item) => sum + this.lineSubtotal(item), 0);
  }

  /** Turns a supplier's suggestions into a draft purchase order. */
  protected generate(group: SupplierReplenishment): void {
    if (group.supplierId == null) {
      return;
    }
    this.generating.set(group.supplierId);
    this.purchaseOrdersService
      .create({
        supplierId: group.supplierId,
        expectedDate: null,
        lines: group.items.map((item) => ({
          productId: item.productId,
          quantity: item.suggestedQty,
          unitCost: item.unitCost ?? 0,
        })),
      })
      .subscribe({
        next: () => {
          this.generating.set(null);
          this.groups.update((groups) => groups.filter((g) => g.supplierId !== group.supplierId));
          this.snackBar.open(`Orden de compra creada para ${group.supplierName} (borrador)`, 'Cerrar', { duration: 4000 });
        },
        error: (err) => {
          this.generating.set(null);
          this.snackBar.open(err?.error?.message ?? 'No se pudo crear la orden de compra', 'Cerrar', { duration: 5000 });
        },
      });
  }
}

/** A suggested reorder line for a low-stock product. */
export interface ReplenishmentItem {
  productId: number;
  sku: string;
  name: string;
  stock: number;
  minStock: number;
  demand30: number;
  suggestedQty: number;
  unitCost: number | null;
  estimatedCost: number | null;
}

/** Replenishment suggestions grouped by supplier (supplierId null = no supplier). */
export interface SupplierReplenishment {
  supplierId: number | null;
  supplierName: string;
  items: ReplenishmentItem[];
  totalEstimatedCost: number;
}

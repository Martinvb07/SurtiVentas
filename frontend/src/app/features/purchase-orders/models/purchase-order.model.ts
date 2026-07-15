export type PurchaseOrderStatus = 'BORRADOR' | 'ENVIADA' | 'RECIBIDA' | 'INGRESADA' | 'CANCELADA';

export interface PurchaseOrderLine {
  id: number;
  productId: number;
  productSku: string;
  productName: string;
  quantity: number;
  unitCost: number;
  subtotal: number;
}

export interface PurchaseOrder {
  id: number;
  orderNumber: string;
  supplierId: number;
  supplierName: string;
  status: PurchaseOrderStatus;
  expectedDate: string | null;
  totalAmount: number;
  createdById: number;
  lines: PurchaseOrderLine[];
  createdAt: string;
  updatedAt: string;
}

export interface PurchaseOrderLineRequest {
  productId: number;
  quantity: number;
  unitCost: number;
}

export interface PurchaseOrderCreateRequest {
  supplierId: number;
  expectedDate: string | null;
  lines: PurchaseOrderLineRequest[];
}

export interface PurchaseOrderTransitionRequest {
  targetStatus: PurchaseOrderStatus;
  note: string | null;
}

export interface PurchaseOrderHistoryEntry {
  id: number;
  fromStatus: PurchaseOrderStatus | null;
  toStatus: PurchaseOrderStatus;
  changedById: number;
  changedByName: string;
  note: string | null;
  changedAt: string;
}

/** A candidate line item detected by OCR on the scanned supplier invoice. */
export interface SupplierInvoiceLine {
  description: string;
  quantity: number | null;
  amount: number | null;
}

/** The scanned supplier invoice + its reconciliation against the order total. */
export interface SupplierInvoice {
  id: number;
  purchaseOrderId: number;
  fileName: string;
  contentType: string;
  extractedText: string | null;
  detectedTotal: number | null;
  poTotal: number;
  difference: number | null;
  matched: boolean;
  lines: SupplierInvoiceLine[];
  uploadedByName: string | null;
  createdAt: string;
}

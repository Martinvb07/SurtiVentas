export type InvoiceStatus = 'PENDIENTE' | 'PARCIAL' | 'PAGADA';

export type PaymentMethod = 'EFECTIVO' | 'TRANSFERENCIA' | 'TARJETA' | 'OTRO';

export interface Payment {
  id: number;
  amount: number;
  method: PaymentMethod;
  reference: string | null;
  registeredByName: string;
  paidAt: string;
}

export interface Invoice {
  id: number;
  invoiceNumber: string;
  orderId: number;
  orderNumber: string;
  customerId: number;
  customerName: string;
  dueDate: string;
  totalAmount: number;
  paidAmount: number;
  balance: number;
  status: InvoiceStatus;
  overdue: boolean;
  issuedAt: string;
  payments: Payment[];
}

export interface BillableOrder {
  orderId: number;
  orderNumber: string;
  customerId: number;
  customerName: string;
  totalAmount: number;
  createdAt: string;
}

export interface InvoiceLineReview {
  productId: number;
  productName: string;
  sku: string;
  quantity: number;
  stock: number;
  sufficient: boolean;
}

export interface GenerateInvoiceRequest {
  orderId: number;
  dueDate: string;
}

export interface RegisterPaymentRequest {
  amount: number;
  method: PaymentMethod;
  reference: string | null;
}

/** The scanned proof of payment (comprobante) + its OCR reconciliation. */
export interface PaymentReceipt {
  id: number;
  paymentId: number;
  fileName: string;
  contentType: string;
  extractedText: string | null;
  detectedAmount: number | null;
  detectedReference: string | null;
  paymentAmount: number;
  difference: number | null;
  matched: boolean;
  uploadedByName: string | null;
  createdAt: string;
}

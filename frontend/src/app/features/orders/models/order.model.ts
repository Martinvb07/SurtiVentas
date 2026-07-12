export type OrderStatus =
  | 'CREADO'
  | 'PENDIENTE_APROBACION'
  | 'APROBADO'
  | 'EN_ALISTAMIENTO'
  | 'ALISTADO'
  | 'ASIGNADO_RUTA'
  | 'ENTREGADO'
  | 'NOVEDAD'
  | 'FACTURADO'
  | 'PAGADO'
  | 'CARTERA_PENDIENTE'
  | 'CANCELADO';

export interface OrderLine {
  id: number;
  productId: number;
  productSku: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface Order {
  id: number;
  orderNumber: string;
  customerId: number;
  customerName: string;
  status: OrderStatus;
  createdById: number;
  assignedDriverId: number | null;
  assignedDriverName: string | null;
  totalAmount: number;
  lines: OrderLine[];
  createdAt: string;
  updatedAt: string;
}

export interface OrderLineRequest {
  productId: number;
  quantity: number;
}

export interface OrderCreateRequest {
  customerId: number;
  lines: OrderLineRequest[];
}

export interface OrderTransitionRequest {
  targetStatus: OrderStatus;
  note: string | null;
  driverId?: number | null;
}

export interface OrderHistoryEntry {
  id: number;
  fromStatus: OrderStatus | null;
  toStatus: OrderStatus;
  changedById: number;
  changedByName: string;
  note: string | null;
  changedAt: string;
}

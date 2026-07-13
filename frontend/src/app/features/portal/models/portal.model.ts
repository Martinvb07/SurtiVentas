export interface PortalSummary {
  customerId: number;
  storeName: string;
  ownerName: string;
  currentDebt: number;
  creditLimit: number;
  availableCredit: number;
  overLimit: boolean;
  totalOrders: number;
  pendingInvoices: number;
  overdueInvoices: number;
  nextDueDate: string | null;
}

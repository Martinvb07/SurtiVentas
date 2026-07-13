export interface SeriesPoint {
  label: string;
  value: number;
}

export interface StatusCount {
  key: string;
  label: string;
  count: number;
}

export interface LowStockItem {
  id: number;
  sku: string;
  name: string;
  stock: number;
  minStock: number;
}

export interface TopProduct {
  name: string;
  quantity: number;
}

export interface Debtor {
  id: number;
  storeName: string;
  currentDebt: number;
  creditLimit: number;
  overLimit: boolean;
}

export interface RecentOrder {
  id: number;
  orderNumber: string;
  customerName: string;
  status: string;
  totalAmount: number;
  createdAt: string;
}

export interface AdminDashboard {
  salesToday: number;
  salesMonth: number;
  ordersInProgress: number;
  totalReceivables: number;
  lowStockCount: number;
  activeCustomers: number;
  salesTrend: SeriesPoint[];
  ordersByStatus: StatusCount[];
  topProducts: TopProduct[];
  salesBySeller: SeriesPoint[];
}

export interface SellerDashboard {
  mySalesMonth: number;
  myOrdersMonth: number;
  myOrdersInProgress: number;
  customersServedMonth: number;
  mySalesTrend: SeriesPoint[];
  myOrdersByStatus: StatusCount[];
  recentOrders: RecentOrder[];
}

export interface WarehouseDashboard {
  toPick: number;
  inProgress: number;
  readyForDispatch: number;
  lowStockCount: number;
  purchaseOrdersToReceive: number;
  pickingFunnel: StatusCount[];
  lowStockItems: LowStockItem[];
}

export interface DriverDashboard {
  assignedToMe: number;
  deliveredToday: number;
  incidentsOpen: number;
  deliveredMonth: number;
  deliveryBreakdown: StatusCount[];
  myDeliveries: RecentOrder[];
}

export interface BillingDashboard {
  ordersToBill: number;
  totalReceivables: number;
  customersOverLimit: number;
  paidThisMonth: number;
  receivablesByClassification: SeriesPoint[];
  topDebtors: Debtor[];
  ordersToBillQueue: RecentOrder[];
}

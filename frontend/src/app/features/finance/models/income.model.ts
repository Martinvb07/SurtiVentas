export interface MonthlyPoint {
  label: string;
  invoiced: number;
  collected: number;
  purchases: number;
}

export interface IncomeReport {
  invoicedMonth: number;
  collectedMonth: number;
  purchasesMonth: number;
  profitMonth: number;
  invoicedTotal: number;
  collectedTotal: number;
  purchasesTotal: number;
  profitTotal: number;
  trend: MonthlyPoint[];
}

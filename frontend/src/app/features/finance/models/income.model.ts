export interface MonthlyPoint {
  label: string;
  invoiced: number;
  collected: number;
  purchases: number;
  payroll: number;
}

export interface IncomeReport {
  invoicedMonth: number;
  collectedMonth: number;
  purchasesMonth: number;
  payrollMonth: number;
  profitMonth: number;
  invoicedTotal: number;
  collectedTotal: number;
  purchasesTotal: number;
  payrollTotal: number;
  profitTotal: number;
  trend: MonthlyPoint[];
}

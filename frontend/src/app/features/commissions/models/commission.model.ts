/** A salesperson's commission for a month (computed from their sales + goal). */
export interface Commission {
  sellerId: number;
  sellerName: string;
  month: string;
  targetAmount: number | null;
  commissionRate: number | null;
  bonusRate: number | null;
  achievedSales: number;
  attainmentPct: number | null;
  hasGoal: boolean;
  goalMet: boolean;
  appliedRate: number | null;
  commission: number;
}

export interface SalesGoal {
  id: number;
  sellerId: number;
  sellerName: string;
  month: string;
  targetAmount: number;
  commissionRate: number;
  bonusRate: number;
}

export interface SalesGoalRequest {
  sellerId: number;
  month: string;
  targetAmount: number;
  commissionRate: number;
  bonusRate: number;
}

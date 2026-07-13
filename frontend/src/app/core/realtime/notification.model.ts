export interface AppNotification {
  type: string;
  title: string;
  message: string;
  orderId: number | null;
  orderNumber: string | null;
  timestamp: string;
  read?: boolean;
}

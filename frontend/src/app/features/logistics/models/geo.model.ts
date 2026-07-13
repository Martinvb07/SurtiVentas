export interface StorePoint {
  id: number;
  storeName: string;
  ownerName: string;
  address: string;
  latitude: number;
  longitude: number;
  classification: string;
}

export interface DeliveryPoint {
  orderId: number;
  orderNumber: string;
  customerName: string;
  address: string;
  latitude: number;
  longitude: number;
  totalAmount: number;
}

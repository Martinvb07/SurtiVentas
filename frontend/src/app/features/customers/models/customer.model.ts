export type CustomerClassification = 'A' | 'B' | 'C';

export interface Customer {
  id: number;
  storeName: string;
  ownerName: string;
  phone: string | null;
  email: string | null;
  address: string;
  latitude: number | null;
  longitude: number | null;
  creditLimit: number;
  currentDebt: number;
  overCreditLimit: boolean;
  classification: CustomerClassification;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CustomerCreateRequest {
  storeName: string;
  ownerName: string;
  phone: string | null;
  email: string | null;
  address: string;
  latitude: number | null;
  longitude: number | null;
  creditLimit: number;
  classification: CustomerClassification;
}

export interface CustomerUpdateRequest {
  storeName: string;
  ownerName: string;
  phone: string | null;
  email: string | null;
  address: string;
  latitude: number | null;
  longitude: number | null;
  creditLimit: number;
  classification: CustomerClassification;
  active: boolean;
}

export interface DebtMovementRequest {
  amountDelta: number;
  reason: string;
}

export interface DebtMovement {
  id: number;
  amountDelta: number;
  reason: string;
  createdById: number;
  createdByName: string;
  createdAt: string;
}

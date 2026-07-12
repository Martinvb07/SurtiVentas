export interface Supplier {
  id: number;
  name: string;
  contactName: string;
  phone: string | null;
  email: string | null;
  address: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface SupplierCreateRequest {
  name: string;
  contactName: string;
  phone: string | null;
  email: string | null;
  address: string | null;
}

export interface SupplierUpdateRequest extends SupplierCreateRequest {
  active: boolean;
}

export interface SupplierProduct {
  id: number;
  productId: number;
  productSku: string;
  productName: string;
  supplierSku: string | null;
  cost: number;
}

export interface SupplierProductRequest {
  productId: number;
  supplierSku: string | null;
  cost: number;
}

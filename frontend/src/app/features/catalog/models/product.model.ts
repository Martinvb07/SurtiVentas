import { Category } from './category.model';
import { UnitOfMeasure } from './unit-of-measure.model';

export interface Product {
  id: number;
  sku: string;
  name: string;
  description: string | null;
  category: Category;
  unitOfMeasure: UnitOfMeasure;
  price: number;
  stock: number;
  minStock: number;
  lowStock: boolean;
  batchTracked: boolean;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ProductCreateRequest {
  sku: string;
  name: string;
  description: string | null;
  categoryId: number;
  unitOfMeasureId: number;
  price: number;
  initialStock: number;
  minStock: number;
  batchTracked: boolean;
}

export interface ProductUpdateRequest {
  name: string;
  description: string | null;
  categoryId: number;
  unitOfMeasureId: number;
  price: number;
  minStock: number;
  batchTracked: boolean;
  active: boolean;
}

export interface StockMovementRequest {
  quantityDelta: number;
  reason: string;
}

export interface StockMovement {
  id: number;
  quantityDelta: number;
  reason: string;
  createdById: number;
  createdByName: string;
  createdAt: string;
}

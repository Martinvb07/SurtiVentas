import { Routes } from '@angular/router';
import { Role } from '../../core/auth/models/role.enum';
import { roleGuard } from '../../core/auth/role.guard';

export const CATALOG_ROUTES: Routes = [
  {
    path: 'products',
    loadComponent: () => import('./product-list/product-list').then((m) => m.ProductList),
    canActivate: [roleGuard([Role.ADMINISTRADOR, Role.BODEGUERO, Role.VENDEDOR])],
  },
  {
    path: 'categories',
    loadComponent: () => import('./category-list/category-list').then((m) => m.CategoryList),
    canActivate: [roleGuard([Role.ADMINISTRADOR])],
  },
  {
    path: 'units',
    loadComponent: () => import('./unit-list/unit-list').then((m) => m.UnitList),
    canActivate: [roleGuard([Role.ADMINISTRADOR])],
  },
];

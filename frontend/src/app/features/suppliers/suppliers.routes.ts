import { Routes } from '@angular/router';
import { Role } from '../../core/auth/models/role.enum';
import { roleGuard } from '../../core/auth/role.guard';

export const SUPPLIERS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./supplier-list/supplier-list').then((m) => m.SupplierList),
    canActivate: [roleGuard([Role.ADMINISTRADOR, Role.FACTURADOR])],
  },
  {
    path: ':id/catalog',
    loadComponent: () => import('./supplier-catalog/supplier-catalog').then((m) => m.SupplierCatalog),
    canActivate: [roleGuard([Role.ADMINISTRADOR, Role.FACTURADOR])],
  },
];

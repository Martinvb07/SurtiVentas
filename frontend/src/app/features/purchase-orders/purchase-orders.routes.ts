import { Routes } from '@angular/router';
import { Role } from '../../core/auth/models/role.enum';
import { roleGuard } from '../../core/auth/role.guard';

export const PURCHASE_ORDERS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./purchase-order-list/purchase-order-list').then((m) => m.PurchaseOrderList),
    canActivate: [roleGuard([Role.ADMINISTRADOR, Role.BODEGUERO])],
  },
];

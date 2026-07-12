import { Routes } from '@angular/router';
import { Role } from '../../core/auth/models/role.enum';
import { roleGuard } from '../../core/auth/role.guard';

export const ORDERS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./order-list/order-list').then((m) => m.OrderList),
    canActivate: [roleGuard([Role.ADMINISTRADOR, Role.VENDEDOR])],
  },
];

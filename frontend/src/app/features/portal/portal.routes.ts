import { Routes } from '@angular/router';
import { Role } from '../../core/auth/models/role.enum';
import { roleGuard } from '../../core/auth/role.guard';

export const PORTAL_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'orders' },
  {
    path: 'orders',
    canActivate: [roleGuard([Role.COMPRADOR])],
    loadComponent: () => import('./my-orders/my-orders').then((m) => m.MyOrders),
  },
  {
    path: 'invoices',
    canActivate: [roleGuard([Role.COMPRADOR])],
    loadComponent: () => import('./my-invoices/my-invoices').then((m) => m.MyInvoices),
  },
];

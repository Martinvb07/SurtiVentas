import { Routes } from '@angular/router';
import { Role } from '../../core/auth/models/role.enum';
import { roleGuard } from '../../core/auth/role.guard';

export const BILLING_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'invoices' },
  {
    path: 'invoices',
    canActivate: [roleGuard([Role.ADMINISTRADOR, Role.FACTURADOR])],
    loadComponent: () => import('./invoice-list/invoice-list').then((m) => m.InvoiceList),
  },
  {
    path: 'billable',
    canActivate: [roleGuard([Role.ADMINISTRADOR, Role.FACTURADOR])],
    loadComponent: () => import('./billable-orders/billable-orders').then((m) => m.BillableOrders),
  },
];

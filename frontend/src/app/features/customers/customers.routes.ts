import { Routes } from '@angular/router';
import { Role } from '../../core/auth/models/role.enum';
import { roleGuard } from '../../core/auth/role.guard';

export const CUSTOMERS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./customer-list/customer-list').then((m) => m.CustomerList),
    canActivate: [roleGuard([Role.ADMINISTRADOR, Role.VENDEDOR, Role.FACTURADOR])],
  },
];

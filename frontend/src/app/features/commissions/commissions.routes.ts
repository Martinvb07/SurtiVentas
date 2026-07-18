import { Routes } from '@angular/router';
import { Role } from '../../core/auth/models/role.enum';
import { roleGuard } from '../../core/auth/role.guard';

export const COMMISSIONS_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'manage' },
  {
    path: 'manage',
    canActivate: [roleGuard([Role.ADMINISTRADOR])],
    loadComponent: () => import('./commissions-admin/commissions-admin').then((m) => m.CommissionsAdmin),
  },
  {
    path: 'me',
    canActivate: [roleGuard([Role.VENDEDOR, Role.ADMINISTRADOR])],
    loadComponent: () => import('./my-commission/my-commission').then((m) => m.MyCommission),
  },
];

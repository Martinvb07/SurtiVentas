import { Routes } from '@angular/router';
import { Role } from '../../core/auth/models/role.enum';
import { roleGuard } from '../../core/auth/role.guard';

export const FINANCE_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'income' },
  {
    path: 'income',
    canActivate: [roleGuard([Role.ADMINISTRADOR])],
    loadComponent: () => import('./income-report/income-report').then((m) => m.IncomeReportPage),
  },
];

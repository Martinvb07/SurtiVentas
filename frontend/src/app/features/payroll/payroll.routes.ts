import { Routes } from '@angular/router';
import { Role } from '../../core/auth/models/role.enum';
import { roleGuard } from '../../core/auth/role.guard';

export const PAYROLL_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'employees' },
  {
    path: 'employees',
    canActivate: [roleGuard([Role.ADMINISTRADOR])],
    loadComponent: () => import('./employee-list/employee-list').then((m) => m.EmployeeList),
  },
];

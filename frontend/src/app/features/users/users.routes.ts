import { Routes } from '@angular/router';
import { Role } from '../../core/auth/models/role.enum';
import { roleGuard } from '../../core/auth/role.guard';

export const USERS_ROUTES: Routes = [
  {
    path: '',
    pathMatch: 'full',
    canActivate: [roleGuard([Role.ADMINISTRADOR])],
    loadComponent: () => import('./user-list/user-list').then((m) => m.UserList),
  },
];

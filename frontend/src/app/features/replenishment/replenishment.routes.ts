import { Routes } from '@angular/router';
import { Role } from '../../core/auth/models/role.enum';
import { roleGuard } from '../../core/auth/role.guard';

export const REPLENISHMENT_ROUTES: Routes = [
  {
    path: '',
    pathMatch: 'full',
    canActivate: [roleGuard([Role.ADMINISTRADOR, Role.FACTURADOR])],
    loadComponent: () => import('./suggestions/suggestions').then((m) => m.SuggestionsPage),
  },
];

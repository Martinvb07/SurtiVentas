import { Routes } from '@angular/router';
import { Role } from '../../core/auth/models/role.enum';
import { roleGuard } from '../../core/auth/role.guard';

export const LOGISTICS_ROUTES: Routes = [
  {
    path: 'warehouse',
    loadComponent: () => import('./warehouse-kanban/warehouse-kanban').then((m) => m.WarehouseKanban),
    canActivate: [roleGuard([Role.ADMINISTRADOR, Role.BODEGUERO])],
  },
  {
    path: 'deliveries',
    loadComponent: () => import('./driver-deliveries/driver-deliveries').then((m) => m.DriverDeliveries),
    canActivate: [roleGuard([Role.ADMINISTRADOR, Role.CONDUCTOR])],
  },
];

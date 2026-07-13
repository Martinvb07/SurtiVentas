import { Routes } from '@angular/router';
import { Role } from '../../core/auth/models/role.enum';
import { roleGuard } from '../../core/auth/role.guard';
import { panelRedirectGuard } from './panel-redirect.guard';

/**
 * Role dashboards. `/app` redirects to the panel that matches the user's role;
 * each panel is a distinct route guarded so only its role (and the
 * administrator, who oversees everything) may open it.
 */
export const DASHBOARD_ROUTES: Routes = [
  {
    path: '',
    pathMatch: 'full',
    canActivate: [panelRedirectGuard],
    children: [],
  },
  {
    path: 'panel/gerente',
    canActivate: [roleGuard([Role.ADMINISTRADOR])],
    loadComponent: () => import('./panels/gerente-panel/gerente-panel').then((m) => m.GerentePanel),
  },
  {
    path: 'panel/vendedor',
    canActivate: [roleGuard([Role.ADMINISTRADOR, Role.VENDEDOR])],
    loadComponent: () => import('./panels/vendedor-panel/vendedor-panel').then((m) => m.VendedorPanel),
  },
  {
    path: 'panel/bodega',
    canActivate: [roleGuard([Role.ADMINISTRADOR, Role.BODEGUERO])],
    loadComponent: () => import('./panels/bodega-panel/bodega-panel').then((m) => m.BodegaPanel),
  },
  {
    path: 'panel/conductor',
    canActivate: [roleGuard([Role.ADMINISTRADOR, Role.CONDUCTOR])],
    loadComponent: () => import('./panels/conductor-panel/conductor-panel').then((m) => m.ConductorPanel),
  },
  {
    path: 'panel/facturacion',
    canActivate: [roleGuard([Role.ADMINISTRADOR, Role.FACTURADOR])],
    loadComponent: () =>
      import('./panels/facturacion-panel/facturacion-panel').then((m) => m.FacturacionPanel),
  },
  {
    path: 'panel/tienda',
    canActivate: [roleGuard([Role.ADMINISTRADOR, Role.COMPRADOR])],
    loadComponent: () => import('./panels/tienda-panel/tienda-panel').then((m) => m.TiendaPanel),
  },
];

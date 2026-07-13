import { Role } from '../../core/auth/models/role.enum';

/** Maps each role to its dedicated dashboard route. */
export const PANEL_PATHS: Record<Role, string> = {
  [Role.ADMINISTRADOR]: '/app/panel/gerente',
  [Role.VENDEDOR]: '/app/panel/vendedor',
  [Role.BODEGUERO]: '/app/panel/bodega',
  [Role.CONDUCTOR]: '/app/panel/conductor',
  [Role.FACTURADOR]: '/app/panel/facturacion',
  [Role.COMPRADOR]: '/app/panel/tienda',
};

export function panelPathForRole(role: Role): string {
  return PANEL_PATHS[role] ?? '/app/panel/gerente';
}

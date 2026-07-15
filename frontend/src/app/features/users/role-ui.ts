import { Role } from '../../core/auth/models/role.enum';

export const ROLES: Role[] = Object.values(Role);

const LABELS: Record<Role, string> = {
  [Role.ADMINISTRADOR]: 'Administrador',
  [Role.VENDEDOR]: 'Vendedor',
  [Role.BODEGUERO]: 'Bodeguero',
  [Role.CONDUCTOR]: 'Conductor',
  [Role.FACTURADOR]: 'Facturador',
  [Role.COMPRADOR]: 'Comprador (tienda)',
};

export function roleLabel(role: Role): string {
  return LABELS[role] ?? role;
}

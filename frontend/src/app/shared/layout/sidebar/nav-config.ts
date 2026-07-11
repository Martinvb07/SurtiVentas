import { Role } from '../../../core/auth/models/role.enum';

export interface NavLeaf {
  label: string;
  icon: string;
  route: string;
  roles: Role[];
}

export interface NavGroup {
  label: string;
  icon: string;
  roles: Role[];
  items: NavLeaf[];
}

export type NavEntry = NavLeaf | NavGroup;

export interface NavSection {
  label: string;
  entries: NavEntry[];
}

export function isNavGroup(entry: NavEntry): entry is NavGroup {
  return (entry as NavGroup).items !== undefined;
}

const ALL_ROLES = Object.values(Role);

export const NAV_SECTIONS: NavSection[] = [
  {
    label: 'Gestión',
    entries: [
      { label: 'Panel general', icon: 'space_dashboard', route: '/app', roles: ALL_ROLES },
    ],
  },
  {
    label: 'Catálogo',
    entries: [
      {
        label: 'Inventario',
        icon: 'inventory_2',
        roles: [Role.ADMINISTRADOR, Role.BODEGUERO, Role.VENDEDOR],
        items: [
          {
            label: 'Productos',
            icon: 'category',
            route: '/app/catalog/products',
            roles: [Role.ADMINISTRADOR, Role.BODEGUERO, Role.VENDEDOR],
          },
          {
            label: 'Categorías',
            icon: 'sell',
            route: '/app/catalog/categories',
            roles: [Role.ADMINISTRADOR],
          },
          {
            label: 'Unidades de medida',
            icon: 'straighten',
            route: '/app/catalog/units',
            roles: [Role.ADMINISTRADOR],
          },
        ],
      },
    ],
  },
];

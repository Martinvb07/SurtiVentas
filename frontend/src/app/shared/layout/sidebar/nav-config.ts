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
      { label: 'Mi panel', icon: 'space_dashboard', route: '/app', roles: ALL_ROLES },
    ],
  },
  {
    label: 'Comercial',
    entries: [
      {
        label: 'Pedidos',
        icon: 'shopping_cart',
        route: '/app/orders',
        roles: [Role.ADMINISTRADOR, Role.VENDEDOR],
      },
      {
        label: 'Clientes',
        icon: 'storefront',
        route: '/app/customers',
        roles: [Role.ADMINISTRADOR, Role.VENDEDOR, Role.FACTURADOR],
      },
      {
        label: 'Mi ruta',
        icon: 'map',
        route: '/app/logistics/route',
        roles: [Role.VENDEDOR],
      },
      {
        label: 'Mapa de tiendas',
        icon: 'map',
        route: '/app/logistics/route',
        roles: [Role.ADMINISTRADOR],
      },
    ],
  },
  {
    label: 'Logística',
    entries: [
      {
        label: 'Alistamiento',
        icon: 'inventory',
        route: '/app/logistics/warehouse',
        roles: [Role.ADMINISTRADOR, Role.BODEGUERO],
      },
      {
        label: 'Mis entregas',
        icon: 'directions_car',
        route: '/app/logistics/deliveries',
        roles: [Role.CONDUCTOR],
      },
      {
        label: 'Mapa de entregas',
        icon: 'map',
        route: '/app/logistics/map',
        roles: [Role.CONDUCTOR],
      },
    ],
  },
  {
    label: 'Facturación',
    entries: [
      {
        label: 'Por facturar',
        icon: 'point_of_sale',
        route: '/app/billing/billable',
        roles: [Role.FACTURADOR],
      },
      {
        label: 'Cartera y facturas',
        icon: 'account_balance_wallet',
        route: '/app/billing/invoices',
        roles: [Role.ADMINISTRADOR, Role.FACTURADOR],
      },
    ],
  },
  {
    label: 'Finanzas',
    entries: [
      {
        label: 'Ingresos',
        icon: 'trending_up',
        route: '/app/finance/income',
        roles: [Role.ADMINISTRADOR],
      },
      {
        label: 'Nómina',
        icon: 'badge',
        route: '/app/payroll/employees',
        roles: [Role.ADMINISTRADOR],
      },
    ],
  },
  {
    label: 'Administración',
    entries: [
      {
        label: 'Usuarios',
        icon: 'manage_accounts',
        route: '/app/users',
        roles: [Role.ADMINISTRADOR],
      },
    ],
  },
  {
    label: 'Mi tienda',
    entries: [
      {
        label: 'Mis pedidos',
        icon: 'shopping_cart',
        route: '/app/portal/orders',
        roles: [Role.COMPRADOR],
      },
      {
        label: 'Mis facturas',
        icon: 'receipt_long',
        route: '/app/portal/invoices',
        roles: [Role.COMPRADOR],
      },
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
      {
        label: 'Compras',
        icon: 'local_shipping',
        roles: [Role.ADMINISTRADOR, Role.FACTURADOR, Role.BODEGUERO],
        items: [
          {
            label: 'Proveedores',
            icon: 'storefront',
            route: '/app/suppliers',
            roles: [Role.ADMINISTRADOR, Role.FACTURADOR],
          },
          {
            label: 'Órdenes de compra',
            icon: 'receipt_long',
            route: '/app/purchase-orders',
            roles: [Role.ADMINISTRADOR, Role.FACTURADOR, Role.BODEGUERO],
          },
        ],
      },
    ],
  },
];

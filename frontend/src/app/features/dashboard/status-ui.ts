/** Human labels and colour tones for order statuses shown in dashboard lists. */

export type Tone = 'success' | 'warning' | 'danger' | 'neutral';

const LABELS: Record<string, string> = {
  CREADO: 'Creado',
  PENDIENTE_APROBACION: 'Pend. aprobación',
  APROBADO: 'Aprobado',
  EN_ALISTAMIENTO: 'En alistamiento',
  ALISTADO: 'Alistado',
  ASIGNADO_RUTA: 'En ruta',
  ENTREGADO: 'Entregado',
  NOVEDAD: 'Novedad',
  FACTURADO: 'Facturado',
  PAGADO: 'Pagado',
  CARTERA_PENDIENTE: 'Cartera pend.',
  CANCELADO: 'Cancelado',
};

const TONES: Record<string, Tone> = {
  CREADO: 'neutral',
  PENDIENTE_APROBACION: 'warning',
  APROBADO: 'success',
  EN_ALISTAMIENTO: 'warning',
  ALISTADO: 'success',
  ASIGNADO_RUTA: 'warning',
  ENTREGADO: 'success',
  NOVEDAD: 'danger',
  FACTURADO: 'success',
  PAGADO: 'success',
  CARTERA_PENDIENTE: 'warning',
  CANCELADO: 'danger',
};

export function orderStatusLabel(status: string): string {
  return LABELS[status] ?? status;
}

export function orderStatusTone(status: string): Tone {
  return TONES[status] ?? 'neutral';
}

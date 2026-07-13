import { Component, Input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

export type KpiTone = 'primary' | 'success' | 'warning' | 'danger' | 'neutral';

/** A single headline metric tile used across every role dashboard. */
@Component({
  selector: 'app-kpi-card',
  imports: [MatIconModule],
  templateUrl: './kpi-card.html',
  styleUrl: './kpi-card.scss',
})
export class KpiCard {
  @Input() label = '';
  @Input() value: string | number | null = '';
  @Input() icon = 'insights';
  @Input() tone: KpiTone = 'neutral';
  @Input() hint?: string;
}

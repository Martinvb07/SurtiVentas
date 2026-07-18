import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { CommissionsService } from '../commissions.service';
import { Commission } from '../models/commission.model';

@Component({
  selector: 'app-my-commission',
  imports: [CurrencyPipe, DecimalPipe, MatIconModule, MatProgressBarModule],
  templateUrl: './my-commission.html',
  styleUrl: './my-commission.scss',
})
export class MyCommission {
  private readonly commissionsService = inject(CommissionsService);

  protected readonly month = signal<string>(new Date().toISOString().slice(0, 7));
  protected readonly commission = signal<Commission | null>(null);
  protected readonly loading = signal(false);

  /** Progress bar value, capped at 100%. */
  protected readonly progress = computed(() => Math.min(this.commission()?.attainmentPct ?? 0, 100));

  constructor() {
    effect(() => {
      const month = this.month();
      this.loading.set(true);
      this.commissionsService.getMyCommission(month).subscribe({
        next: (c) => {
          this.commission.set(c);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
    });
  }

  protected onMonthChange(value: string): void {
    if (value) {
      this.month.set(value);
    }
  }
}

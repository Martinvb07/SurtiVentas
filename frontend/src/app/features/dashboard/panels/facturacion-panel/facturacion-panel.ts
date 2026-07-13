import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../../core/auth/auth.service';
import { DashboardService } from '../../dashboard.service';
import { BillingDashboard } from '../../models/dashboard.model';
import { valueBar } from '../../ui/charts';
import { ChartComponent } from '../../ui/chart/chart';
import { KpiCard } from '../../ui/kpi-card/kpi-card';
import { QuickAction } from '../../ui/quick-action/quick-action';

@Component({
  selector: 'app-facturacion-panel',
  imports: [CurrencyPipe, MatIconModule, ChartComponent, KpiCard, QuickAction],
  templateUrl: './facturacion-panel.html',
})
export class FacturacionPanel {
  private readonly service = inject(DashboardService);
  protected readonly auth = inject(AuthService);

  protected readonly data = signal<BillingDashboard | null>(null);
  protected readonly loading = signal(true);

  protected readonly classificationConfig = computed(() => {
    const d = this.data();
    if (!d) return null;
    const hasData = d.receivablesByClassification.some((s) => s.value > 0);
    return hasData
      ? valueBar(
          d.receivablesByClassification.map((s) => s.label),
          d.receivablesByClassification.map((s) => s.value),
          'Cartera',
        )
      : null;
  });

  constructor() {
    this.service.billing().subscribe({
      next: (d) => {
        this.data.set(d);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}

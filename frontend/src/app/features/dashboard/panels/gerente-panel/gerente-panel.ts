import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../../core/auth/auth.service';
import { DashboardService } from '../../dashboard.service';
import { AdminDashboard } from '../../models/dashboard.model';
import { statusDoughnut, trendLine, valueBar } from '../../ui/charts';
import { ChartComponent } from '../../ui/chart/chart';
import { KpiCard } from '../../ui/kpi-card/kpi-card';
import { QuickAction } from '../../ui/quick-action/quick-action';

@Component({
  selector: 'app-gerente-panel',
  imports: [CurrencyPipe, MatIconModule, ChartComponent, KpiCard, QuickAction],
  templateUrl: './gerente-panel.html',
})
export class GerentePanel {
  private readonly service = inject(DashboardService);
  protected readonly auth = inject(AuthService);

  protected readonly data = signal<AdminDashboard | null>(null);
  protected readonly loading = signal(true);

  protected readonly trendConfig = computed(() => {
    const d = this.data();
    return d ? trendLine(d.salesTrend, 'Ventas') : null;
  });

  protected readonly statusConfig = computed(() => {
    const d = this.data();
    return d && d.ordersByStatus.length ? statusDoughnut(d.ordersByStatus) : null;
  });

  protected readonly sellerConfig = computed(() => {
    const d = this.data();
    return d && d.salesBySeller.length
      ? valueBar(
          d.salesBySeller.map((s) => s.label),
          d.salesBySeller.map((s) => s.value),
          'Ventas',
        )
      : null;
  });

  constructor() {
    this.service.admin().subscribe({
      next: (d) => {
        this.data.set(d);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}

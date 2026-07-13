import { Component, computed, inject, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../../core/auth/auth.service';
import { DashboardService } from '../../dashboard.service';
import { WarehouseDashboard } from '../../models/dashboard.model';
import { valueBar } from '../../ui/charts';
import { ChartComponent } from '../../ui/chart/chart';
import { KpiCard } from '../../ui/kpi-card/kpi-card';
import { QuickAction } from '../../ui/quick-action/quick-action';

@Component({
  selector: 'app-bodega-panel',
  imports: [MatIconModule, ChartComponent, KpiCard, QuickAction],
  templateUrl: './bodega-panel.html',
})
export class BodegaPanel {
  private readonly service = inject(DashboardService);
  protected readonly auth = inject(AuthService);

  protected readonly data = signal<WarehouseDashboard | null>(null);
  protected readonly loading = signal(true);

  protected readonly funnelConfig = computed(() => {
    const d = this.data();
    if (!d) return null;
    const hasData = d.pickingFunnel.some((s) => s.count > 0);
    return hasData
      ? valueBar(
          d.pickingFunnel.map((s) => s.label),
          d.pickingFunnel.map((s) => s.count),
          'Pedidos',
          false,
        )
      : null;
  });

  constructor() {
    this.service.warehouse().subscribe({
      next: (d) => {
        this.data.set(d);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}

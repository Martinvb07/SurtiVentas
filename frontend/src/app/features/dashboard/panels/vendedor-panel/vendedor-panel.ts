import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../../core/auth/auth.service';
import { DashboardService } from '../../dashboard.service';
import { SellerDashboard } from '../../models/dashboard.model';
import { orderStatusLabel, orderStatusTone } from '../../status-ui';
import { statusDoughnut, trendLine } from '../../ui/charts';
import { ChartComponent } from '../../ui/chart/chart';
import { KpiCard } from '../../ui/kpi-card/kpi-card';
import { QuickAction } from '../../ui/quick-action/quick-action';

@Component({
  selector: 'app-vendedor-panel',
  imports: [CurrencyPipe, MatIconModule, ChartComponent, KpiCard, QuickAction],
  templateUrl: './vendedor-panel.html',
})
export class VendedorPanel {
  private readonly service = inject(DashboardService);
  protected readonly auth = inject(AuthService);
  protected readonly statusLabel = orderStatusLabel;
  protected readonly statusTone = orderStatusTone;

  protected readonly data = signal<SellerDashboard | null>(null);
  protected readonly loading = signal(true);

  protected readonly trendConfig = computed(() => {
    const d = this.data();
    return d ? trendLine(d.mySalesTrend, 'Mis ventas') : null;
  });

  protected readonly statusConfig = computed(() => {
    const d = this.data();
    return d && d.myOrdersByStatus.length ? statusDoughnut(d.myOrdersByStatus) : null;
  });

  constructor() {
    this.service.seller().subscribe({
      next: (d) => {
        this.data.set(d);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}

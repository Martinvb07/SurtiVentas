import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../../core/auth/auth.service';
import { DashboardService } from '../../dashboard.service';
import { DriverDashboard } from '../../models/dashboard.model';
import { KpiCard } from '../../ui/kpi-card/kpi-card';
import { QuickAction } from '../../ui/quick-action/quick-action';

@Component({
  selector: 'app-conductor-panel',
  imports: [CurrencyPipe, MatIconModule, KpiCard, QuickAction],
  templateUrl: './conductor-panel.html',
})
export class ConductorPanel {
  private readonly service = inject(DashboardService);
  protected readonly auth = inject(AuthService);

  protected readonly data = signal<DriverDashboard | null>(null);
  protected readonly loading = signal(true);

  constructor() {
    this.service.driver().subscribe({
      next: (d) => {
        this.data.set(d);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}

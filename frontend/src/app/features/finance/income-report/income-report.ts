import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { ChartConfiguration } from 'chart.js';
import { ChartComponent } from '../../dashboard/ui/chart/chart';
import { KpiCard } from '../../dashboard/ui/kpi-card/kpi-card';
import { FinanceService } from '../finance.service';
import { IncomeReport } from '../models/income.model';

function compactCurrency(value: number): string {
  const abs = Math.abs(value);
  if (abs >= 1_000_000) return `$${(value / 1_000_000).toFixed(1)}M`;
  if (abs >= 1_000) return `$${Math.round(value / 1_000)}k`;
  return `$${value}`;
}

@Component({
  selector: 'app-income-report',
  imports: [CurrencyPipe, MatIconModule, ChartComponent, KpiCard],
  templateUrl: './income-report.html',
  styleUrl: './income-report.scss',
})
export class IncomeReportPage {
  private readonly finance = inject(FinanceService);

  protected readonly data = signal<IncomeReport | null>(null);
  protected readonly loading = signal(true);

  protected readonly chartConfig = computed<ChartConfiguration<'bar'> | null>(() => {
    const d = this.data();
    if (!d) return null;
    return {
      type: 'bar',
      data: {
        labels: d.trend.map((t) => t.label),
        datasets: [
          { label: 'Facturado', data: d.trend.map((t) => t.invoiced), backgroundColor: '#1b4a7a', borderRadius: 4 },
          { label: 'Cobrado', data: d.trend.map((t) => t.collected), backgroundColor: '#2f9e68', borderRadius: 4 },
          { label: 'Compras', data: d.trend.map((t) => t.purchases), backgroundColor: '#e89b2e', borderRadius: 4 },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'top', labels: { color: '#5b6b7b', boxWidth: 12, font: { size: 11 } } },
          tooltip: {
            callbacks: { label: (c) => `${c.dataset.label}: ${compactCurrency(Number(c.parsed.y))}` },
          },
        },
        scales: {
          x: { grid: { display: false }, ticks: { color: '#5b6b7b', font: { size: 10 } } },
          y: {
            grid: { color: 'rgba(120, 140, 160, 0.14)' },
            beginAtZero: true,
            ticks: { color: '#5b6b7b', callback: (v) => compactCurrency(Number(v)), font: { size: 10 } },
          },
        },
      },
    };
  });

  constructor() {
    this.finance.income().subscribe({
      next: (report) => {
        this.data.set(report);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}

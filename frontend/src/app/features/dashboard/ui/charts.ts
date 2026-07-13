import { ChartConfiguration } from 'chart.js';
import { SeriesPoint, StatusCount } from '../models/dashboard.model';

/** SurtiVentas categorical palette (kept in sync with styles/_tokens.scss). */
export const CHART_COLORS = [
  '#1b4a7a',
  '#e89b2e',
  '#2f9e68',
  '#c4483a',
  '#5b8fb9',
  '#7a5ea8',
  '#8794a3',
  '#d9a441',
];

const GRID = 'rgba(120, 140, 160, 0.14)';
const TICK = '#5b6b7b';

function compactCurrency(value: number): string {
  const abs = Math.abs(value);
  if (abs >= 1_000_000) return `$${(value / 1_000_000).toFixed(1)}M`;
  if (abs >= 1_000) return `$${Math.round(value / 1_000)}k`;
  return `$${value}`;
}

/** Filled line for a daily trend (e.g. sales over the last two weeks). */
export function trendLine(points: SeriesPoint[], label: string): ChartConfiguration<'line'> {
  return {
    type: 'line',
    data: {
      labels: points.map((p) => p.label),
      datasets: [
        {
          label,
          data: points.map((p) => p.value),
          borderColor: '#1b4a7a',
          backgroundColor: 'rgba(27, 74, 122, 0.12)',
          fill: true,
          tension: 0.35,
          borderWidth: 2,
          pointRadius: 0,
          pointHoverRadius: 4,
          pointBackgroundColor: '#1b4a7a',
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: { callbacks: { label: (c) => compactCurrency(Number(c.parsed.y)) } },
      },
      scales: {
        x: {
          grid: { display: false },
          ticks: { color: TICK, maxRotation: 0, autoSkip: true, font: { size: 10 } },
        },
        y: {
          grid: { color: GRID },
          beginAtZero: true,
          ticks: { color: TICK, callback: (v) => compactCurrency(Number(v)), font: { size: 10 } },
        },
      },
    },
  };
}

/** Doughnut of counts grouped by status/category. */
export function statusDoughnut(items: StatusCount[]): ChartConfiguration<'doughnut'> {
  return {
    type: 'doughnut',
    data: {
      labels: items.map((i) => i.label),
      datasets: [{ data: items.map((i) => i.count), backgroundColor: CHART_COLORS, borderWidth: 0 }],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '62%',
      plugins: {
        legend: {
          position: 'right',
          labels: { color: TICK, boxWidth: 10, boxHeight: 10, padding: 10, font: { size: 11 } },
        },
      },
    },
  };
}

/** Horizontal bars for ranked values (sellers, classifications, …). */
export function valueBar(
  labels: string[],
  values: number[],
  label: string,
  money = true,
): ChartConfiguration<'bar'> {
  return {
    type: 'bar',
    data: {
      labels,
      datasets: [
        { label, data: values, backgroundColor: '#1b4a7a', borderRadius: 6, maxBarThickness: 30 },
      ],
    },
    options: {
      indexAxis: 'y',
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: { label: (c) => (money ? compactCurrency(Number(c.parsed.x)) : String(c.parsed.x)) },
        },
      },
      scales: {
        x: {
          grid: { color: GRID },
          beginAtZero: true,
          ticks: {
            color: TICK,
            callback: (v) => (money ? compactCurrency(Number(v)) : String(v)),
            font: { size: 10 },
          },
        },
        y: { grid: { display: false }, ticks: { color: TICK, font: { size: 11 } } },
      },
    },
  };
}

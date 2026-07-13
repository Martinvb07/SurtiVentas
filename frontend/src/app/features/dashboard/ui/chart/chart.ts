import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnDestroy,
  ViewChild,
} from '@angular/core';
import { Chart, ChartConfiguration, registerables } from 'chart.js';

Chart.register(...registerables);

/**
 * Thin reusable wrapper around a Chart.js canvas. Panels pass a full
 * {@link ChartConfiguration}; the chart is (re)built whenever it changes and
 * disposed on destroy to avoid leaking canvases across route changes.
 */
@Component({
  selector: 'app-chart',
  template: '<canvas #canvas></canvas>',
  styles: [
    ':host { display: block; position: relative; width: 100%; height: 100%; }',
    'canvas { max-width: 100%; }',
  ],
})
export class ChartComponent implements AfterViewInit, OnChanges, OnDestroy {
  @ViewChild('canvas', { static: true }) private canvasRef!: ElementRef<HTMLCanvasElement>;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  @Input({ required: true }) config!: ChartConfiguration<any>;

  private chart?: Chart;
  private ready = false;

  ngAfterViewInit(): void {
    this.ready = true;
    this.render();
  }

  ngOnChanges(): void {
    if (this.ready) {
      this.render();
    }
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
  }

  private render(): void {
    if (!this.config) {
      return;
    }
    this.chart?.destroy();
    this.chart = new Chart(this.canvasRef.nativeElement, this.config);
  }
}

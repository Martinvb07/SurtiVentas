import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { GeoService } from '../geo.service';
import { DeliveryPoint } from '../models/geo.model';
import { MapPoint, RouteMap } from '../route-map/route-map';

@Component({
  selector: 'app-driver-map',
  imports: [CurrencyPipe, MatIconModule, RouteMap],
  templateUrl: './driver-map.html',
  styleUrl: './driver-map.scss',
})
export class DriverMap {
  private readonly geo = inject(GeoService);

  protected readonly deliveries = signal<DeliveryPoint[]>([]);
  protected readonly loading = signal(true);

  protected readonly points = computed<MapPoint[]>(() =>
    this.deliveries().map((d) => ({
      lat: d.latitude,
      lng: d.longitude,
      title: d.customerName,
      subtitle: `${d.orderNumber} · ${d.address}`,
      tone: 'warning',
    })),
  );

  constructor() {
    this.geo.deliveries().subscribe({
      next: (deliveries) => {
        this.deliveries.set(deliveries);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}

import { Component, computed, inject, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../core/auth/auth.service';
import { Role } from '../../../core/auth/models/role.enum';
import { GeoService } from '../geo.service';
import { StorePoint } from '../models/geo.model';
import { MapPoint, RouteMap } from '../route-map/route-map';

@Component({
  selector: 'app-seller-route',
  imports: [MatIconModule, RouteMap],
  templateUrl: './seller-route.html',
  styleUrl: './seller-route.scss',
})
export class SellerRoute {
  private readonly geo = inject(GeoService);
  private readonly auth = inject(AuthService);

  protected readonly isAdmin = computed(() => this.auth.currentUser()?.role === Role.ADMINISTRADOR);

  protected readonly stores = signal<StorePoint[]>([]);
  protected readonly loading = signal(true);

  protected readonly points = computed<MapPoint[]>(() =>
    this.stores().map((s) => ({
      lat: s.latitude,
      lng: s.longitude,
      title: s.storeName,
      subtitle: s.address,
      tone: 'primary',
    })),
  );

  constructor() {
    this.geo.stores().subscribe({
      next: (stores) => {
        this.stores.set(stores);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}

import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnDestroy,
  ViewChild,
} from '@angular/core';
import * as L from 'leaflet';

export interface MapPoint {
  lat: number;
  lng: number;
  title: string;
  subtitle?: string;
  tone?: 'primary' | 'success' | 'warning';
}

const TONE_COLORS: Record<NonNullable<MapPoint['tone']>, string> = {
  primary: '#1b4a7a',
  success: '#2f9e68',
  warning: '#e89b2e',
};

// Villavicencio center — fallback view when there are no points to fit.
const DEFAULT_CENTER: L.LatLngExpression = [4.142, -73.626];

/**
 * Reusable Leaflet map. Renders one teardrop pin per {@link MapPoint} (styled
 * inline to avoid Leaflet's bundler-broken default marker images) and fits the
 * view to all points. Tiles come from OpenStreetMap.
 */
@Component({
  selector: 'app-route-map',
  template: '<div #map class="route-map-canvas"></div>',
  styles: [
    ':host { display: block; height: 100%; }',
    '.route-map-canvas { height: 100%; width: 100%; border-radius: var(--sv-radius-lg); }',
  ],
})
export class RouteMap implements AfterViewInit, OnChanges, OnDestroy {
  @ViewChild('map', { static: true }) private mapEl!: ElementRef<HTMLDivElement>;
  @Input() points: MapPoint[] = [];

  private map?: L.Map;
  private markers?: L.LayerGroup;
  private ready = false;

  ngAfterViewInit(): void {
    this.map = L.map(this.mapEl.nativeElement).setView(DEFAULT_CENTER, 13);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap',
    }).addTo(this.map);
    this.markers = L.layerGroup().addTo(this.map);
    this.ready = true;
    this.render();
  }

  ngOnChanges(): void {
    if (this.ready) {
      this.render();
    }
  }

  ngOnDestroy(): void {
    this.map?.remove();
  }

  private render(): void {
    if (!this.map || !this.markers) {
      return;
    }
    this.markers.clearLayers();
    if (!this.points.length) {
      this.map.setView(DEFAULT_CENTER, 13);
      return;
    }

    const coords: L.LatLngTuple[] = [];
    for (const point of this.points) {
      const color = TONE_COLORS[point.tone ?? 'primary'];
      const icon = L.divIcon({
        className: '',
        iconSize: [20, 20],
        iconAnchor: [10, 20],
        popupAnchor: [0, -18],
        html:
          `<span style="display:block;width:18px;height:18px;border-radius:50% 50% 50% 0;` +
          `background:${color};transform:rotate(-45deg);border:2px solid #fff;` +
          `box-shadow:0 1px 4px rgba(0,0,0,.4)"></span>`,
      });
      const popup = `<strong>${point.title}</strong>${point.subtitle ? '<br>' + point.subtitle : ''}`;
      L.marker([point.lat, point.lng], { icon }).bindPopup(popup).addTo(this.markers);
      coords.push([point.lat, point.lng]);
    }

    if (coords.length === 1) {
      this.map.setView(coords[0], 15);
    } else {
      this.map.fitBounds(L.latLngBounds(coords), { padding: [40, 40] });
    }
    // Ensure correct sizing after the container becomes visible.
    setTimeout(() => this.map?.invalidateSize(), 0);
  }
}

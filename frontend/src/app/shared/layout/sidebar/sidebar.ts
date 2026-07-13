import { SlicePipe } from '@angular/common';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, map, startWith } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { Role } from '../../../core/auth/models/role.enum';
import { NAV_SECTIONS, NavEntry, NavGroup, NavSection, isNavGroup } from './nav-config';

const COLLAPSE_STORAGE_KEY = 'sv_sidebar_collapsed';

@Component({
  selector: 'app-sidebar',
  imports: [SlicePipe, MatIconModule, MatTooltipModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export class Sidebar {
  protected readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly collapsed = signal(localStorage.getItem(COLLAPSE_STORAGE_KEY) === '1');
  protected readonly openGroups = signal<Record<string, boolean>>({});

  private readonly currentUrl = toSignal(
    this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd),
      map((event) => (event as NavigationEnd).urlAfterRedirects),
      startWith(this.router.url),
    ),
    { initialValue: this.router.url },
  );

  protected readonly sections = computed<NavSection[]>(() => {
    const role = this.authService.currentUser()?.role;
    if (!role) return [];
    return NAV_SECTIONS.map((section) => ({
      label: section.label,
      entries: section.entries
        .filter((entry) => entry.roles.includes(role))
        .map((entry) => this.filterEntry(entry, role)),
    })).filter((section) => section.entries.length > 0);
  });

  constructor() {
    effect(() => {
      const url = this.currentUrl();
      const updates: Record<string, boolean> = {};
      for (const section of NAV_SECTIONS) {
        for (const entry of section.entries) {
          if (isNavGroup(entry) && entry.items.some((item) => url.startsWith(item.route))) {
            updates[entry.label] = true;
          }
        }
      }
      if (Object.keys(updates).length > 0) {
        this.openGroups.update((current) => ({ ...current, ...updates }));
      }
    });
  }

  protected isGroup(entry: NavEntry): entry is NavGroup {
    return isNavGroup(entry);
  }

  /** The "Mi panel" entry links to /app, which redirects to /app/panel/<role>;
   *  keep it highlighted while the user is on any role panel. */
  protected isPanelHome(entry: NavEntry): boolean {
    return !isNavGroup(entry) && entry.route === '/app' && this.currentUrl().startsWith('/app/panel');
  }

  protected isGroupActive(group: NavGroup): boolean {
    const url = this.currentUrl();
    return group.items.some((item) => url === item.route || url.startsWith(item.route + '/'));
  }

  protected isGroupOpen(label: string): boolean {
    return !!this.openGroups()[label];
  }

  protected toggleGroup(label: string): void {
    if (this.collapsed()) {
      this.setCollapsed(false);
    }
    this.openGroups.update((current) => ({ ...current, [label]: !current[label] }));
  }

  protected toggleCollapsed(): void {
    this.setCollapsed(!this.collapsed());
  }

  private setCollapsed(value: boolean): void {
    this.collapsed.set(value);
    localStorage.setItem(COLLAPSE_STORAGE_KEY, value ? '1' : '0');
  }

  private filterEntry(entry: NavEntry, role: Role): NavEntry {
    if (!isNavGroup(entry)) {
      return entry;
    }
    return { ...entry, items: entry.items.filter((item) => item.roles.includes(role)) };
  }
}

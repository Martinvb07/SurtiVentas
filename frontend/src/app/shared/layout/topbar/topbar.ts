import { DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { NotificationService } from '../../../core/realtime/notification.service';

@Component({
  selector: 'app-topbar',
  imports: [DatePipe, MatIconModule, MatBadgeModule, MatButtonModule, MatMenuModule],
  templateUrl: './topbar.html',
  styleUrl: './topbar.scss',
})
export class Topbar {
  protected readonly notifications = inject(NotificationService);

  constructor(
    protected readonly authService: AuthService,
    private readonly router: Router,
  ) {}

  protected onNotificationsOpened(): void {
    this.notifications.markAllRead();
  }

  protected logout(): void {
    this.notifications.disconnect();
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}

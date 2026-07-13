import { Component, OnDestroy, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NotificationService } from '../../../core/realtime/notification.service';
import { Sidebar } from '../sidebar/sidebar';
import { Topbar } from '../topbar/topbar';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, Sidebar, Topbar],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
})
export class Shell implements OnDestroy {
  private readonly notifications = inject(NotificationService);

  constructor() {
    this.notifications.connect();
  }

  ngOnDestroy(): void {
    this.notifications.disconnect();
  }
}

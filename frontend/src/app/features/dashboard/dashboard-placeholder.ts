import { Component } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-dashboard-placeholder',
  templateUrl: './dashboard-placeholder.html',
  styleUrl: './dashboard-placeholder.scss',
})
export class DashboardPlaceholder {
  constructor(protected readonly authService: AuthService) {}
}

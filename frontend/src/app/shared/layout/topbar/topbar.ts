import { Component } from '@angular/core';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-topbar',
  imports: [MatIconModule, MatBadgeModule, MatButtonModule, MatMenuModule],
  templateUrl: './topbar.html',
  styleUrl: './topbar.scss',
})
export class Topbar {
  constructor(
    protected readonly authService: AuthService,
    private readonly router: Router,
  ) {}

  protected logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}

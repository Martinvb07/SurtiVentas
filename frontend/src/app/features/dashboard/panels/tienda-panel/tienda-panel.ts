import { Component, inject } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../../core/auth/auth.service';
import { QuickAction } from '../../ui/quick-action/quick-action';

/**
 * Self-service portal for the store owner (COMPRADOR). The portal itself is a
 * Phase 2 deliverable — customers aren't yet linked to user accounts — so this
 * panel presents the planned capabilities as upcoming rather than faking data.
 */
@Component({
  selector: 'app-tienda-panel',
  imports: [MatIconModule, QuickAction],
  templateUrl: './tienda-panel.html',
})
export class TiendaPanel {
  protected readonly auth = inject(AuthService);
}

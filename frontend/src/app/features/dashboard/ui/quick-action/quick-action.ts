import { Component, Input, booleanAttribute } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';

/**
 * A shortcut tile linking to an existing feature screen. When {@link soon} is
 * set (or no route is given) it renders disabled with a "Próximamente" badge —
 * used for capabilities that belong to later roadmap phases.
 */
@Component({
  selector: 'app-quick-action',
  imports: [RouterLink, MatIconModule],
  templateUrl: './quick-action.html',
  styleUrl: './quick-action.scss',
})
export class QuickAction {
  @Input() label = '';
  @Input() icon = 'bolt';
  @Input() route?: string;
  @Input({ transform: booleanAttribute }) soon = false;
}

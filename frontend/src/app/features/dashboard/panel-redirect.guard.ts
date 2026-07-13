import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { panelPathForRole } from './panel-routes';

/**
 * Index redirect for `/app`: sends each authenticated user to the dashboard
 * built for their role. This is what makes login land on the right panel.
 */
export const panelRedirectGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const user = auth.currentUser();
  return router.createUrlTree([user ? panelPathForRole(user.role) : '/login']);
};

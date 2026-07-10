import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { Role } from './models/role.enum';

/**
 * Guard factory for future role-scoped feature routes, e.g.
 * `canActivate: [roleGuard([Role.ADMINISTRADOR, Role.BODEGUERO])]` on the
 * catalog module's route config.
 */
export function roleGuard(allowedRoles: Role[]): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    const user = authService.currentUser();
    if (user && allowedRoles.includes(user.role)) {
      return true;
    }

    return router.createUrlTree(['/app']);
  };
}

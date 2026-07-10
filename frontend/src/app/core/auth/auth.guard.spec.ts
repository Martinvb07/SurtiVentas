import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, provideRouter } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthService } from './auth.service';

describe('authGuard', () => {
  function configure(authenticated: boolean) {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: { isAuthenticated: signal(authenticated) } },
      ],
    });
    return TestBed.inject(Router);
  }

  function runGuard(url: string) {
    const route = {} as ActivatedRouteSnapshot;
    const state = { url } as RouterStateSnapshot;
    return TestBed.runInInjectionContext(() => authGuard(route, state));
  }

  it('allows navigation when authenticated', () => {
    configure(true);

    const result = runGuard('/app');

    expect(result).toBe(true);
  });

  it('redirects to /login with a returnUrl when not authenticated', () => {
    const router = configure(false);

    const result = runGuard('/app');

    const expectedTree = router.createUrlTree(['/login'], { queryParams: { returnUrl: '/app' } });
    expect(router.serializeUrl(result as ReturnType<Router['createUrlTree']>))
      .toBe(router.serializeUrl(expectedTree));
  });
});

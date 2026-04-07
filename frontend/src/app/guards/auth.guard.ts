import { inject } from '@angular/core';
import { CanActivateFn, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const auth = inject(AuthService);

  if (auth.currentUser()) return true;

  const redirectTo = '/' + route.url.map((s) => s.path).join('/');
  auth.loginWithGoogle({ redirectTo });
  return false;
};

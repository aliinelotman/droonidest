import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.currentUser()) return true;

  // Do not trigger a login popup here — browsers block popups that are not
  // opened from a direct user gesture. Redirect to home where the user can
  // click the Login button themselves.
  return router.createUrlTree(['/']);
};

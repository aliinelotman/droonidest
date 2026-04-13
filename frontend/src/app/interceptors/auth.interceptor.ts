import { inject } from '@angular/core';
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { tap } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.getAccessToken();

  const outgoing = req.clone({
    withCredentials: true,
    ...(token ? { setHeaders: { Authorization: `Bearer ${token}` } } : {}),
  });

  return next(outgoing).pipe(
    tap({
      error: (err) => {
        if (err instanceof HttpErrorResponse && err.status === 401 && auth.getAccessToken() !== null) {
          auth.logout();
        }
      },
    })
  );
};

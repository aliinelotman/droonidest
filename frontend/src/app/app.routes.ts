import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { adminGuard } from './guards/admin.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/module-one/module-one.component').then(
        (m) => m.ModuleOneComponent
      ),
  },
  {
    path: 'module-two',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/module-two/module-two.component').then(
        (m) => m.ModuleTwoComponent
      ),
  },
  {
    path: 'admin',
    canActivate: [authGuard, adminGuard],
    loadComponent: () =>
      import('./pages/admin/admin-page.component').then(
        (m) => m.AdminPageComponent
      ),
  },
  {
    path: 'auth/callback',
    loadComponent: () =>
      import('./pages/auth-callback/auth-callback.component').then(
        (m) => m.AuthCallbackComponent
      ),
  },
];

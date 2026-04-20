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
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./pages/admin/landing/admin-landing.component').then(
            (m) => m.AdminLandingComponent
          ),
      },
      {
        path: 'content',
        loadComponent: () =>
          import('./pages/admin/content/admin-content-page.component').then(
            (m) => m.AdminContentPageComponent
          ),
      },
      {
        path: 'users',
        data: { section: 'users' },
        loadComponent: () =>
          import('./pages/admin/stub/admin-stub.component').then(
            (m) => m.AdminStubComponent
          ),
      },
      {
        path: 'analytics',
        data: { section: 'analytics' },
        loadComponent: () =>
          import('./pages/admin/stub/admin-stub.component').then(
            (m) => m.AdminStubComponent
          ),
      },
      {
        path: 'payments',
        data: { section: 'payments' },
        loadComponent: () =>
          import('./pages/admin/stub/admin-stub.component').then(
            (m) => m.AdminStubComponent
          ),
      },
      {
        path: 'email',
        data: { section: 'email' },
        loadComponent: () =>
          import('./pages/admin/stub/admin-stub.component').then(
            (m) => m.AdminStubComponent
          ),
      },
    ],
  },
  {
    path: 'auth/callback',
    loadComponent: () =>
      import('./pages/auth-callback/auth-callback.component').then(
        (m) => m.AuthCallbackComponent
      ),
  },
];

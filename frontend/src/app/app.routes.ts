import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/module-one/module-one.component').then(
        (m) => m.DroneExplorerComponent
      ),
  },
  {
    path: 'module-two',
    loadComponent: () =>
      import('./pages/module-two/module-two.component').then(
        (m) => m.ModuleTwoComponent
      ),
  },
];

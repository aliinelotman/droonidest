import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-admin-landing',
  standalone: true,
  imports: [],
  templateUrl: './admin-landing.component.html',
  styleUrl: './admin-landing.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminLandingComponent {}

import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { map, Observable } from 'rxjs';

export type AdminSection = 'users' | 'analytics' | 'payments' | 'email';

const SECTION_LABELS: Readonly<Record<AdminSection, string>> = {
  users: 'User Management',
  analytics: 'Analytics Dashboard',
  payments: 'Payment Processing',
  email: 'Email Notifications',
};

@Component({
  selector: 'app-admin-stub',
  standalone: true,
  imports: [AsyncPipe],
  templateUrl: './admin-stub.component.html',
  styleUrl: './admin-stub.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminStubComponent {
  private readonly route = inject(ActivatedRoute);

  readonly label$: Observable<string> = this.route.data.pipe(
    map((data) => SECTION_LABELS[data['section'] as AdminSection] ?? 'Admin'),
  );
}

import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterModule } from '@angular/router';

interface AdminSubmenuItem {
  readonly label: string;
  readonly route: string;
  readonly enabled: boolean;
}

@Component({
  selector: 'app-admin-submenu',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './admin-submenu.component.html',
  styleUrl: './admin-submenu.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminSubmenuComponent {
  readonly items: ReadonlyArray<AdminSubmenuItem> = [
    { label: 'User Management',     route: '/admin/users',     enabled: false },
    { label: 'Content Management',  route: '/admin/content',   enabled: true  },
    { label: 'Analytics Dashboard', route: '/admin/analytics', enabled: false },
    { label: 'Payment Processing',  route: '/admin/payments',  enabled: false },
    { label: 'Email Notifications', route: '/admin/email',     enabled: false },
  ];
}

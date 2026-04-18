import { ChangeDetectionStrategy, Component } from '@angular/core';
import { DatePipe, NgFor } from '@angular/common';

interface DashboardCategory {
  title: string;
  modifiedAt: string; // ISO date
}

@Component({
  selector: 'app-admin-dashboard-list',
  standalone: true,
  imports: [NgFor, DatePipe],
  templateUrl: './admin-dashboard-list.component.html',
  styleUrl: './admin-dashboard-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminDashboardListComponent {
  readonly categories: readonly DashboardCategory[] = [
    { title: 'User Authentication', modifiedAt: '2026-04-15' },
    { title: 'Content Management', modifiedAt: '2026-04-14' },
    { title: 'Analytics Dashboard', modifiedAt: '2026-04-12' },
    { title: 'Payment Processing', modifiedAt: '2026-04-10' },
    { title: 'Email Notifications', modifiedAt: '2026-04-08' },
  ];
}

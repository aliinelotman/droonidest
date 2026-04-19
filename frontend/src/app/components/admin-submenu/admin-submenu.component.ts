import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  EventEmitter,
  HostListener,
  inject,
  Output,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterModule } from '@angular/router';
import { filter } from 'rxjs/operators';

interface AdminSubmenuItem {
  readonly label: string;
  readonly route: string;
  readonly enabled: boolean;
}

const HOVER_GRACE_MS = 150;

@Component({
  selector: 'app-admin-submenu',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './admin-submenu.component.html',
  styleUrl: './admin-submenu.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminSubmenuComponent {
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly items: ReadonlyArray<AdminSubmenuItem> = [
    { label: 'User Management',     route: '/admin/users',     enabled: false },
    { label: 'Content Management',  route: '/admin/content',   enabled: true  },
    { label: 'Analytics Dashboard', route: '/admin/analytics', enabled: false },
    { label: 'Payment Processing',  route: '/admin/payments',  enabled: false },
    { label: 'Email Notifications', route: '/admin/email',     enabled: false },
  ];

  readonly pinned = signal(false);
  readonly hovered = signal(false);
  readonly routeIsAdminChild = signal(this.computeRouteIsAdminChild(this.router.url));

  readonly open = computed(
    () => this.pinned() || this.hovered() || this.routeIsAdminChild(),
  );

  constructor() {
    this.router.events
      .pipe(
        filter((e): e is NavigationEnd => e instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((event) => {
        this.routeIsAdminChild.set(this.computeRouteIsAdminChild(event.urlAfterRedirects));
      });
  }

  private hoverGraceTimer: ReturnType<typeof setTimeout> | null = null;

  @Output() readonly itemClicked = new EventEmitter<void>();

  onTriggerClick(): void {
    this.pinned.update((value) => !value);
  }

  onItemClick(item: AdminSubmenuItem): void {
    if (!item.enabled) return;
    this.itemClicked.emit();
  }

  onTriggerEnter(): void {
    this.cancelHoverGrace();
    this.hovered.set(true);
  }

  onTriggerLeave(): void {
    this.scheduleHoverClear();
  }

  onPanelEnter(): void {
    this.cancelHoverGrace();
    this.hovered.set(true);
  }

  onPanelLeave(): void {
    this.scheduleHoverClear();
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.pinned()) {
      this.pinned.set(false);
    }
  }

  private scheduleHoverClear(): void {
    this.cancelHoverGrace();
    this.hoverGraceTimer = setTimeout(() => {
      this.hovered.set(false);
      this.hoverGraceTimer = null;
    }, HOVER_GRACE_MS);
  }

  private cancelHoverGrace(): void {
    if (this.hoverGraceTimer !== null) {
      clearTimeout(this.hoverGraceTimer);
      this.hoverGraceTimer = null;
    }
  }

  private computeRouteIsAdminChild(url: string): boolean {
    // True for /admin/<anything>, false for bare /admin and non-admin routes.
    return /^\/admin\/[^/]/.test(url);
  }
}

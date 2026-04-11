import { Component, HostListener, Input, AfterViewInit, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

export interface NavSection {
  id: string;
  label: string;
}

export interface ModuleLink {
  label: string;
  route: string;
  active?: boolean;
}

@Component({
  selector: 'app-section-nav',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './section-nav.component.html',
  styleUrl: './section-nav.component.scss',
})
export class SectionNavComponent implements AfterViewInit {
  private readonly authService = inject(AuthService);

  @Input() sections: NavSection[] = [];
  @Input() moduleLinks: ModuleLink[] = [];
  activeId: string = '';
  sidebarOpen = false;
  scrollPercent = 0;
  exploding = false;

  private readonly storageKey = 'scrollPos:';
  private hasExploded = false;
  private explosionTimeout: ReturnType<typeof setTimeout> | null = null;

  ngAfterViewInit(): void {
    const saved = sessionStorage.getItem(this.storageKey + location.pathname);
    if (saved) {
      setTimeout(() => window.scrollTo(0, Number(saved)), 50);
    }
  }

  @HostListener('window:scroll')
  onScroll(): void {
    const docHeight = document.documentElement.scrollHeight - window.innerHeight;
    this.scrollPercent = docHeight > 0 ? (window.scrollY / docHeight) * 100 : 0;
    sessionStorage.setItem(this.storageKey + location.pathname, String(window.scrollY));

    if (this.scrollPercent >= 99 && !this.hasExploded) {
      this.hasExploded = true;
      this.exploding = true;
      this.explosionTimeout = setTimeout(() => (this.exploding = false), 700);
    } else if (this.scrollPercent < 95) {
      this.hasExploded = false;
    }
  }

  readonly currentUser = this.authService.currentUser;

  login(): void {
    this.closeSidebar();
    this.authService.loginWithGoogle();
  }

  logout(): void {
    this.closeSidebar();
    this.authService.logout();
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.sidebarOpen) this.closeSidebar();
  }

  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
  }

  closeSidebar(): void {
    this.sidebarOpen = false;
  }

  scrollTo(sectionId: string): void {
    this.closeSidebar();
    const el = document.getElementById(sectionId);
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

  setActive(id: string): void {
    this.activeId = id;
  }
}

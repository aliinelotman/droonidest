import { Component, Input } from '@angular/core';
import { RouterModule } from '@angular/router';

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
export class SectionNavComponent {
  @Input() sections: NavSection[] = [];
  @Input() moduleLinks: ModuleLink[] = [];
  activeId: string = '';
  sidebarOpen = false;

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

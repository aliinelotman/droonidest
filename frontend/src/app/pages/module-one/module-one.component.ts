import {
  Component,
  AfterViewInit,
  OnDestroy,
  QueryList,
  ViewChildren,
  ElementRef,
  ViewChild,
  inject,
  computed,
} from '@angular/core';
import { HeroSectionComponent } from '../../components/hero-section/hero-section.component';
import { InfoCardComponent } from '../../components/info-card/info-card.component';
import { NarrativeSectionComponent } from '../../components/narrative-section/narrative-section.component';
import { GalleryComponent } from '../../components/gallery/gallery.component';
import { FunFactComponent } from '../../components/fun-fact/fun-fact.component';
import { ClosingSectionComponent } from '../../components/closing-section/closing-section.component';
import { QuizComponent } from '../../components/quiz/quiz.component';
import { DroneAssemblyComponent } from '../../components/drone-assembly/drone-assembly.component';
import {
  SectionNavComponent,
  NavSection,
  ModuleLink,
} from '../../components/section-nav/section-nav.component';
import { ModuleCardComponent } from '../../components/module-card/module-card.component';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-module-one',
  standalone: true,
  imports: [
    HeroSectionComponent,
    InfoCardComponent,
    NarrativeSectionComponent,
    GalleryComponent,
    FunFactComponent,
    ClosingSectionComponent,
    QuizComponent,
    DroneAssemblyComponent,
    SectionNavComponent,
    ModuleCardComponent,
  ],
  templateUrl: './module-one.component.html',
  styleUrl: './module-one.component.scss',
})
export class ModuleOneComponent implements AfterViewInit, OnDestroy {
  protected readonly authService = inject(AuthService);

  @ViewChildren('fadeTarget') fadeTargets!: QueryList<ElementRef>;
  @ViewChild('sectionNav') sectionNav!: SectionNavComponent;

  private fadeObserver!: IntersectionObserver;
  private navObserver!: IntersectionObserver;

  readonly isModuleTwoLocked = computed(() => !this.authService.currentUser());

  navSections: NavSection[] = [
    { id: 'hero', label: 'Sissejuhatus' },
    { id: 'overview', label: 'Ülevaade' },
    { id: 'exercise', label: 'Harjutus' },
    { id: 'gallery', label: 'Galerii' },
    { id: 'operations', label: 'Juhtimine' },
    { id: 'quiz', label: 'Test' },
    { id: 'modules', label: 'Moodulid' },
  ];

  moduleLinks: ModuleLink[] = [
    { label: 'Moodul 1', route: '/', active: true },
    { label: 'Moodul 2', route: '/module-two' },
  ];

  ngAfterViewInit(): void {
    this.setupFadeObserver();
    this.setupNavObserver();
  }

  ngOnDestroy(): void {
    this.fadeObserver?.disconnect();
    this.navObserver?.disconnect();
  }

  private setupFadeObserver(): void {
    this.fadeObserver = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('visible');
          }
        });
      },
      { threshold: 0.15 }
    );

    this.fadeTargets.forEach((el) => this.fadeObserver.observe(el.nativeElement));
  }

  private setupNavObserver(): void {
    this.navObserver = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting && this.sectionNav) {
            this.sectionNav.setActive(entry.target.id);
          }
        });
      },
      { threshold: 0.3 }
    );

    this.navSections.forEach((s) => {
      const el = document.getElementById(s.id);
      if (el) this.navObserver.observe(el);
    });
  }
}

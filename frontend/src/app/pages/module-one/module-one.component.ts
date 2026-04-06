import {
  Component,
  AfterViewInit,
  OnDestroy,
  QueryList,
  ViewChildren,
  ElementRef,
  ViewChild,
} from '@angular/core';
import { HeroSectionComponent } from '../../components/hero-section/hero-section.component';
import { InfoCardComponent } from '../../components/info-card/info-card.component';
import { NarrativeSectionComponent } from '../../components/narrative-section/narrative-section.component';
import { GalleryComponent } from '../../components/gallery/gallery.component';
import { FunFactComponent } from '../../components/fun-fact/fun-fact.component';
import { ClosingSectionComponent } from '../../components/closing-section/closing-section.component';
import { QuizComponent } from '../../components/quiz/quiz.component';
import {
  SectionNavComponent,
  NavSection,
  ModuleLink,
} from '../../components/section-nav/section-nav.component';
import { ModuleCardComponent } from '../../components/module-card/module-card.component';

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
    SectionNavComponent,
    ModuleCardComponent,
  ],
  templateUrl: './module-one.component.html',
  styleUrl: './module-one.component.scss',
})
export class ModuleOneComponent implements AfterViewInit, OnDestroy {
  @ViewChildren('fadeTarget') fadeTargets!: QueryList<ElementRef>;
  @ViewChild('sectionNav') sectionNav!: SectionNavComponent;

  private fadeObserver!: IntersectionObserver;
  private navObserver!: IntersectionObserver;

  navSections: NavSection[] = [
    { id: 'hero', label: 'Intro' },
    { id: 'overview', label: 'Overview' },
    { id: 'gallery', label: 'Arsenal' },
    { id: 'operations', label: 'Operations' },
    { id: 'future', label: 'Future' },
    { id: 'quiz', label: 'Quiz' },
  ];

  moduleLinks: ModuleLink[] = [
    { label: 'Module One', route: '/', active: true },
    { label: 'Module Two', route: '/module-two' },
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

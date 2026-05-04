import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  OnDestroy,
  QueryList,
  ViewChildren,
} from '@angular/core';
import { NarrativeSectionComponent } from '../narrative-section/narrative-section.component';

export interface NarrativeTrioItem {
  body: string;
  imageUrl: string;
  imageAlt: string;
}

@Component({
  selector: 'app-narrative-trio',
  standalone: true,
  imports: [NarrativeSectionComponent],
  templateUrl: './narrative-trio.component.html',
  styleUrl: './narrative-trio.component.scss',
})
export class NarrativeTrioComponent implements AfterViewInit, OnDestroy {
  @Input() heading = '';
  @Input({ required: true }) items!: NarrativeTrioItem[];

  @ViewChildren('fadeTarget') fadeTargets!: QueryList<ElementRef<HTMLElement>>;

  private fadeObserver?: IntersectionObserver;

  ngAfterViewInit(): void {
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

    this.fadeTargets.forEach((el) => this.fadeObserver!.observe(el.nativeElement));
  }

  ngOnDestroy(): void {
    this.fadeObserver?.disconnect();
  }
}

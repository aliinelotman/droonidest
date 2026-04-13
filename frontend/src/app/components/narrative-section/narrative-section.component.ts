import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-narrative-section',
  standalone: true,
  templateUrl: './narrative-section.component.html',
  styleUrl: './narrative-section.component.scss',
})
export class NarrativeSectionComponent {
  @Input({ required: true }) heading!: string;
  @Input({ required: true }) body!: string;
  @Input() imageUrl = '';
  @Input() imageAlt = 'Illustration';
  @Input() reverse = false;
}

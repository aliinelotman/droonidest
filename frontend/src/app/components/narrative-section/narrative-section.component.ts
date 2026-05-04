import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-narrative-section',
  standalone: true,
  templateUrl: './narrative-section.component.html',
  styleUrl: './narrative-section.component.scss',
})
export class NarrativeSectionComponent {
  @Input() heading = '';
  @Input({ required: true }) body!: string;
  @Input() imageUrl = '';
  @Input() imageAlt = 'Illustration';
  @Input() reverse = false;

  /** Body split on blank lines; single block stays one paragraph. */
  get bodyParagraphs(): string[] {
    const parts = this.body
      .split(/\r?\n\s*\r?\n/)
      .map((p) => p.trim())
      .filter((p) => p.length > 0);
    return parts.length > 0 ? parts : [this.body];
  }
}

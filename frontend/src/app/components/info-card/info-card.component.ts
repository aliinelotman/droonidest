import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-info-card',
  standalone: true,
  templateUrl: './info-card.component.html',
  styleUrl: './info-card.component.scss',
})
export class InfoCardComponent {
  @Input({ required: true }) title!: string;
  @Input({ required: true }) body!: string;
  @Input() accentColor = 'var(--color-accent)';
}

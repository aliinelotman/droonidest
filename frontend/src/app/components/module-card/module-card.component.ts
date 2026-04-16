import { Component, Input, Output, EventEmitter } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-module-card',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './module-card.component.html',
  styleUrl: './module-card.component.scss',
})
export class ModuleCardComponent {
  @Input({ required: true }) headline!: string;
  @Input({ required: true }) intro!: string;
  @Input() link: string | null = null;
  @Input() disabled = false;
  @Input() locked = false;

  @Output() loginRequested = new EventEmitter<void>();

  onCardClick(event: Event): void {
    if (this.locked) {
      event.preventDefault();
      this.loginRequested.emit();
    }
  }
}

import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
  inject,
  signal,
} from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { ModuleService } from '../../../services/module.service';
import { LessonResponse } from '../../../services/module.types';

@Component({
  selector: 'app-lesson-editor-modal',
  standalone: true,
  imports: [NgFor, NgIf],
  templateUrl: './lesson-editor-modal.component.html',
  styleUrl: './lesson-editor-modal.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LessonEditorModalComponent implements OnChanges {
  private readonly moduleService = inject(ModuleService);

  @Input({ required: true }) moduleId!: string;
  @Input() moduleTitle: string = '';

  @Output() readonly close = new EventEmitter<void>();

  readonly lessons = signal<LessonResponse[]>([]);
  readonly loading = signal<boolean>(true);
  readonly errorMessage = signal<string | null>(null);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['moduleId'] && this.moduleId) {
      void this.loadLessons();
    }
  }

  private async loadLessons(): Promise<void> {
    this.loading.set(true);
    this.errorMessage.set(null);
    try {
      const data = await this.moduleService.listLessons(this.moduleId);
      this.lessons.set(data);
    } catch {
      this.errorMessage.set('Could not load lessons');
    } finally {
      this.loading.set(false);
    }
  }

  isActive(lesson: LessonResponse): boolean {
    return lesson.status === 'PUBLISHED';
  }

  typeTag(lesson: LessonResponse): string {
    return lesson.videoUrl ? 'Video' : 'Read';
  }

  onClose(): void {
    this.close.emit();
  }
}

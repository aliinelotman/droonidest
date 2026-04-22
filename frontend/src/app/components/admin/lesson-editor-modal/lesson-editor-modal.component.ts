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
import { FormsModule } from '@angular/forms';
import { ModuleService } from '../../../services/module.service';
import { LessonResponse, ContentFormat } from '../../../services/module.types';

@Component({
  selector: 'app-lesson-editor-modal',
  standalone: true,
  imports: [NgFor, NgIf, FormsModule],
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

  readonly editingLesson = signal<LessonResponse | null>(null);
  readonly editTitle = signal('');
  readonly editContent = signal('');
  readonly editIsVideo = signal(false);
  readonly editVideoUrl = signal('');
  readonly editFormat = signal<ContentFormat>('MARKDOWN');
  readonly saving = signal(false);

  readonly showCreateForm = signal(false);
  readonly createTitle = signal('');
  readonly createContent = signal('');
  readonly createIsVideo = signal(false);
  readonly createVideoUrl = signal('');
  readonly createFormat = signal<ContentFormat>('MARKDOWN');
  readonly creating = signal(false);

  readonly deleteTarget = signal<LessonResponse | null>(null);
  readonly deleting = signal(false);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['moduleId'] && this.moduleId) {
      void this.loadLessons();
    }
  }

  private async loadLessons(): Promise<void> {
    this.loading.set(true);
    this.errorMessage.set(null);
    try {
      const detail = await this.moduleService.manageGetModule(this.moduleId);
      const fullLessons = await Promise.all(
        detail.lessons.map((s) => this.moduleService.manageGetLesson(s.id))
      );
      this.lessons.set(fullLessons);
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

  /* ── Edit lesson ── */

  onEditLesson(lesson: LessonResponse): void {
    this.editingLesson.set(lesson);
    this.editTitle.set(lesson.title);
    this.editContent.set(lesson.content ?? '');
    this.editIsVideo.set(!!lesson.videoUrl);
    this.editVideoUrl.set(lesson.videoUrl ?? '');
    this.editFormat.set(lesson.contentFormat ?? 'MARKDOWN');
  }

  onEditCancel(): void {
    this.editingLesson.set(null);
  }

  async onEditSave(): Promise<void> {
    const lesson = this.editingLesson();
    if (!lesson) return;
    this.saving.set(true);
    try {
      const updated = await this.moduleService.updateLesson(lesson.id, {
        title: this.editTitle().trim(),
        content: this.editContent(),
        contentFormat: this.editFormat(),
        videoUrl: this.editIsVideo() ? this.editVideoUrl().trim() || null : null,
        ifFreePreview: lesson.ifFreePreview,
      });
      this.lessons.update((list) => list.map((l) => (l.id === updated.id ? updated : l)));
      this.editingLesson.set(null);
    } catch {
      this.errorMessage.set('Could not save lesson');
    } finally {
      this.saving.set(false);
    }
  }

  /* ── Delete lesson ── */

  onDeleteRequest(lesson: LessonResponse): void {
    this.deleteTarget.set(lesson);
  }

  onDeleteCancel(): void {
    this.deleteTarget.set(null);
  }

  async onDeleteConfirm(): Promise<void> {
    const target = this.deleteTarget();
    if (!target) return;
    this.deleting.set(true);
    try {
      await this.moduleService.deleteLesson(target.id);
      this.lessons.update((list) => list.filter((l) => l.id !== target.id));
      this.deleteTarget.set(null);
    } catch {
      this.errorMessage.set('Could not delete lesson');
    } finally {
      this.deleting.set(false);
    }
  }

  /* ── Create lesson ── */

  onCreateOpen(): void {
    this.showCreateForm.set(true);
    this.createTitle.set('');
    this.createContent.set('');
    this.createIsVideo.set(false);
    this.createVideoUrl.set('');
    this.createFormat.set('MARKDOWN');
  }

  onCreateCancel(): void {
    this.showCreateForm.set(false);
  }

  async onCreateSubmit(): Promise<void> {
    const title = this.createTitle().trim();
    if (!title) return;
    this.creating.set(true);
    try {
      const created = await this.moduleService.createLesson(this.moduleId, {
        title,
        content: this.createContent(),
        contentFormat: this.createFormat(),
        videoUrl: this.createIsVideo() ? this.createVideoUrl().trim() || null : null,
        ifFreePreview: false,
      });
      this.lessons.update((list) => [...list, created]);
      this.showCreateForm.set(false);
    } catch {
      this.errorMessage.set('Could not create lesson');
    } finally {
      this.creating.set(false);
    }
  }
}

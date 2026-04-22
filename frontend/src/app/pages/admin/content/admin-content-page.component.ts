import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { NgIf } from '@angular/common';
import { StudyModulesListComponent } from '../../../components/admin/study-modules-list/study-modules-list.component';
import { LessonEditorModalComponent } from '../../../components/admin/lesson-editor-modal/lesson-editor-modal.component';
import { ModuleResponse } from '../../../services/module.types';

@Component({
  selector: 'app-admin-content-page',
  standalone: true,
  imports: [NgIf, StudyModulesListComponent, LessonEditorModalComponent],
  templateUrl: './admin-content-page.component.html',
  styleUrl: './admin-content-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminContentPageComponent {
  readonly activeModule = signal<ModuleResponse | null>(null);

  onEditModule(module: ModuleResponse): void {
    this.activeModule.set(module);
  }

  onCloseModal(): void {
    this.activeModule.set(null);
  }
}

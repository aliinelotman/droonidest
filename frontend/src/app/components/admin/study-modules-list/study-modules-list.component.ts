import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  OnInit,
  Output,
  inject,
  signal,
} from '@angular/core';
import { DatePipe, NgFor, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ModuleService } from '../../../services/module.service';
import { ModuleResponse, ContentStatus } from '../../../services/module.types';

@Component({
  selector: 'app-study-modules-list',
  standalone: true,
  imports: [NgFor, NgIf, DatePipe, FormsModule],
  templateUrl: './study-modules-list.component.html',
  styleUrl: './study-modules-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StudyModulesListComponent implements OnInit {
  private readonly moduleService = inject(ModuleService);

  readonly modules = signal<ModuleResponse[]>([]);
  readonly loading = signal<boolean>(true);
  readonly errorMessage = signal<string | null>(null);

  readonly showCreateForm = signal(false);
  readonly createTitle = signal('');
  readonly createDescription = signal('');
  readonly creating = signal(false);

  readonly deleteTarget = signal<ModuleResponse | null>(null);
  readonly deleting = signal(false);

  @Output() readonly editRequested = new EventEmitter<ModuleResponse>();

  async ngOnInit(): Promise<void> {
    await this.loadModules();
  }

  private async loadModules(): Promise<void> {
    this.loading.set(true);
    this.errorMessage.set(null);
    try {
      const data = await this.moduleService.manageListModules();
      this.modules.set(data);
    } catch {
      this.errorMessage.set('Could not load modules');
    } finally {
      this.loading.set(false);
    }
  }

  isActive(module: ModuleResponse): boolean {
    return module.status === 'PUBLISHED';
  }

  async onToggle(module: ModuleResponse): Promise<void> {
    const newStatus: ContentStatus = module.status === 'PUBLISHED' ? 'DRAFT' : 'PUBLISHED';
    try {
      const updated = await this.moduleService.updateModule(module.id, {
        title: module.title,
        description: module.description,
        thumbnailUrl: module.thumbnailUrl,
        ifFreePreview: module.ifFreePreview,
        status: newStatus,
      });
      this.modules.update((list) => list.map((m) => (m.id === updated.id ? updated : m)));
    } catch {
      this.errorMessage.set('Could not update module status');
    }
  }

  onEdit(module: ModuleResponse): void {
    this.editRequested.emit(module);
  }

  onOpen(): void {
    // TODO: This should open the module by id in a new tab
    window.open('/', '_blank');
  }

  /* ── Delete flow ── */

  onDeleteRequest(module: ModuleResponse): void {
    this.deleteTarget.set(module);
  }

  onDeleteCancel(): void {
    this.deleteTarget.set(null);
  }

  async onDeleteConfirm(): Promise<void> {
    const target = this.deleteTarget();
    if (!target) return;
    this.deleting.set(true);
    try {
      await this.moduleService.deleteModule(target.id);
      this.modules.update((list) => list.filter((m) => m.id !== target.id));
      this.deleteTarget.set(null);
    } catch {
      this.errorMessage.set('Could not delete module');
    } finally {
      this.deleting.set(false);
    }
  }

  /* ── Create flow ── */

  onCreateOpen(): void {
    this.showCreateForm.set(true);
    this.createTitle.set('');
    this.createDescription.set('');
  }

  onCreateCancel(): void {
    this.showCreateForm.set(false);
  }

  async onCreateSubmit(): Promise<void> {
    const title = this.createTitle().trim();
    if (!title) return;
    this.creating.set(true);
    try {
      const created = await this.moduleService.createModule({
        title,
        description: this.createDescription().trim(),
        thumbnailUrl: null,
        ifFreePreview: false,
      });
      this.modules.update((list) => [...list, created]);
      this.showCreateForm.set(false);
    } catch {
      this.errorMessage.set('Could not create module');
    } finally {
      this.creating.set(false);
    }
  }
}

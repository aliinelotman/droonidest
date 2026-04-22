import { ChangeDetectionStrategy, Component, EventEmitter, OnInit, Output, inject, signal } from '@angular/core';
import { DatePipe, NgFor, NgIf } from '@angular/common';
import { ModuleService } from '../../../services/module.service';
import { ModuleResponse } from '../../../services/module.types';

@Component({
  selector: 'app-study-modules-list',
  standalone: true,
  imports: [NgFor, NgIf, DatePipe],
  templateUrl: './study-modules-list.component.html',
  styleUrl: './study-modules-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StudyModulesListComponent implements OnInit {
  private readonly moduleService = inject(ModuleService);

  readonly modules = signal<ModuleResponse[]>([]);
  readonly loading = signal<boolean>(true);
  readonly errorMessage = signal<string | null>(null);

  @Output() readonly editRequested = new EventEmitter<ModuleResponse>();

  async ngOnInit(): Promise<void> {
    try {
      const data = await this.moduleService.listModules();
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

  onEdit(module: ModuleResponse): void {
    this.editRequested.emit(module);
  }
}

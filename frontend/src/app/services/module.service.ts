import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  ModuleResponse,
  ModuleDetailResponse,
  LessonResponse,
  CreateModuleRequest,
  UpdateModuleRequest,
  CreateLessonRequest,
  UpdateLessonRequest,
} from './module.types';

@Injectable({ providedIn: 'root' })
export class ModuleService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;
  private readonly manageBase = `${environment.apiUrl}/api/v1/manage`;

  /* ── Public endpoints ── */

  listModules(): Promise<ModuleResponse[]> {
    return firstValueFrom(this.http.get<ModuleResponse[]>(`${this.apiUrl}/api/v1/modules`));
  }

  listLessons(moduleId: string): Promise<LessonResponse[]> {
    return firstValueFrom(
      this.http.get<LessonResponse[]>(`${this.apiUrl}/api/v1/modules/${moduleId}/lessons`)
    );
  }

  /* ── Manage module endpoints ── */

  manageListModules(): Promise<ModuleResponse[]> {
    return firstValueFrom(this.http.get<ModuleResponse[]>(`${this.manageBase}/modules`));
  }

  manageGetModule(id: string): Promise<ModuleDetailResponse> {
    return firstValueFrom(this.http.get<ModuleDetailResponse>(`${this.manageBase}/modules/${id}`));
  }

  createModule(req: CreateModuleRequest): Promise<ModuleResponse> {
    return firstValueFrom(this.http.post<ModuleResponse>(`${this.manageBase}/modules`, req));
  }

  updateModule(id: string, req: UpdateModuleRequest): Promise<ModuleResponse> {
    return firstValueFrom(this.http.put<ModuleResponse>(`${this.manageBase}/modules/${id}`, req));
  }

  deleteModule(id: string): Promise<void> {
    return firstValueFrom(this.http.delete<void>(`${this.manageBase}/modules/${id}`));
  }

  /* ── Manage lesson endpoints ── */

  manageGetLesson(id: string): Promise<LessonResponse> {
    return firstValueFrom(this.http.get<LessonResponse>(`${this.manageBase}/lessons/${id}`));
  }

  createLesson(moduleId: string, req: CreateLessonRequest): Promise<LessonResponse> {
    return firstValueFrom(
      this.http.post<LessonResponse>(`${this.manageBase}/modules/${moduleId}/lessons`, req)
    );
  }

  updateLesson(id: string, req: UpdateLessonRequest): Promise<LessonResponse> {
    return firstValueFrom(this.http.put<LessonResponse>(`${this.manageBase}/lessons/${id}`, req));
  }

  deleteLesson(id: string): Promise<void> {
    return firstValueFrom(this.http.delete<void>(`${this.manageBase}/lessons/${id}`));
  }
}

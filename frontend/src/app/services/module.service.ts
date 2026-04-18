import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../environments/environment';
import { ModuleResponse, LessonResponse } from './module.types';

@Injectable({ providedIn: 'root' })
export class ModuleService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  listModules(): Promise<ModuleResponse[]> {
    return firstValueFrom(this.http.get<ModuleResponse[]>(`${this.apiUrl}/api/modules`));
  }

  listLessons(moduleId: string): Promise<LessonResponse[]> {
    return firstValueFrom(
      this.http.get<LessonResponse[]>(`${this.apiUrl}/api/modules/${moduleId}/lessons`)
    );
  }
}

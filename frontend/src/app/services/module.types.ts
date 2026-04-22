export type ContentStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
export type ContentFormat = 'HTML' | 'MARKDOWN';

export interface ModuleResponse {
  id: string;
  title: string;
  description: string;
  thumbnailUrl: string | null;
  status: ContentStatus;
  sortOrder: number;
  ifFreePreview: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface LessonSummary {
  id: string;
  title: string;
}

export interface ModuleDetailResponse extends ModuleResponse {
  lessons: LessonSummary[];
}

export interface LessonResponse {
  id: string;
  moduleId: string;
  title: string;
  content: string;
  status: ContentStatus;
  contentFormat: ContentFormat;
  videoUrl: string | null;
  ifFreePreview: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateModuleRequest {
  title: string;
  description: string;
  thumbnailUrl: string | null;
  ifFreePreview: boolean;
}

export interface UpdateModuleRequest {
  title: string;
  description: string;
  thumbnailUrl: string | null;
  ifFreePreview: boolean;
  status: ContentStatus;
}

export interface CreateLessonRequest {
  title: string;
  content: string;
  contentFormat: ContentFormat;
  videoUrl: string | null;
  ifFreePreview: boolean;
}

export interface UpdateLessonRequest {
  title: string;
  content: string;
  contentFormat: ContentFormat;
  videoUrl: string | null;
  ifFreePreview: boolean;
}

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

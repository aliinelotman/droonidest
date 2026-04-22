import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ModuleService } from './module.service';
import { ModuleResponse, LessonResponse } from './module.types';

const mockModule: ModuleResponse = {
  id: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  title: 'Module 1',
  description: 'First module',
  thumbnailUrl: null,
  status: 'PUBLISHED',
  sortOrder: 0,
  ifFreePreview: false,
  createdAt: '2026-04-16T10:00:00Z',
  updatedAt: '2026-04-16T10:00:00Z',
};

const mockLesson: LessonResponse = {
  id: 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
  moduleId: mockModule.id,
  title: 'Introduction',
  content: 'Welcome',
  status: 'PUBLISHED',
  contentFormat: 'HTML',
  videoUrl: null,
  ifFreePreview: true,
  createdAt: '2026-04-16T10:00:00Z',
  updatedAt: '2026-04-16T10:00:00Z',
};

describe('ModuleService', () => {
  let service: ModuleService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ModuleService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ModuleService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('listModules GETs /api/modules and returns the list', async () => {
    const promise = service.listModules();
    const req = httpMock.expectOne('http://localhost:8080/api/modules');
    expect(req.request.method).toBe('GET');
    req.flush([mockModule]);
    await expectAsync(promise).toBeResolvedTo([mockModule]);
  });

  it('listLessons GETs /api/modules/:id/lessons and returns the list', async () => {
    const promise = service.listLessons(mockModule.id);
    const req = httpMock.expectOne(`http://localhost:8080/api/modules/${mockModule.id}/lessons`);
    expect(req.request.method).toBe('GET');
    req.flush([mockLesson]);
    await expectAsync(promise).toBeResolvedTo([mockLesson]);
  });
});

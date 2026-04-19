import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { AdminContentPageComponent } from './admin-content-page.component';

describe('AdminContentPageComponent', () => {
  let fixture: ComponentFixture<AdminContentPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminContentPageComponent],
      providers: [provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminContentPageComponent);
    fixture.detectChanges();
  });

  it('creates the component', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });
});

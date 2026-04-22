import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminLandingComponent } from './admin-landing.component';

describe('AdminLandingComponent', () => {
  let fixture: ComponentFixture<AdminLandingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminLandingComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminLandingComponent);
    fixture.detectChanges();
  });

  it('renders the heading', () => {
    const heading = fixture.nativeElement.querySelector('h1');
    expect(heading).toBeTruthy();
    expect(heading.textContent).toContain('Admin Console');
  });

  it('renders the prompt to pick a section', () => {
    const text = fixture.nativeElement.textContent ?? '';
    expect(text).toContain('Select a section from the menu');
  });
});

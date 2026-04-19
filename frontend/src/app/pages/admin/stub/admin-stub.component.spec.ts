import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { AdminStubComponent, AdminSection } from './admin-stub.component';

function configure(section: AdminSection) {
  return TestBed.configureTestingModule({
    imports: [AdminStubComponent],
    providers: [
      {
        provide: ActivatedRoute,
        useValue: { data: of({ section }) },
      },
    ],
  }).compileComponents();
}

describe('AdminStubComponent', () => {
  it('renders the User Management label for section=users', async () => {
    await configure('users');
    const fixture: ComponentFixture<AdminStubComponent> =
      TestBed.createComponent(AdminStubComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('User Management');
    expect(fixture.nativeElement.textContent).toContain('Coming soon');
  });

  it('renders the Analytics Dashboard label for section=analytics', async () => {
    await configure('analytics');
    const fixture: ComponentFixture<AdminStubComponent> =
      TestBed.createComponent(AdminStubComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Analytics Dashboard');
  });

  it('renders the Payment Processing label for section=payments', async () => {
    await configure('payments');
    const fixture: ComponentFixture<AdminStubComponent> =
      TestBed.createComponent(AdminStubComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Payment Processing');
  });

  it('renders the Email Notifications label for section=email', async () => {
    await configure('email');
    const fixture: ComponentFixture<AdminStubComponent> =
      TestBed.createComponent(AdminStubComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Email Notifications');
  });
});

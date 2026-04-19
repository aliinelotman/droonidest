import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AdminSubmenuComponent } from './admin-submenu.component';

describe('AdminSubmenuComponent', () => {
  let fixture: ComponentFixture<AdminSubmenuComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminSubmenuComponent],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminSubmenuComponent);
    fixture.detectChanges();
  });

  it('renders the ADMIN trigger button', () => {
    const trigger = fixture.nativeElement.querySelector('.admin-submenu__trigger');
    expect(trigger).toBeTruthy();
    expect(trigger.textContent.toLowerCase()).toContain('admin');
  });

  it('renders all 5 menu items in order', () => {
    const items = fixture.nativeElement.querySelectorAll('.admin-submenu__item');
    expect(items.length).toBe(5);
    expect(items[0].textContent).toContain('User Management');
    expect(items[1].textContent).toContain('Content Management');
    expect(items[2].textContent).toContain('Analytics Dashboard');
    expect(items[3].textContent).toContain('Payment Processing');
    expect(items[4].textContent).toContain('Email Notifications');
  });

  it('renders enabled items as anchors and disabled items as spans', () => {
    const items = fixture.nativeElement.querySelectorAll('.admin-submenu__item');
    // index 1 = Content Management (enabled)
    expect(items[1].querySelector('a')).toBeTruthy();
    expect(items[1].querySelector('span[aria-disabled="true"]')).toBeFalsy();
    // index 0 = Users (disabled)
    expect(items[0].querySelector('a')).toBeFalsy();
    expect(items[0].querySelector('span[aria-disabled="true"]')).toBeTruthy();
  });

  it('starts with the panel closed', () => {
    const panel = fixture.nativeElement.querySelector('.admin-submenu__panel');
    expect(panel.classList.contains('admin-submenu__panel--open')).toBeFalse();
  });

  it('opens the panel when the trigger is clicked, and pins it', () => {
    const trigger = fixture.nativeElement.querySelector('.admin-submenu__trigger');
    trigger.click();
    fixture.detectChanges();
    const panel = fixture.nativeElement.querySelector('.admin-submenu__panel');
    expect(panel.classList.contains('admin-submenu__panel--open')).toBeTrue();
  });

  it('closes the panel when the trigger is clicked twice', () => {
    const trigger = fixture.nativeElement.querySelector('.admin-submenu__trigger');
    trigger.click();
    trigger.click();
    fixture.detectChanges();
    const panel = fixture.nativeElement.querySelector('.admin-submenu__panel');
    expect(panel.classList.contains('admin-submenu__panel--open')).toBeFalse();
  });

  it('opens the panel on mouseenter (hover) without pinning', () => {
    const trigger = fixture.nativeElement.querySelector('.admin-submenu__trigger');
    trigger.dispatchEvent(new MouseEvent('mouseenter'));
    fixture.detectChanges();
    const panel = fixture.nativeElement.querySelector('.admin-submenu__panel');
    expect(panel.classList.contains('admin-submenu__panel--open')).toBeTrue();
  });

  it('sets aria-expanded on the trigger to reflect open state', () => {
    const trigger: HTMLButtonElement = fixture.nativeElement.querySelector(
      '.admin-submenu__trigger',
    );
    expect(trigger.getAttribute('aria-expanded')).toBe('false');
    trigger.click();
    fixture.detectChanges();
    expect(trigger.getAttribute('aria-expanded')).toBe('true');
  });
});

import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
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

  it('emits itemClicked when an enabled item is clicked', () => {
    const spy = jasmine.createSpy('itemClicked');
    fixture.componentInstance.itemClicked.subscribe(spy);
    const enabledLink: HTMLAnchorElement = fixture.nativeElement.querySelector(
      '.admin-submenu__item:nth-child(2) a',
    );
    enabledLink.click();
    expect(spy).toHaveBeenCalledTimes(1);
  });

  it('does NOT emit itemClicked when a disabled item is clicked', () => {
    const spy = jasmine.createSpy('itemClicked');
    fixture.componentInstance.itemClicked.subscribe(spy);
    const disabledSpan: HTMLSpanElement = fixture.nativeElement.querySelector(
      '.admin-submenu__item:nth-child(1) span[aria-disabled="true"]',
    );
    disabledSpan.click();
    expect(spy).not.toHaveBeenCalled();
  });

  it('clears pinned state when Escape is pressed', () => {
    fixture.componentInstance.pinned.set(true);
    fixture.detectChanges();
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    fixture.detectChanges();
    expect(fixture.componentInstance.pinned()).toBeFalse();
  });

  it('keeps the panel open while the cursor is over the panel itself', () => {
    const trigger = fixture.nativeElement.querySelector('.admin-submenu__trigger');
    const panel = fixture.nativeElement.querySelector('.admin-submenu__panel');
    trigger.dispatchEvent(new MouseEvent('mouseenter'));
    trigger.dispatchEvent(new MouseEvent('mouseleave'));
    panel.dispatchEvent(new MouseEvent('mouseenter'));
    fixture.detectChanges();
    expect(panel.classList.contains('admin-submenu__panel--open')).toBeTrue();
  });

  it('clears hovered after 150ms grace when mouse leaves trigger', fakeAsync(() => {
    const component = fixture.componentInstance;

    component.onTriggerEnter();
    fixture.detectChanges();
    expect(component.hovered()).toBeTrue();

    component.onTriggerLeave();
    // timer not yet elapsed
    tick(149);
    fixture.detectChanges();
    expect(component.hovered()).toBeTrue();  // still open

    tick(1);  // now at 150ms
    fixture.detectChanges();
    expect(component.hovered()).toBeFalse(); // now cleared
  }));
});

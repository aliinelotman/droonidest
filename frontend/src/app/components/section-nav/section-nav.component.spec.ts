import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { provideRouter } from '@angular/router';
import { By } from '@angular/platform-browser';
import { SectionNavComponent } from './section-nav.component';
import { AuthService, UserResponse } from '../../services/auth.service';

const mockAdminUser: UserResponse = {
  id: '1',
  email: 'admin@example.com',
  displayName: 'Admin User',
  avatarUrl: null,
  role: 'ADMIN',
};

const mockRegularUser: UserResponse = {
  id: '2',
  email: 'user@example.com',
  displayName: 'Regular User',
  avatarUrl: null,
  role: 'USER',
};

describe('SectionNavComponent', () => {
  let authService: jasmine.SpyObj<AuthService>;

  function createComponent(user: UserResponse | null) {
    authService = jasmine.createSpyObj('AuthService', ['loginWithGoogle', 'logout'], {
      currentUser: signal(user),
    });

    TestBed.configureTestingModule({
      imports: [SectionNavComponent],
      providers: [
        { provide: AuthService, useValue: authService },
        provideRouter([]),
      ],
    });

    const fixture = TestBed.createComponent(SectionNavComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('shows app-admin-submenu when role is ADMIN', () => {
    const fixture = createComponent(mockAdminUser);
    const submenu = fixture.debugElement.query(By.css('app-admin-submenu'));
    expect(submenu).not.toBeNull();
  });

  it('does not show app-admin-submenu when role is USER', () => {
    const fixture = createComponent(mockRegularUser);
    const submenu = fixture.debugElement.query(By.css('app-admin-submenu'));
    expect(submenu).toBeNull();
  });

  it('does not show app-admin-submenu when user is anonymous', () => {
    const fixture = createComponent(null);
    const submenu = fixture.debugElement.query(By.css('app-admin-submenu'));
    expect(submenu).toBeNull();
  });

  it('calls closeSidebar when AdminSubmenuComponent emits itemClicked', () => {
    const fixture = createComponent(mockAdminUser);
    const component = fixture.componentInstance;
    spyOn(component, 'closeSidebar');

    const submenu = fixture.debugElement.query(By.css('app-admin-submenu'));
    submenu.triggerEventHandler('itemClicked', null);

    expect(component.closeSidebar).toHaveBeenCalledTimes(1);
  });

  it('sets sidebarOpen to false after onAdminItemClick', () => {
    const fixture = createComponent(mockAdminUser);
    const component = fixture.componentInstance;
    component.sidebarOpen = true;

    component.onAdminItemClick();

    expect(component.sidebarOpen).toBeFalse();
  });
});

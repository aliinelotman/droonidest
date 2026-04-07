import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { provideRouter } from '@angular/router';
import { By } from '@angular/platform-browser';
import { HeaderComponent } from './header.component';
import { AuthService, UserResponse } from '../../services/auth.service';

const mockUser: UserResponse = {
  id: '1', email: 'test@example.com', displayName: 'Test User', avatarUrl: null, role: 'USER',
};

describe('HeaderComponent', () => {
  let authService: jasmine.SpyObj<AuthService>;

  function createComponent(user: UserResponse | null) {
    authService = jasmine.createSpyObj('AuthService', ['loginWithGoogle', 'logout'], {
      currentUser: signal(user),
    });

    TestBed.configureTestingModule({
      imports: [HeaderComponent],
      providers: [
        { provide: AuthService, useValue: authService },
        provideRouter([]),
      ],
    });

    const fixture = TestBed.createComponent(HeaderComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('should show login button when user is not authenticated', () => {
    const fixture = createComponent(null);
    const loginBtn = fixture.debugElement.query(By.css('[data-testid="login-btn"]'));
    const logoutBtn = fixture.debugElement.query(By.css('[data-testid="logout-btn"]'));

    expect(loginBtn).not.toBeNull();
    expect(logoutBtn).toBeNull();
  });

  it('should show display name and logout button when authenticated', () => {
    const fixture = createComponent(mockUser);
    const loginBtn = fixture.debugElement.query(By.css('[data-testid="login-btn"]'));
    const logoutBtn = fixture.debugElement.query(By.css('[data-testid="logout-btn"]'));

    expect(loginBtn).toBeNull();
    expect(logoutBtn).not.toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Test User');
  });

  it('should call loginWithGoogle when login button is clicked', () => {
    const fixture = createComponent(null);
    fixture.debugElement.query(By.css('[data-testid="login-btn"]')).nativeElement.click();

    expect(authService.loginWithGoogle).toHaveBeenCalledWith();
  });

  it('should call logout when logout button is clicked', () => {
    const fixture = createComponent(mockUser);
    fixture.debugElement.query(By.css('[data-testid="logout-btn"]')).nativeElement.click();

    expect(authService.logout).toHaveBeenCalled();
  });
});

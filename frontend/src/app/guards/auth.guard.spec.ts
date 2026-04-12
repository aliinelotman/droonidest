import { TestBed } from '@angular/core/testing';
import { RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { authGuard } from './auth.guard';
import { AuthService, UserResponse } from '../services/auth.service';

const mockUser: UserResponse = {
  id: '1', email: 'test@example.com', displayName: 'Test', avatarUrl: null, role: 'USER',
};

describe('authGuard', () => {
  let authService: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    authService = jasmine.createSpyObj('AuthService', [], {
      currentUser: signal<UserResponse | null>(null),
    });

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authService },
        provideRouter([]),
      ],
    });
  });

  it('should return true when user is authenticated', () => {
    Object.defineProperty(authService, 'currentUser', {
      get: () => signal(mockUser),
    });

    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as any, {} as RouterStateSnapshot)
    );

    expect(result).toBeTrue();
  });

  it('should redirect to "/" when unauthenticated', () => {
    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as any, {} as RouterStateSnapshot)
    );

    expect(result).toBeInstanceOf(UrlTree);
    expect((result as UrlTree).toString()).toBe('/');
  });

  it('should not call loginWithGoogle when unauthenticated', () => {
    TestBed.runInInjectionContext(() =>
      authGuard({} as any, {} as RouterStateSnapshot)
    );

    expect(authService.loginWithGoogle).toBeUndefined();
  });
});

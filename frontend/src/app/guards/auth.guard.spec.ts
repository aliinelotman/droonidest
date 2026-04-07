import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot, UrlSegment, provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { authGuard } from './auth.guard';
import { AuthService, UserResponse } from '../services/auth.service';

const mockUser: UserResponse = {
  id: '1', email: 'test@example.com', displayName: 'Test', avatarUrl: null, role: 'USER',
};

describe('authGuard', () => {
  let authService: jasmine.SpyObj<AuthService>;
  let route: ActivatedRouteSnapshot;

  beforeEach(() => {
    authService = jasmine.createSpyObj('AuthService', ['loginWithGoogle'], {
      currentUser: signal<UserResponse | null>(null),
    });

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authService },
        provideRouter([]),
      ],
    });

    route = { url: [{ path: 'module-two' } as UrlSegment] } as ActivatedRouteSnapshot;
  });

  it('should return true when user is authenticated', () => {
    Object.defineProperty(authService, 'currentUser', {
      get: () => signal(mockUser),
    });

    const result = TestBed.runInInjectionContext(() =>
      authGuard(route, {} as RouterStateSnapshot)
    );

    expect(result).toBeTrue();
  });

  it('should return false and call loginWithGoogle when unauthenticated', () => {
    const result = TestBed.runInInjectionContext(() =>
      authGuard(route, {} as RouterStateSnapshot)
    );

    expect(result).toBeFalse();
    expect(authService.loginWithGoogle).toHaveBeenCalledWith({
      redirectTo: '/module-two',
    });
  });
});

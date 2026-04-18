import { TestBed } from '@angular/core/testing';
import { RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { signal, WritableSignal } from '@angular/core';
import { adminGuard } from './admin.guard';
import { AuthService, UserResponse } from '../services/auth.service';

function makeUser(role: 'USER' | 'ADMIN'): UserResponse {
  return {
    id: '11111111-1111-1111-1111-111111111111',
    email: 't@example.com',
    displayName: 'T',
    avatarUrl: null,
    role,
  };
}

describe('adminGuard', () => {
  let authService: jasmine.SpyObj<AuthService>;
  let currentUserSignal: WritableSignal<UserResponse | null>;

  beforeEach(() => {
    currentUserSignal = signal<UserResponse | null>(null);

    authService = jasmine.createSpyObj('AuthService', [], {
      currentUser: currentUserSignal.asReadonly(),
    });

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authService },
        provideRouter([]),
      ],
    });
  });

  function run() {
    return TestBed.runInInjectionContext(() =>
      adminGuard({} as any, {} as RouterStateSnapshot)
    );
  }

  it('returns true when current user is ADMIN', () => {
    currentUserSignal.set(makeUser('ADMIN'));
    expect(run()).toBeTrue();
  });

  it('redirects to / when current user is USER', () => {
    currentUserSignal.set(makeUser('USER'));
    const result = run();
    expect(result).toBeInstanceOf(UrlTree);
    expect((result as UrlTree).toString()).toBe('/');
  });

  it('redirects to / when no user is logged in', () => {
    const result = run();
    expect(result).toBeInstanceOf(UrlTree);
    expect((result as UrlTree).toString()).toBe('/');
  });
});

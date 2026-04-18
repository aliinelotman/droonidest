import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { signal } from '@angular/core';
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
  let routerSpy: jasmine.SpyObj<Router>;
  let authStub: { currentUser: () => UserResponse | null };

  beforeEach(() => {
    routerSpy = jasmine.createSpyObj<Router>('Router', ['parseUrl']);
    routerSpy.parseUrl.and.callFake((url: string) => ({ toString: () => url } as any));
    authStub = { currentUser: () => null };

    TestBed.configureTestingModule({
      providers: [
        { provide: Router, useValue: routerSpy },
        { provide: AuthService, useValue: authStub },
      ],
    });
  });

  function run(): boolean | ReturnType<Router['parseUrl']> {
    return TestBed.runInInjectionContext(() =>
      adminGuard({} as any, {} as any)
    ) as boolean | ReturnType<Router['parseUrl']>;
  }

  it('returns true when current user is ADMIN', () => {
    authStub.currentUser = () => makeUser('ADMIN');
    expect(run()).toBe(true);
  });

  it('redirects to / when current user is USER', () => {
    authStub.currentUser = () => makeUser('USER');
    const result = run();
    expect(result).not.toBe(true);
    expect(routerSpy.parseUrl).toHaveBeenCalledWith('/');
  });

  it('redirects to / when no user is logged in', () => {
    authStub.currentUser = () => null;
    const result = run();
    expect(result).not.toBe(true);
    expect(routerSpy.parseUrl).toHaveBeenCalledWith('/');
  });
});

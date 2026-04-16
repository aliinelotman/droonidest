import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AuthService, UserResponse } from './auth.service';
import { environment } from '../../environments/environment';

const REFRESH_URL = `${environment.authApi}/refresh`;
const AUTHORIZE_URL = `${environment.authApi}/google/authorize-url`;
const GOOGLE_AUTH_URL = `${environment.authApi}/google`;
const LOGOUT_URL = `${environment.authApi}/logout`;

const mockUser: UserResponse = {
  id: '11111111-1111-1111-1111-111111111111',
  email: 'test@example.com',
  displayName: 'Test User',
  avatarUrl: null,
  role: 'USER',
};

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  describe('initAuth', () => {
    it('should set currentUser and accessToken on successful refresh', async () => {
      const promise = service.initAuth();

      const req = httpMock.expectOne(REFRESH_URL);
      expect(req.request.method).toBe('POST');
      req.flush({ accessToken: 'token123', user: mockUser });

      await promise;

      expect(service.currentUser()).toEqual(mockUser);
      expect(service.getAccessToken()).toBe('token123');
    });

    it('should leave currentUser null when server returns 204 (no cookie)', async () => {
      const promise = service.initAuth();

      httpMock.expectOne(REFRESH_URL)
        .flush(null, { status: 204, statusText: 'No Content' });

      await promise;

      expect(service.currentUser()).toBeNull();
      expect(service.getAccessToken()).toBeNull();
    });

    it('should leave currentUser null when refresh fails', async () => {
      const promise = service.initAuth();

      httpMock.expectOne(REFRESH_URL)
        .flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

      await promise;

      expect(service.currentUser()).toBeNull();
      expect(service.getAccessToken()).toBeNull();
    });
  });

  describe('loginWithGoogle', () => {
    function fakePopup(): { location: { href: string }; close: jasmine.Spy } {
      return {
        location: { href: '' },
        close: jasmine.createSpy('close'),
      };
    }

    it('should open a blank popup synchronously', () => {
      spyOn(window, 'open').and.returnValue(fakePopup() as unknown as Window);

      service.loginWithGoogle();

      expect(window.open).toHaveBeenCalledWith(
        'about:blank',
        '_blank',
        jasmine.stringContaining('popup')
      );
      // Drain the authorize-url request so afterEach's httpMock.verify() passes.
      httpMock.expectOne(AUTHORIZE_URL)
        .flush({ url: 'https://accounts.google.com/o/oauth2/v2/auth?client_id=abc' });
    });

    it('should redirect the popup to the backend-supplied Google URL', () => {
      const popup = fakePopup();
      spyOn(window, 'open').and.returnValue(popup as unknown as Window);

      service.loginWithGoogle();

      const req = httpMock.expectOne(AUTHORIZE_URL);
      expect(req.request.method).toBe('GET');
      req.flush({ url: 'https://accounts.google.com/o/oauth2/v2/auth?client_id=abc' });

      expect(popup.location.href).toBe('https://accounts.google.com/o/oauth2/v2/auth?client_id=abc');
    });

    it('should close the popup if fetching the authorize URL fails', () => {
      const popup = fakePopup();
      spyOn(window, 'open').and.returnValue(popup as unknown as Window);

      service.loginWithGoogle();

      httpMock.expectOne(AUTHORIZE_URL)
        .flush('Server error', { status: 500, statusText: 'Server Error' });

      expect(popup.close).toHaveBeenCalled();
    });

    it('should not throw when popup is blocked', () => {
      spyOn(window, 'open').and.returnValue(null);

      expect(() => service.loginWithGoogle()).not.toThrow();
      // No authorize-url call is made when the popup is blocked.
      httpMock.expectNone(AUTHORIZE_URL);
    });

    it('should exchange code and set currentUser after postMessage', async () => {
      let capturedHandler: ((e: MessageEvent) => void) | undefined;
      spyOn(window, 'open').and.returnValue(fakePopup() as unknown as Window);
      spyOn(window, 'addEventListener').and.callFake(
        (type: string, handler: EventListenerOrEventListenerObject) => {
          if (type === 'message') capturedHandler = handler as (e: MessageEvent) => void;
        }
      );

      service.loginWithGoogle();

      httpMock.expectOne(AUTHORIZE_URL)
        .flush({ url: 'https://accounts.google.com/o/oauth2/v2/auth?client_id=abc' });

      expect(capturedHandler).toBeDefined();

      capturedHandler!(new MessageEvent('message', {
        data: { code: 'oauth-code-abc', state: 'state-token-xyz' },
        origin: window.location.origin,
      }));

      const req = httpMock.expectOne(GOOGLE_AUTH_URL);
      expect(req.request.body).toEqual({ code: 'oauth-code-abc', state: 'state-token-xyz' });
      req.flush({ accessToken: 'new-token', user: mockUser });

      await Promise.resolve();

      expect(service.currentUser()).toEqual(mockUser);
      expect(service.getAccessToken()).toBe('new-token');
    });

    it('should ignore postMessages from other origins', () => {
      let capturedHandler: ((e: MessageEvent) => void) | undefined;
      spyOn(window, 'open').and.returnValue(fakePopup() as unknown as Window);
      spyOn(window, 'addEventListener').and.callFake(
        (type: string, handler: EventListenerOrEventListenerObject) => {
          if (type === 'message') capturedHandler = handler as (e: MessageEvent) => void;
        }
      );

      service.loginWithGoogle();

      httpMock.expectOne(AUTHORIZE_URL)
        .flush({ url: 'https://accounts.google.com/o/oauth2/v2/auth?client_id=abc' });

      capturedHandler!(new MessageEvent('message', {
        data: { code: 'oauth-code-abc', state: 'state-token-xyz' },
        origin: 'https://evil.com',
      }));

      httpMock.expectNone(GOOGLE_AUTH_URL);
    });
  });

  describe('logout', () => {
    let reloadSpy: jasmine.Spy;

    beforeEach(() => {
      reloadSpy = spyOn(service as any, 'reloadToRoot');
    });

    it('should clear currentUser and accessToken immediately', () => {
      (service as any).accessToken = 'some-token';
      (service as any).currentUserSignal.set(mockUser);

      service.logout();

      expect(service.currentUser()).toBeNull();
      expect(service.getAccessToken()).toBeNull();
      httpMock.expectOne(LOGOUT_URL);
    });

    it('should call logout endpoint', () => {
      service.logout();

      const req = httpMock.expectOne(LOGOUT_URL);
      expect(req.request.method).toBe('POST');
    });

    it('should reload to "/" after the server clears the refresh cookie', () => {
      service.logout();

      httpMock.expectOne(LOGOUT_URL).flush(null);

      expect(reloadSpy).toHaveBeenCalled();
    });

    it('should still reload to "/" if the logout request fails', () => {
      service.logout();

      httpMock.expectOne(LOGOUT_URL)
        .flush('Server error', { status: 500, statusText: 'Server Error' });

      expect(reloadSpy).toHaveBeenCalled();
    });
  });

  describe('getAccessToken', () => {
    it('should return null when no token is stored', () => {
      expect(service.getAccessToken()).toBeNull();
    });
  });
});

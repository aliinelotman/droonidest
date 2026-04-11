import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { AuthCallbackComponent } from './auth-callback.component';

describe('AuthCallbackComponent', () => {
  let closeSpy: jasmine.Spy;

  beforeEach(() => {
    closeSpy = spyOn(window, 'close');
  });

  function createComponent(params: { code?: string | null; error?: string | null }) {
    TestBed.configureTestingModule({
      imports: [AuthCallbackComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            queryParamMap: of({
              get: (key: string) => {
                if (key === 'code') return params.code ?? null;
                if (key === 'error') return params.error ?? null;
                return null;
              },
            }),
          },
        },
      ],
    });

    const fixture = TestBed.createComponent(AuthCallbackComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('should post code to opener and close when code is present', () => {
    const opener = jasmine.createSpyObj('opener', ['postMessage']);
    Object.defineProperty(window, 'opener', { value: opener, configurable: true });

    createComponent({ code: 'oauth-code-xyz' });

    expect(opener.postMessage).toHaveBeenCalledWith(
      { code: 'oauth-code-xyz' },
      window.location.origin
    );
    expect(closeSpy).toHaveBeenCalled();
  });

  it('should close without posting when Google returns an error', () => {
    const opener = jasmine.createSpyObj('opener', ['postMessage']);
    Object.defineProperty(window, 'opener', { value: opener, configurable: true });

    createComponent({ error: 'access_denied' });

    expect(opener.postMessage).not.toHaveBeenCalled();
    expect(closeSpy).toHaveBeenCalled();
  });

  it('should leave popup alone when neither code nor error is present', () => {
    const opener = jasmine.createSpyObj('opener', ['postMessage']);
    Object.defineProperty(window, 'opener', { value: opener, configurable: true });

    createComponent({});

    expect(opener.postMessage).not.toHaveBeenCalled();
    expect(closeSpy).not.toHaveBeenCalled();
  });
});

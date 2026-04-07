import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { AuthCallbackComponent } from './auth-callback.component';

describe('AuthCallbackComponent', () => {
  let closeSpy: jasmine.Spy;

  beforeEach(() => {
    closeSpy = spyOn(window, 'close');
  });

  function createComponent(code: string | null) {
    TestBed.configureTestingModule({
      imports: [AuthCallbackComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            queryParamMap: of({ get: (key: string) => (key === 'code' ? code : null) }),
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

    createComponent('oauth-code-xyz');

    expect(opener.postMessage).toHaveBeenCalledWith(
      { code: 'oauth-code-xyz' },
      window.location.origin
    );
    expect(closeSpy).toHaveBeenCalled();
  });

  it('should close without posting when code is missing', () => {
    const opener = jasmine.createSpyObj('opener', ['postMessage']);
    Object.defineProperty(window, 'opener', { value: opener, configurable: true });

    createComponent(null);

    expect(opener.postMessage).not.toHaveBeenCalled();
    expect(closeSpy).toHaveBeenCalled();
  });
});

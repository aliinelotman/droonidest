import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { take } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface UserResponse {
  id: string;
  email: string;
  displayName: string;
  avatarUrl: string | null;
  role: 'USER' | 'ADMIN';
}

interface AuthTokenResponse {
  accessToken: string;
  user: UserResponse;
}

export interface LoginOptions {
  redirectTo?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private accessToken: string | null = null;
  private isLoggingOut = false;
  private readonly currentUserSignal = signal<UserResponse | null>(null);

  readonly currentUser = this.currentUserSignal.asReadonly();

  getAccessToken(): string | null {
    return this.accessToken;
  }

  async initAuth(): Promise<void> {
    try {
      const result = await firstValueFrom(
        this.http.post<AuthTokenResponse>(`${environment.authApi}/refresh`, {}, { observe: 'response' })
      );
      if (result.status === 200 && result.body) {
        this.accessToken = result.body.accessToken;
        this.currentUserSignal.set(result.body.user);
      }
      // 204 = no refresh token cookie → user stays unauthenticated
    } catch {
      // Invalid/expired token or network error — user stays unauthenticated
    }
  }

  loginWithGoogle(options?: LoginOptions): void {
    const width = 500;
    const height = 600;
    const left = window.screenX + (window.outerWidth - width) / 2;
    const top = window.screenY + (window.outerHeight - height) / 2;

    // Open the popup synchronously inside the click handler so browsers don't
    // block it. We'll set its location once the backend returns the Google URL.
    const popup = window.open(
      'about:blank',
      '_blank',
      `width=${width},height=${height},left=${left},top=${top},popup=true`
    );

    if (!popup) {
      console.warn('Popup blocked. Please allow popups for this site.');
      return;
    }

    const handler = (event: MessageEvent) => {
      if (event.origin !== window.location.origin) return;
      if (!event.data?.code || !event.data?.state) return;

      clearInterval(closeInterval);
      window.removeEventListener('message', handler);
      this.exchangeCode(event.data.code, event.data.state, options?.redirectTo);
    };

    // Clean up the message listener if the user closes the popup without logging in,
    // or after a 10-minute hard cap so the interval never runs indefinitely.
    const POPUP_TIMEOUT_MS = 10 * 60 * 1_000;
    const startedAt = Date.now();
    const closeInterval = setInterval(() => {
      if (popup.closed || Date.now() - startedAt > POPUP_TIMEOUT_MS) {
        clearInterval(closeInterval);
        window.removeEventListener('message', handler);
      }
    }, 500);

    window.addEventListener('message', handler);

    this.http.get<{ url: string }>(`${environment.authApi}/google/authorize-url`)
      .pipe(take(1))
      .subscribe({
        next: ({ url }) => {
          if (!url.startsWith('https://accounts.google.com/')) {
            console.error('Unexpected authorize URL origin, aborting login');
            clearInterval(closeInterval);
            window.removeEventListener('message', handler);
            popup.close();
            return;
          }
          popup.location.href = url;
        },
        error: (err) => {
          console.error('Failed to get Google authorize URL', err);
          clearInterval(closeInterval);
          window.removeEventListener('message', handler);
          popup.close();
        },
      });
  }

  logout(): void {
    if (this.isLoggingOut) return;
    this.isLoggingOut = true;

    this.accessToken = null;
    this.currentUserSignal.set(null);

    // Reload to '/' after clearing the refresh cookie on the server. This gives
    // us a clean slate regardless of which page the user was on and avoids
    // having to track stale state across components.
    this.http.post(`${environment.authApi}/logout`, {})
      .pipe(take(1))
      .subscribe({
        next: () => this.reloadToRoot(),
        error: () => this.reloadToRoot(),
      });
  }

  // Wrapped in a method so tests can spy on it (window.location.assign isn't
  // spyable in Jasmine because its descriptor isn't writable).
  protected reloadToRoot(): void {
    window.location.assign('/');
  }

  private exchangeCode(code: string, state: string, redirectTo?: string): void {
    this.http.post<AuthTokenResponse>(`${environment.authApi}/google`, { code, state })
      .pipe(take(1))
      .subscribe({
        next: ({ accessToken, user }) => {
          this.accessToken = accessToken;
          this.currentUserSignal.set(user);
          if (redirectTo) {
            this.router.navigateByUrl(redirectTo);
          }
        },
        error: (err) => {
          console.error('Google auth failed', err);
        },
      });
  }
}

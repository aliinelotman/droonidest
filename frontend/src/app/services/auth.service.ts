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
  private readonly apiUrl = environment.apiUrl;

  private accessToken: string | null = null;
  private readonly currentUserSignal = signal<UserResponse | null>(null);

  readonly currentUser = this.currentUserSignal.asReadonly();

  getAccessToken(): string | null {
    return this.accessToken;
  }

  async initAuth(): Promise<void> {
    try {
      const result = await firstValueFrom(
        this.http.post<AuthTokenResponse>(`${this.apiUrl}/api/v1/auth/refresh`, {})
      );
      this.accessToken = result.accessToken;
      this.currentUserSignal.set(result.user);
    } catch {
      // No valid refresh token — user stays unauthenticated
    }
  }

  loginWithGoogle(options?: LoginOptions): void {
    const popup = window.open(
      '/auth/callback',
      '_blank',
      'width=500,height=600,popup=true'
    );

    if (!popup) {
      console.warn('Popup blocked. Please allow popups for this site.');
      return;
    }

    const handler = (event: MessageEvent) => {
      if (event.origin !== window.location.origin) return;
      if (!event.data?.code) return;

      window.removeEventListener('message', handler);
      this.exchangeCode(event.data.code, options?.redirectTo);
    };

    window.addEventListener('message', handler);
  }

  logout(): void {
    this.accessToken = null;
    this.currentUserSignal.set(null);

    this.http.post(`${this.apiUrl}/api/v1/auth/logout`, {})
      .pipe(take(1))
      .subscribe();
  }

  private exchangeCode(code: string, redirectTo?: string): void {
    this.http.post<AuthTokenResponse>(`${this.apiUrl}/api/v1/auth/google`, { code })
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

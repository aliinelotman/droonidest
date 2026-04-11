import { Component, OnInit, inject, ChangeDetectionStrategy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { take } from 'rxjs/operators';

@Component({
  selector: 'app-auth-callback',
  standalone: true,
  templateUrl: './auth-callback.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuthCallbackComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);

  ngOnInit(): void {
    this.route.queryParamMap.pipe(take(1)).subscribe((params) => {
      const code = params.get('code');
      const error = params.get('error');

      // Only act when we're the OAuth return trip — either a code or an error
      // from Google. Without either, leave the popup alone (defensive guard).
      if (!code && !error) {
        return;
      }

      if (code && window.opener) {
        window.opener.postMessage({ code }, window.location.origin);
      }
      window.close();
    });
  }
}

# Admin Submenu & Sub-Routes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the single Admin nav link with a 5-item submenu (1 working, 4 stubs), and split `/admin` into a parent route with six children.

**Architecture:** New `AdminSubmenuComponent` rendered inside the existing `SectionNavComponent`. Existing `AdminPageComponent` is renamed/moved to `AdminContentPageComponent` at `/admin/content`. New `AdminLandingComponent` at `/admin` and reusable `AdminStubComponent` at `/admin/{users,analytics,payments,email}` (section name from `ActivatedRoute.data`). Submenu open state is a single `computed` over three signals (`pinned`, `hovered`, `routeIsAdmin`); CSS @media queries handle desktop flyout vs mobile inline rendering.

**Tech Stack:** Angular 19 standalone components, signals, Jasmine + Karma, RxJS for `Router.events`.

**Spec:** `docs/superpowers/specs/2026-04-19-admin-submenu-design.md`

---

## File Structure

**New files:**
- `frontend/src/app/components/admin-submenu/admin-submenu.component.ts`
- `frontend/src/app/components/admin-submenu/admin-submenu.component.html`
- `frontend/src/app/components/admin-submenu/admin-submenu.component.scss`
- `frontend/src/app/components/admin-submenu/admin-submenu.component.spec.ts`
- `frontend/src/app/pages/admin/landing/admin-landing.component.ts`
- `frontend/src/app/pages/admin/landing/admin-landing.component.html`
- `frontend/src/app/pages/admin/landing/admin-landing.component.scss`
- `frontend/src/app/pages/admin/landing/admin-landing.component.spec.ts`
- `frontend/src/app/pages/admin/stub/admin-stub.component.ts`
- `frontend/src/app/pages/admin/stub/admin-stub.component.html`
- `frontend/src/app/pages/admin/stub/admin-stub.component.scss`
- `frontend/src/app/pages/admin/stub/admin-stub.component.spec.ts`

**Renamed/moved:**
- `frontend/src/app/pages/admin/admin-page.component.{ts,html,scss}` → `frontend/src/app/pages/admin/content/admin-content-page.component.{ts,html,scss}` (class `AdminPageComponent` → `AdminContentPageComponent`, selector `app-admin-page` → `app-admin-content-page`)

**Modified:**
- `frontend/src/app/app.routes.ts` — replace single `/admin` route with parent + 6 children
- `frontend/src/app/components/section-nav/section-nav.component.ts` — import `AdminSubmenuComponent`, drop the unused router-link Admin item logic
- `frontend/src/app/components/section-nav/section-nav.component.html` — replace the inline admin `<li>` block with `<app-admin-submenu>`

---

## Task 1: Rename AdminPageComponent → AdminContentPageComponent

**Files:**
- Move: `frontend/src/app/pages/admin/admin-page.component.ts` → `frontend/src/app/pages/admin/content/admin-content-page.component.ts`
- Move: `frontend/src/app/pages/admin/admin-page.component.html` → `frontend/src/app/pages/admin/content/admin-content-page.component.html`
- Move: `frontend/src/app/pages/admin/admin-page.component.scss` → `frontend/src/app/pages/admin/content/admin-content-page.component.scss`
- Modify: `frontend/src/app/app.routes.ts`

- [ ] **Step 1: Create the new directory and move files**

```bash
mkdir -p frontend/src/app/pages/admin/content
git mv frontend/src/app/pages/admin/admin-page.component.ts   frontend/src/app/pages/admin/content/admin-content-page.component.ts
git mv frontend/src/app/pages/admin/admin-page.component.html frontend/src/app/pages/admin/content/admin-content-page.component.html
git mv frontend/src/app/pages/admin/admin-page.component.scss frontend/src/app/pages/admin/content/admin-content-page.component.scss
```

- [ ] **Step 2: Update class name, selector, template/style URLs, and relative imports**

Edit `frontend/src/app/pages/admin/content/admin-content-page.component.ts`. The current file is:

```ts
import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { NgIf } from '@angular/common';
import { AdminDashboardListComponent } from '../../components/admin/admin-dashboard-list/admin-dashboard-list.component';
import { StudyModulesListComponent } from '../../components/admin/study-modules-list/study-modules-list.component';
import { LessonEditorModalComponent } from '../../components/admin/lesson-editor-modal/lesson-editor-modal.component';
import { ModuleResponse } from '../../services/module.types';

@Component({
  selector: 'app-admin-page',
  standalone: true,
  imports: [NgIf, AdminDashboardListComponent, StudyModulesListComponent, LessonEditorModalComponent],
  templateUrl: './admin-page.component.html',
  styleUrl: './admin-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminPageComponent { ... }
```

Replace with (note: relative imports gain one extra `../` because the file moved one level deeper):

```ts
import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { NgIf } from '@angular/common';
import { AdminDashboardListComponent } from '../../../components/admin/admin-dashboard-list/admin-dashboard-list.component';
import { StudyModulesListComponent } from '../../../components/admin/study-modules-list/study-modules-list.component';
import { LessonEditorModalComponent } from '../../../components/admin/lesson-editor-modal/lesson-editor-modal.component';
import { ModuleResponse } from '../../../services/module.types';

@Component({
  selector: 'app-admin-content-page',
  standalone: true,
  imports: [NgIf, AdminDashboardListComponent, StudyModulesListComponent, LessonEditorModalComponent],
  templateUrl: './admin-content-page.component.html',
  styleUrl: './admin-content-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminContentPageComponent {
  readonly activeModule = signal<ModuleResponse | null>(null);

  onEditModule(module: ModuleResponse): void {
    this.activeModule.set(module);
  }

  onCloseModal(): void {
    this.activeModule.set(null);
  }
}
```

- [ ] **Step 3: Update the route to point at the renamed component**

In `frontend/src/app/app.routes.ts`, replace the existing `/admin` route block:

```ts
{
  path: 'admin',
  canActivate: [authGuard, adminGuard],
  loadComponent: () =>
    import('./pages/admin/admin-page.component').then(
      (m) => m.AdminPageComponent
    ),
},
```

with:

```ts
{
  path: 'admin',
  canActivate: [authGuard, adminGuard],
  loadComponent: () =>
    import('./pages/admin/content/admin-content-page.component').then(
      (m) => m.AdminContentPageComponent
    ),
},
```

(The route stays at `/admin` for now; Task 4 splits it into parent + children.)

- [ ] **Step 4: Build and run tests to verify nothing broke**

Run from `frontend/`:
```bash
npm run build
npm test -- --watch=false --browsers=ChromeHeadless
```
Expected: build succeeds, all existing tests pass.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/app/pages/admin/content frontend/src/app/app.routes.ts
git commit -m "refactor(DEV-31): rename AdminPageComponent to AdminContentPageComponent"
```

---

## Task 2: AdminLandingComponent

**Files:**
- Create: `frontend/src/app/pages/admin/landing/admin-landing.component.ts`
- Create: `frontend/src/app/pages/admin/landing/admin-landing.component.html`
- Create: `frontend/src/app/pages/admin/landing/admin-landing.component.scss`
- Test: `frontend/src/app/pages/admin/landing/admin-landing.component.spec.ts`

- [ ] **Step 1: Write the failing test**

Create `admin-landing.component.spec.ts`:

```ts
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminLandingComponent } from './admin-landing.component';

describe('AdminLandingComponent', () => {
  let fixture: ComponentFixture<AdminLandingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminLandingComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminLandingComponent);
    fixture.detectChanges();
  });

  it('renders the heading', () => {
    const heading = fixture.nativeElement.querySelector('h1');
    expect(heading).toBeTruthy();
    expect(heading.textContent).toContain('Admin Console');
  });

  it('renders the prompt to pick a section', () => {
    const text = fixture.nativeElement.textContent ?? '';
    expect(text).toContain('Select a section from the menu');
  });
});
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
npm test -- --watch=false --browsers=ChromeHeadless --include='**/admin-landing.component.spec.ts'
```
Expected: FAIL with module-not-found for `./admin-landing.component`.

- [ ] **Step 3: Implement the component**

Create `admin-landing.component.ts`:

```ts
import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-admin-landing',
  standalone: true,
  imports: [],
  templateUrl: './admin-landing.component.html',
  styleUrl: './admin-landing.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminLandingComponent {}
```

Create `admin-landing.component.html`:

```html
<section class="admin-landing">
  <h1 class="admin-landing__title">Admin Console</h1>
  <p class="admin-landing__prompt">Select a section from the menu to get started.</p>
</section>
```

Create `admin-landing.component.scss`:

```scss
.admin-landing {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 56px);
  padding: 32px;
  text-align: center;

  &__title {
    font-family: var(--font-display);
    font-size: clamp(1.6rem, 4vw, 2.4rem);
    color: var(--color-accent);
    margin: 0 0 16px;
  }

  &__prompt {
    font-size: 1.1rem;
    color: var(--color-text-muted);
    margin: 0;
    max-width: 480px;
  }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
npm test -- --watch=false --browsers=ChromeHeadless --include='**/admin-landing.component.spec.ts'
```
Expected: PASS (2/2).

- [ ] **Step 5: Commit**

```bash
git add frontend/src/app/pages/admin/landing
git commit -m "feat(DEV-31): add AdminLandingComponent for /admin"
```

---

## Task 3: AdminStubComponent

**Files:**
- Create: `frontend/src/app/pages/admin/stub/admin-stub.component.ts`
- Create: `frontend/src/app/pages/admin/stub/admin-stub.component.html`
- Create: `frontend/src/app/pages/admin/stub/admin-stub.component.scss`
- Test: `frontend/src/app/pages/admin/stub/admin-stub.component.spec.ts`

- [ ] **Step 1: Write the failing test**

Create `admin-stub.component.spec.ts`:

```ts
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { AdminStubComponent, AdminSection } from './admin-stub.component';

function configure(section: AdminSection) {
  return TestBed.configureTestingModule({
    imports: [AdminStubComponent],
    providers: [
      {
        provide: ActivatedRoute,
        useValue: { data: of({ section }) },
      },
    ],
  }).compileComponents();
}

describe('AdminStubComponent', () => {
  it('renders the User Management label for section=users', async () => {
    await configure('users');
    const fixture: ComponentFixture<AdminStubComponent> =
      TestBed.createComponent(AdminStubComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('User Management');
    expect(fixture.nativeElement.textContent).toContain('Coming soon');
  });

  it('renders the Analytics Dashboard label for section=analytics', async () => {
    await configure('analytics');
    const fixture: ComponentFixture<AdminStubComponent> =
      TestBed.createComponent(AdminStubComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Analytics Dashboard');
  });

  it('renders the Payment Processing label for section=payments', async () => {
    await configure('payments');
    const fixture: ComponentFixture<AdminStubComponent> =
      TestBed.createComponent(AdminStubComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Payment Processing');
  });

  it('renders the Email Notifications label for section=email', async () => {
    await configure('email');
    const fixture: ComponentFixture<AdminStubComponent> =
      TestBed.createComponent(AdminStubComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Email Notifications');
  });
});
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
npm test -- --watch=false --browsers=ChromeHeadless --include='**/admin-stub.component.spec.ts'
```
Expected: FAIL with module-not-found.

- [ ] **Step 3: Implement the component**

Create `admin-stub.component.ts`:

```ts
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { map, Observable } from 'rxjs';

export type AdminSection = 'users' | 'analytics' | 'payments' | 'email';

const SECTION_LABELS: Readonly<Record<AdminSection, string>> = {
  users: 'User Management',
  analytics: 'Analytics Dashboard',
  payments: 'Payment Processing',
  email: 'Email Notifications',
};

@Component({
  selector: 'app-admin-stub',
  standalone: true,
  imports: [AsyncPipe],
  templateUrl: './admin-stub.component.html',
  styleUrl: './admin-stub.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminStubComponent {
  private readonly route = inject(ActivatedRoute);

  readonly label$: Observable<string> = this.route.data.pipe(
    map((data) => SECTION_LABELS[data['section'] as AdminSection] ?? 'Admin'),
  );
}
```

Create `admin-stub.component.html`:

```html
<section class="admin-stub">
  <h1 class="admin-stub__title">{{ label$ | async }}</h1>
  <p class="admin-stub__body">Coming soon — this section is not yet available.</p>
</section>
```

Create `admin-stub.component.scss`:

```scss
.admin-stub {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 56px);
  padding: 32px;
  text-align: center;

  &__title {
    font-family: var(--font-display);
    font-size: clamp(1.6rem, 4vw, 2.4rem);
    color: var(--color-accent);
    margin: 0 0 16px;
  }

  &__body {
    font-size: 1.1rem;
    color: var(--color-text-muted);
    margin: 0;
    max-width: 480px;
  }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
npm test -- --watch=false --browsers=ChromeHeadless --include='**/admin-stub.component.spec.ts'
```
Expected: PASS (4/4).

- [ ] **Step 5: Commit**

```bash
git add frontend/src/app/pages/admin/stub
git commit -m "feat(DEV-31): add AdminStubComponent for placeholder admin sections"
```

---

## Task 4: Wire `/admin` parent route with six children

**Files:**
- Modify: `frontend/src/app/app.routes.ts`

- [ ] **Step 1: Replace the single `/admin` entry with a parent + children block**

Open `frontend/src/app/app.routes.ts`. The file currently has the block from Task 1 (single `/admin` → AdminContentPageComponent). Replace that whole block with:

```ts
{
  path: 'admin',
  canActivate: [authGuard, adminGuard],
  children: [
    {
      path: '',
      loadComponent: () =>
        import('./pages/admin/landing/admin-landing.component').then(
          (m) => m.AdminLandingComponent
        ),
    },
    {
      path: 'content',
      loadComponent: () =>
        import('./pages/admin/content/admin-content-page.component').then(
          (m) => m.AdminContentPageComponent
        ),
    },
    {
      path: 'users',
      data: { section: 'users' },
      loadComponent: () =>
        import('./pages/admin/stub/admin-stub.component').then(
          (m) => m.AdminStubComponent
        ),
    },
    {
      path: 'analytics',
      data: { section: 'analytics' },
      loadComponent: () =>
        import('./pages/admin/stub/admin-stub.component').then(
          (m) => m.AdminStubComponent
        ),
    },
    {
      path: 'payments',
      data: { section: 'payments' },
      loadComponent: () =>
        import('./pages/admin/stub/admin-stub.component').then(
          (m) => m.AdminStubComponent
        ),
    },
    {
      path: 'email',
      data: { section: 'email' },
      loadComponent: () =>
        import('./pages/admin/stub/admin-stub.component').then(
          (m) => m.AdminStubComponent
        ),
    },
  ],
},
```

Leave all other routes untouched.

- [ ] **Step 2: Build to verify routes compile**

```bash
cd frontend && npm run build
```
Expected: build succeeds.

- [ ] **Step 3: Run all tests**

```bash
npm test -- --watch=false --browsers=ChromeHeadless
```
Expected: all tests pass.

- [ ] **Step 4: Manual smoke check**

Start dev server (`npm start`), log in as ADMIN, then visit each URL directly in the browser address bar:
- `/admin` → landing ("Admin Console" + "Select a section…")
- `/admin/content` → existing dashboard + modules + lesson modal
- `/admin/users` → "User Management — Coming soon"
- `/admin/analytics` → "Analytics Dashboard — Coming soon"
- `/admin/payments` → "Payment Processing — Coming soon"
- `/admin/email` → "Email Notifications — Coming soon"

Also confirm: as a non-ADMIN user (or logged out), each of those URLs redirects to `/`.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/app/app.routes.ts
git commit -m "feat(DEV-31): split /admin into parent route with 6 children"
```

---

## Task 5: AdminSubmenuComponent — items, template, base styles

**Files:**
- Create: `frontend/src/app/components/admin-submenu/admin-submenu.component.ts`
- Create: `frontend/src/app/components/admin-submenu/admin-submenu.component.html`
- Create: `frontend/src/app/components/admin-submenu/admin-submenu.component.scss`
- Test: `frontend/src/app/components/admin-submenu/admin-submenu.component.spec.ts`

- [ ] **Step 1: Write the failing test**

Create `admin-submenu.component.spec.ts`:

```ts
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AdminSubmenuComponent } from './admin-submenu.component';

describe('AdminSubmenuComponent', () => {
  let fixture: ComponentFixture<AdminSubmenuComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminSubmenuComponent],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminSubmenuComponent);
    fixture.detectChanges();
  });

  it('renders the ADMIN trigger button', () => {
    const trigger = fixture.nativeElement.querySelector('.admin-submenu__trigger');
    expect(trigger).toBeTruthy();
    expect(trigger.textContent.toLowerCase()).toContain('admin');
  });

  it('renders all 5 menu items in order', () => {
    const items = fixture.nativeElement.querySelectorAll('.admin-submenu__item');
    expect(items.length).toBe(5);
    expect(items[0].textContent).toContain('User Management');
    expect(items[1].textContent).toContain('Content Management');
    expect(items[2].textContent).toContain('Analytics Dashboard');
    expect(items[3].textContent).toContain('Payment Processing');
    expect(items[4].textContent).toContain('Email Notifications');
  });

  it('renders enabled items as anchors and disabled items as spans', () => {
    const items = fixture.nativeElement.querySelectorAll('.admin-submenu__item');
    // index 1 = Content Management (enabled)
    expect(items[1].querySelector('a')).toBeTruthy();
    expect(items[1].querySelector('span[aria-disabled="true"]')).toBeFalsy();
    // index 0 = Users (disabled)
    expect(items[0].querySelector('a')).toBeFalsy();
    expect(items[0].querySelector('span[aria-disabled="true"]')).toBeTruthy();
  });
});
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
npm test -- --watch=false --browsers=ChromeHeadless --include='**/admin-submenu.component.spec.ts'
```
Expected: FAIL — module-not-found.

- [ ] **Step 3: Implement the component**

Create `admin-submenu.component.ts`:

```ts
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterModule } from '@angular/router';

interface AdminSubmenuItem {
  readonly label: string;
  readonly route: string;
  readonly enabled: boolean;
}

@Component({
  selector: 'app-admin-submenu',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './admin-submenu.component.html',
  styleUrl: './admin-submenu.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminSubmenuComponent {
  readonly items: ReadonlyArray<AdminSubmenuItem> = [
    { label: 'User Management',     route: '/admin/users',     enabled: false },
    { label: 'Content Management',  route: '/admin/content',   enabled: true  },
    { label: 'Analytics Dashboard', route: '/admin/analytics', enabled: false },
    { label: 'Payment Processing',  route: '/admin/payments',  enabled: false },
    { label: 'Email Notifications', route: '/admin/email',     enabled: false },
  ];
}
```

Create `admin-submenu.component.html`:

```html
<button
  type="button"
  class="admin-submenu__trigger"
  aria-haspopup="menu"
  aria-controls="admin-submenu-panel"
>
  Admin
  <span class="admin-submenu__chevron" aria-hidden="true">›</span>
</button>

<div
  id="admin-submenu-panel"
  class="admin-submenu__panel"
  role="menu"
  aria-label="Admin sections"
>
  <div class="admin-submenu__heading">Admin Options</div>
  <ul class="admin-submenu__list">
    @for (item of items; track item.route) {
      <li class="admin-submenu__item" role="none">
        @if (item.enabled) {
          <a
            class="admin-submenu__link"
            role="menuitem"
            [routerLink]="item.route"
            routerLinkActive="admin-submenu__link--active"
          >{{ item.label }}</a>
        } @else {
          <span
            class="admin-submenu__link admin-submenu__link--disabled"
            role="menuitem"
            aria-disabled="true"
          >{{ item.label }}</span>
        }
      </li>
    }
  </ul>
</div>
```

Create `admin-submenu.component.scss` (minimal — full responsive/open-state styling lands in Task 8):

```scss
:host {
  display: inline-block;
  position: relative;
}

.admin-submenu__trigger {
  font-family: var(--font-display);
  font-size: clamp(1.2rem, 4.5vh, 2.5rem);
  text-transform: uppercase;
  font-weight: 800;
  letter-spacing: 0.04em;
  color: var(--color-text-light);
  background: none;
  border: 2px solid transparent;
  border-radius: 6px;
  cursor: pointer;
  padding: 4px 12px;
}

.admin-submenu__chevron {
  margin-left: 8px;
  color: var(--color-accent);
}

.admin-submenu__panel {
  display: none; // Task 6 will toggle via [class.admin-submenu__panel--open]
  background: rgba(20, 32, 54, 0.95);
  border-radius: 12px;
  padding: 16px 20px;
  min-width: 240px;
}

.admin-submenu__heading {
  font-family: var(--font-display);
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: var(--color-accent);
  font-size: 0.9rem;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  margin-bottom: 12px;
}

.admin-submenu__list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.admin-submenu__item {
  margin: 2px 0;
}

.admin-submenu__link {
  display: block;
  padding: 10px 12px;
  color: var(--color-text-light);
  text-decoration: none;
  border-radius: 4px;
  cursor: pointer;
  border-left: 3px solid transparent;
  transition: background 150ms ease, color 150ms ease;

  &:hover {
    background: rgba(255, 255, 255, 0.04);
  }

  &--active {
    color: var(--color-accent);
    border-left-color: var(--color-accent);
    background: rgba(240, 165, 0, 0.08);
  }

  &--disabled {
    opacity: 0.4;
    cursor: not-allowed;

    &:hover {
      background: none;
    }
  }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
npm test -- --watch=false --browsers=ChromeHeadless --include='**/admin-submenu.component.spec.ts'
```
Expected: PASS (3/3).

- [ ] **Step 5: Commit**

```bash
git add frontend/src/app/components/admin-submenu
git commit -m "feat(DEV-31): scaffold AdminSubmenuComponent with items"
```

---

## Task 6: AdminSubmenuComponent — open state (pinned, hovered, route-driven)

**Files:**
- Modify: `frontend/src/app/components/admin-submenu/admin-submenu.component.ts`
- Modify: `frontend/src/app/components/admin-submenu/admin-submenu.component.html`
- Modify: `frontend/src/app/components/admin-submenu/admin-submenu.component.spec.ts`

- [ ] **Step 1: Add failing tests for open-state behavior**

Append to `admin-submenu.component.spec.ts` (inside the existing `describe`):

```ts
it('starts with the panel closed', () => {
  const panel = fixture.nativeElement.querySelector('.admin-submenu__panel');
  expect(panel.classList.contains('admin-submenu__panel--open')).toBeFalse();
});

it('opens the panel when the trigger is clicked, and pins it', () => {
  const trigger = fixture.nativeElement.querySelector('.admin-submenu__trigger');
  trigger.click();
  fixture.detectChanges();
  const panel = fixture.nativeElement.querySelector('.admin-submenu__panel');
  expect(panel.classList.contains('admin-submenu__panel--open')).toBeTrue();
});

it('closes the panel when the trigger is clicked twice', () => {
  const trigger = fixture.nativeElement.querySelector('.admin-submenu__trigger');
  trigger.click();
  trigger.click();
  fixture.detectChanges();
  const panel = fixture.nativeElement.querySelector('.admin-submenu__panel');
  expect(panel.classList.contains('admin-submenu__panel--open')).toBeFalse();
});

it('opens the panel on mouseenter (hover) without pinning', () => {
  const trigger = fixture.nativeElement.querySelector('.admin-submenu__trigger');
  trigger.dispatchEvent(new MouseEvent('mouseenter'));
  fixture.detectChanges();
  const panel = fixture.nativeElement.querySelector('.admin-submenu__panel');
  expect(panel.classList.contains('admin-submenu__panel--open')).toBeTrue();
});

it('sets aria-expanded on the trigger to reflect open state', () => {
  const trigger: HTMLButtonElement = fixture.nativeElement.querySelector(
    '.admin-submenu__trigger',
  );
  expect(trigger.getAttribute('aria-expanded')).toBe('false');
  trigger.click();
  fixture.detectChanges();
  expect(trigger.getAttribute('aria-expanded')).toBe('true');
});
```

- [ ] **Step 2: Run tests to verify they fail**

```bash
npm test -- --watch=false --browsers=ChromeHeadless --include='**/admin-submenu.component.spec.ts'
```
Expected: 5 new tests FAIL (panel never gets `--open` class, no `aria-expanded` reactivity).

- [ ] **Step 3: Implement open-state logic**

Replace the contents of `admin-submenu.component.ts` with:

```ts
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterModule } from '@angular/router';
import { filter } from 'rxjs/operators';

interface AdminSubmenuItem {
  readonly label: string;
  readonly route: string;
  readonly enabled: boolean;
}

@Component({
  selector: 'app-admin-submenu',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './admin-submenu.component.html',
  styleUrl: './admin-submenu.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminSubmenuComponent {
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly items: ReadonlyArray<AdminSubmenuItem> = [
    { label: 'User Management',     route: '/admin/users',     enabled: false },
    { label: 'Content Management',  route: '/admin/content',   enabled: true  },
    { label: 'Analytics Dashboard', route: '/admin/analytics', enabled: false },
    { label: 'Payment Processing',  route: '/admin/payments',  enabled: false },
    { label: 'Email Notifications', route: '/admin/email',     enabled: false },
  ];

  readonly pinned = signal(false);
  readonly hovered = signal(false);
  readonly routeIsAdminChild = signal(this.computeRouteIsAdminChild(this.router.url));

  readonly open = computed(
    () => this.pinned() || this.hovered() || this.routeIsAdminChild(),
  );

  constructor() {
    this.router.events
      .pipe(
        filter((e): e is NavigationEnd => e instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((event) => {
        this.routeIsAdminChild.set(this.computeRouteIsAdminChild(event.urlAfterRedirects));
      });
  }

  onTriggerClick(): void {
    this.pinned.update((value) => !value);
  }

  onTriggerEnter(): void {
    this.hovered.set(true);
  }

  onTriggerLeave(): void {
    this.hovered.set(false);
  }

  private computeRouteIsAdminChild(url: string): boolean {
    // True for /admin/<anything>, false for bare /admin and non-admin routes.
    return /^\/admin\/[^/]/.test(url);
  }
}
```

Replace the trigger and panel markup in `admin-submenu.component.html`:

```html
<button
  type="button"
  class="admin-submenu__trigger"
  [class.admin-submenu__trigger--open]="open()"
  [attr.aria-expanded]="open()"
  aria-haspopup="menu"
  aria-controls="admin-submenu-panel"
  (click)="onTriggerClick()"
  (mouseenter)="onTriggerEnter()"
  (mouseleave)="onTriggerLeave()"
>
  Admin
  <span class="admin-submenu__chevron" aria-hidden="true">›</span>
</button>

<div
  id="admin-submenu-panel"
  class="admin-submenu__panel"
  [class.admin-submenu__panel--open]="open()"
  role="menu"
  aria-label="Admin sections"
>
  <div class="admin-submenu__heading">Admin Options</div>
  <ul class="admin-submenu__list">
    @for (item of items; track item.route) {
      <li class="admin-submenu__item" role="none">
        @if (item.enabled) {
          <a
            class="admin-submenu__link"
            role="menuitem"
            [routerLink]="item.route"
            routerLinkActive="admin-submenu__link--active"
          >{{ item.label }}</a>
        } @else {
          <span
            class="admin-submenu__link admin-submenu__link--disabled"
            role="menuitem"
            aria-disabled="true"
          >{{ item.label }}</span>
        }
      </li>
    }
  </ul>
</div>
```

Update `admin-submenu.component.scss` — replace the line `display: none;` inside `.admin-submenu__panel` with:

```scss
display: none;

&--open {
  display: block;
}
```

And add an open-state border style on the trigger:

```scss
.admin-submenu__trigger--open {
  border-color: var(--color-accent);
  color: var(--color-accent);
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
npm test -- --watch=false --browsers=ChromeHeadless --include='**/admin-submenu.component.spec.ts'
```
Expected: PASS (8/8).

- [ ] **Step 5: Commit**

```bash
git add frontend/src/app/components/admin-submenu
git commit -m "feat(DEV-31): add open/close state to AdminSubmenuComponent"
```

---

## Task 7: AdminSubmenuComponent — itemClicked output, panel hover grace, escape-to-close

**Files:**
- Modify: `frontend/src/app/components/admin-submenu/admin-submenu.component.ts`
- Modify: `frontend/src/app/components/admin-submenu/admin-submenu.component.html`
- Modify: `frontend/src/app/components/admin-submenu/admin-submenu.component.spec.ts`

- [ ] **Step 1: Write failing tests**

Append to the `describe` block in `admin-submenu.component.spec.ts`:

```ts
it('emits itemClicked when an enabled item is clicked', () => {
  const spy = jasmine.createSpy('itemClicked');
  fixture.componentInstance.itemClicked.subscribe(spy);
  const enabledLink: HTMLAnchorElement = fixture.nativeElement.querySelector(
    '.admin-submenu__item:nth-child(2) a',
  );
  enabledLink.click();
  expect(spy).toHaveBeenCalledTimes(1);
});

it('does NOT emit itemClicked when a disabled item is clicked', () => {
  const spy = jasmine.createSpy('itemClicked');
  fixture.componentInstance.itemClicked.subscribe(spy);
  const disabledSpan: HTMLSpanElement = fixture.nativeElement.querySelector(
    '.admin-submenu__item:nth-child(1) span[aria-disabled="true"]',
  );
  disabledSpan.click();
  expect(spy).not.toHaveBeenCalled();
});

it('clears pinned state when Escape is pressed', () => {
  fixture.componentInstance.pinned.set(true);
  fixture.detectChanges();
  document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
  fixture.detectChanges();
  expect(fixture.componentInstance.pinned()).toBeFalse();
});

it('keeps the panel open while the cursor is over the panel itself', () => {
  const trigger = fixture.nativeElement.querySelector('.admin-submenu__trigger');
  const panel = fixture.nativeElement.querySelector('.admin-submenu__panel');
  trigger.dispatchEvent(new MouseEvent('mouseenter'));
  trigger.dispatchEvent(new MouseEvent('mouseleave'));
  panel.dispatchEvent(new MouseEvent('mouseenter'));
  fixture.detectChanges();
  expect(panel.classList.contains('admin-submenu__panel--open')).toBeTrue();
});
```

- [ ] **Step 2: Run tests to verify they fail**

```bash
npm test -- --watch=false --browsers=ChromeHeadless --include='**/admin-submenu.component.spec.ts'
```
Expected: 4 new tests FAIL.

- [ ] **Step 3: Implement output, panel hover handlers (with 150ms grace), escape listener**

Add to the imports at the top of `admin-submenu.component.ts`:

```ts
import { EventEmitter, HostListener, Output } from '@angular/core';
```

Add a constant near the top of the file (above the `@Component` decorator):

```ts
const HOVER_GRACE_MS = 150;
```

Inside the `AdminSubmenuComponent` class, add a private timer field and the handlers:

```ts
private hoverGraceTimer: ReturnType<typeof setTimeout> | null = null;

@Output() readonly itemClicked = new EventEmitter<void>();

onItemClick(item: AdminSubmenuItem): void {
  if (!item.enabled) return;
  this.itemClicked.emit();
}

onTriggerEnter(): void {
  this.cancelHoverGrace();
  this.hovered.set(true);
}

onTriggerLeave(): void {
  this.scheduleHoverClear();
}

onPanelEnter(): void {
  this.cancelHoverGrace();
  this.hovered.set(true);
}

onPanelLeave(): void {
  this.scheduleHoverClear();
}

@HostListener('document:keydown.escape')
onEscape(): void {
  if (this.pinned()) {
    this.pinned.set(false);
  }
}

private scheduleHoverClear(): void {
  this.cancelHoverGrace();
  this.hoverGraceTimer = setTimeout(() => {
    this.hovered.set(false);
    this.hoverGraceTimer = null;
  }, HOVER_GRACE_MS);
}

private cancelHoverGrace(): void {
  if (this.hoverGraceTimer !== null) {
    clearTimeout(this.hoverGraceTimer);
    this.hoverGraceTimer = null;
  }
}
```

(Note: `onTriggerEnter` and `onTriggerLeave` already exist from Task 6 with simpler bodies — replace the bodies with the versions above so they share the grace-timer logic.)

Update `admin-submenu.component.html`. Add `(mouseenter)` / `(mouseleave)` to the panel and `(click)` to each menu item:

```html
<div
  id="admin-submenu-panel"
  class="admin-submenu__panel"
  [class.admin-submenu__panel--open]="open()"
  role="menu"
  aria-label="Admin sections"
  (mouseenter)="onPanelEnter()"
  (mouseleave)="onPanelLeave()"
>
  <div class="admin-submenu__heading">Admin Options</div>
  <ul class="admin-submenu__list">
    @for (item of items; track item.route) {
      <li class="admin-submenu__item" role="none">
        @if (item.enabled) {
          <a
            class="admin-submenu__link"
            role="menuitem"
            [routerLink]="item.route"
            routerLinkActive="admin-submenu__link--active"
            (click)="onItemClick(item)"
          >{{ item.label }}</a>
        } @else {
          <span
            class="admin-submenu__link admin-submenu__link--disabled"
            role="menuitem"
            aria-disabled="true"
            (click)="onItemClick(item)"
          >{{ item.label }}</span>
        }
      </li>
    }
  </ul>
</div>
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
npm test -- --watch=false --browsers=ChromeHeadless --include='**/admin-submenu.component.spec.ts'
```
Expected: PASS (12/12).

- [ ] **Step 5: Commit**

```bash
git add frontend/src/app/components/admin-submenu
git commit -m "feat(DEV-31): add itemClicked output, panel hover, escape-to-close"
```

---

## Task 8: AdminSubmenuComponent — responsive SCSS (desktop flyout, mobile inline)

**Files:**
- Modify: `frontend/src/app/components/admin-submenu/admin-submenu.component.scss`

- [ ] **Step 1: Replace the panel layout SCSS with responsive variants**

Open `frontend/src/app/components/admin-submenu/admin-submenu.component.scss`. Replace the `.admin-submenu__panel` block with:

```scss
.admin-submenu__panel {
  display: none;
  background: rgba(20, 32, 54, 0.95);
  border-radius: 12px;
  padding: 16px 20px;
  min-width: 240px;
  z-index: 310;

  // Desktop: float to the right of the trigger.
  @media (min-width: 768px) {
    position: absolute;
    top: 50%;
    left: calc(100% + 16px);
    transform: translateY(-50%);
  }

  // Mobile: stack inline below the trigger.
  @media (max-width: 767.98px) {
    position: relative;
    margin: 8px auto 0;
    width: min(85vw, 320px);
  }

  &--open {
    display: block;
  }
}
```

- [ ] **Step 2: Verify all submenu tests still pass**

```bash
cd frontend && npm test -- --watch=false --browsers=ChromeHeadless --include='**/admin-submenu.component.spec.ts'
```
Expected: PASS (12/12).

- [ ] **Step 3: Build to confirm SCSS compiles**

```bash
npm run build
```
Expected: build succeeds with no SCSS errors.

- [ ] **Step 4: Commit**

```bash
git add frontend/src/app/components/admin-submenu/admin-submenu.component.scss
git commit -m "feat(DEV-31): responsive layout for admin submenu (desktop right / mobile below)"
```

---

## Task 9: Wire AdminSubmenuComponent into SectionNavComponent

**Files:**
- Modify: `frontend/src/app/components/section-nav/section-nav.component.ts`
- Modify: `frontend/src/app/components/section-nav/section-nav.component.html`

- [ ] **Step 1: Import AdminSubmenuComponent in section-nav.component.ts**

Open `frontend/src/app/components/section-nav/section-nav.component.ts`. Add the import at the top:

```ts
import { AdminSubmenuComponent } from '../admin-submenu/admin-submenu.component';
```

Add `AdminSubmenuComponent` to the `@Component({ imports: [...] })` array (alongside `RouterModule`):

```ts
@Component({
  selector: 'app-section-nav',
  standalone: true,
  imports: [RouterModule, AdminSubmenuComponent],
  templateUrl: './section-nav.component.html',
  styleUrl: './section-nav.component.scss',
})
```

Add a method to handle item clicks from the submenu:

```ts
onAdminItemClick(): void {
  this.closeSidebar();
}
```

- [ ] **Step 2: Replace the inline admin link with the submenu component**

Open `frontend/src/app/components/section-nav/section-nav.component.html`. The current admin block (added in the prior iteration) is:

```html
@if (currentUser()?.role === 'ADMIN') {
  <li class="nav__item" [style.--i]="sections.length">
    <a
      class="nav__link"
      routerLink="/admin"
      (click)="closeSidebar()"
    >Admin</a>
  </li>
}
<li class="nav__item" [style.--i]="sections.length + (currentUser()?.role === 'ADMIN' ? 1 : 0)">
  @if (currentUser()) {
    <button class="nav__link" (click)="logout()">Logout</button>
  } @else {
    <button class="nav__link" (click)="login()">Login</button>
  }
</li>
@if (moduleLinks.length) {
  @for (link of moduleLinks; track link.route; let i = $index) {
    <li class="nav__item nav__item--module" [style.--i]="sections.length + (currentUser()?.role === 'ADMIN' ? 2 : 1) + i">
```

Replace it with:

```html
@if (currentUser()?.role === 'ADMIN') {
  <li class="nav__item" [style.--i]="sections.length">
    <app-admin-submenu (itemClicked)="onAdminItemClick()"></app-admin-submenu>
  </li>
}
<li class="nav__item" [style.--i]="sections.length + (currentUser()?.role === 'ADMIN' ? 1 : 0)">
  @if (currentUser()) {
    <button class="nav__link" (click)="logout()">Logout</button>
  } @else {
    <button class="nav__link" (click)="login()">Login</button>
  }
</li>
@if (moduleLinks.length) {
  @for (link of moduleLinks; track link.route; let i = $index) {
    <li class="nav__item nav__item--module" [style.--i]="sections.length + (currentUser()?.role === 'ADMIN' ? 2 : 1) + i">
```

- [ ] **Step 3: Build and run all tests**

```bash
cd frontend && npm run build
npm test -- --watch=false --browsers=ChromeHeadless
```
Expected: build succeeds; all tests pass.

- [ ] **Step 4: Commit**

```bash
git add frontend/src/app/components/section-nav
git commit -m "feat(DEV-31): render AdminSubmenuComponent in SectionNavComponent"
```

---

## Task 10: Manual end-to-end verification

**Files:** none (manual QA only)

- [ ] **Step 1: Start the stack**

```bash
docker compose up -d
cd frontend && npm start
```

Wait for Angular dev server to report "Compiled successfully" on `http://localhost:4200`.

- [ ] **Step 2: Verify nav visibility for non-admin**

In an incognito window, open `http://localhost:4200`, open the burger menu. Confirm: no "Admin" entry visible.

- [ ] **Step 3: Verify nav visibility for admin (desktop)**

Log in as the seeded ADMIN user. Open burger menu on a desktop-width viewport (≥768px).
- ADMIN entry visible with chevron `›`.
- Hover ADMIN → panel appears to the right with 5 items.
- Click ADMIN → panel pins (stays open after mouseleave).
- Click ADMIN again → panel closes.
- Click "Content Management" → navigates to `/admin/content`, sidebar closes.
- Click ADMIN again → panel re-opens.
- Click on greyed-out items (Users, Analytics, Payments, Email) → nothing happens, no navigation.

- [ ] **Step 4: Verify nav visibility for admin (mobile)**

Resize the browser to <768px (or use device emulation). Open burger menu.
- ADMIN entry visible.
- Tap ADMIN → submenu expands inline below ADMIN, pushing Logout/module links down.
- Tap "Content Management" → navigates and the whole sidebar closes.
- Tap ADMIN again → submenu opens; tap ADMIN again → submenu closes.

- [ ] **Step 5: Verify direct URL entry**

While logged in as ADMIN, type each URL into the address bar:
- `/admin` → "Admin Console" landing page.
- `/admin/content` → existing dashboard + 3 modules + lesson editor opens on edit.
- `/admin/users` → "User Management — Coming soon".
- `/admin/analytics` → "Analytics Dashboard — Coming soon".
- `/admin/payments` → "Payment Processing — Coming soon".
- `/admin/email` → "Email Notifications — Coming soon".

When on each `/admin/*` URL, open the burger menu — the submenu should be force-open and the matching item highlighted with the orange left-rail.

- [ ] **Step 6: Verify guards still apply**

Log out. Visit `/admin/users` directly → redirects to `/`.

- [ ] **Step 7: Run the full test suite one more time**

```bash
cd frontend && npm test -- --watch=false --browsers=ChromeHeadless && npm run build
```
Expected: all tests pass, build succeeds.

- [ ] **Step 8: No commit needed (verification only).** If any defect is found in steps 2–6, fix it in the appropriate task's component, add a regression test, and commit before proceeding.

---

## Out of Scope (deferred follow-ups)

- Building real pages for Users, Analytics, Payments, Email (each gets its own spec).
- A back button or breadcrumb inside the admin pages.
- Animating the panel open/close (currently a hard show/hide).
- Trapping keyboard focus inside the panel when pinned.

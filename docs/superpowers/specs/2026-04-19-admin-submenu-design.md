# Admin Submenu & Sub-Routes Design

**Date:** 2026-04-19
**Branch:** DEV-31
**Status:** Draft for review

## Goal

Replace the single "Admin" link in the main section nav with an interactive submenu that exposes five admin sections (Content, Users, Analytics, Payments, Email). Only Content Management is wired to a real page in this iteration; the other four are visible-but-disabled placeholders. The current `/admin` page is moved to `/admin/content` so URLs match the menu.

## Non-Goals

- Building User/Analytics/Payments/Email pages (separate specs later).
- Restructuring `AdminContentPageComponent`'s internals (only its location/name changes).
- Adding any backend endpoints — this is a frontend-only change.

## Visual Reference

Two mockups from the user:

1. **Default state** — ADMIN highlighted with orange border + chevron, panel to the right titled "ADMIN OPTIONS" listing the 5 sections.
2. **Active item** — Content Management highlighted with a vertical orange left-rail and orange text; other 4 items in normal weight.

## Routing

Replace the current single `/admin` route with a parent route that carries `authGuard + adminGuard` once, plus six children:

| Path | Component | Notes |
|---|---|---|
| `/admin` | `AdminLandingComponent` | "Select a section from the menu" landing |
| `/admin/content` | `AdminContentPageComponent` | Renamed from current `AdminPageComponent`. Same composition: dashboard list + modules list + lesson modal. |
| `/admin/users` | `AdminStubComponent` | Input: `section="users"` |
| `/admin/analytics` | `AdminStubComponent` | Input: `section="analytics"` |
| `/admin/payments` | `AdminStubComponent` | Input: `section="payments"` |
| `/admin/email` | `AdminStubComponent` | Input: `section="email"` |

Guards run at the parent level — children inherit, no per-child duplication.

## Components

### New: `AdminSubmenuComponent`

Located at `frontend/src/app/components/admin-submenu/`.

**Inputs:** none (reads current URL via `Router`).
**Outputs:** `itemClicked` — emitted when any clickable submenu item is activated, so `SectionNavComponent` can call `closeSidebar()` on mobile.

**Internal state (signals):**
- `pinned: WritableSignal<boolean>` — toggled by clicking the ADMIN trigger
- `hovered: WritableSignal<boolean>` — true while mouse is over ADMIN trigger or panel
- `routeIsAdmin: Signal<boolean>` — computed from `Router.events` / `RouterModule`, true when URL starts with `/admin/`

**Open computed:** `open = computed(() => pinned() || hovered() || routeIsAdmin())`.

**Items array** (constant):
```ts
readonly items: ReadonlyArray<{ label: string; route: string; enabled: boolean }> = [
  { label: 'User Management',     route: '/admin/users',     enabled: false },
  { label: 'Content Management',  route: '/admin/content',   enabled: true  },
  { label: 'Analytics Dashboard', route: '/admin/analytics', enabled: false },
  { label: 'Payment Processing',  route: '/admin/payments',  enabled: false },
  { label: 'Email Notifications', route: '/admin/email',     enabled: false },
];
```

**Rendering — desktop (≥768px):**
- Panel `position: absolute`, anchored right of the ADMIN nav item, vertically aligned with it.
- 150ms grace timer on `mouseleave` before clearing `hovered` (prevents flicker when moving cursor between trigger and panel).
- Enabled items render as `<a routerLink="…">`. Disabled items render as `<span aria-disabled="true">`, opacity `0.4`, `cursor: not-allowed`, no hover state.
- Active item (route match) shows orange left-rail + orange text.

**Rendering — mobile (<768px):**
- No hover. `hovered` stays false; only `pinned` and `routeIsAdmin` open the panel.
- Panel renders **inline below** the ADMIN trigger as a nested `<ul>`, indented. Pushes Logout/Module links down. Staggered reveal animation matches sibling nav items (reuse existing `--i` index pattern).
- Tapping any enabled item navigates and emits `itemClicked` → parent calls `closeSidebar()`.

**Viewport detection:** use a single `BreakpointObserver` (CDK) or a window resize signal — pick whichever is already used elsewhere in the codebase; do not introduce a second pattern.

### New: `AdminLandingComponent`

`pages/admin/landing/`. Renders a centered card: title "Admin Console", body "Select a section from the menu to get started." Optionally a row of 5 small icon tiles (one per section) as a visual hint — non-interactive (the menu is the navigation).

### New: `AdminStubComponent`

`pages/admin/stub/`.

**Section source:** reads `section: 'users' | 'analytics' | 'payments' | 'email'` from `ActivatedRoute.data` (configured per route in `app.routes.ts`). No `@Input` — it's only ever instantiated as a routed component.

**Behavior:** maps the section key to a label via a constant lookup, renders:
> **[Section Label]**
> Coming soon — this section is not yet available.

Uses the same admin SCSS tokens as the rest of the admin pages so it doesn't look out of place.

### Renamed: `AdminPageComponent` → `AdminContentPageComponent`

- File: `pages/admin/admin-page.component.{ts,html,scss}` → `pages/admin/content/admin-content-page.component.{ts,html,scss}`
- Class: `AdminPageComponent` → `AdminContentPageComponent`
- Selector: `app-admin-page` → `app-admin-content-page`
- Update the `loadComponent` import in `app.routes.ts` to point at the new path.
- No internal logic changes.

### Modified: `SectionNavComponent`

- Remove the inline `<li>` block added in the previous iteration that rendered the plain "Admin" `<a routerLink="/admin">`.
- Render `<app-admin-submenu>` in its place, only when `currentUser()?.role === 'ADMIN'`.
- Subscribe to its `itemClicked` output and call `closeSidebar()` on mobile.
- Add `AdminSubmenuComponent` to the standalone component's `imports` array.
- The `[style.--i]` index math for Logout / module links must account for whether the ADMIN row is rendered (same conditional offset pattern already in place).

### Modified: `app.routes.ts`

Replace the existing single `/admin` route with:

```ts
{
  path: 'admin',
  canActivate: [authGuard, adminGuard],
  children: [
    { path: '', loadComponent: () => import('./pages/admin/landing/admin-landing.component').then(m => m.AdminLandingComponent) },
    { path: 'content', loadComponent: () => import('./pages/admin/content/admin-content-page.component').then(m => m.AdminContentPageComponent) },
    { path: 'users',     loadComponent: () => import('./pages/admin/stub/admin-stub.component').then(m => m.AdminStubComponent), data: { section: 'users' } },
    { path: 'analytics', loadComponent: () => import('./pages/admin/stub/admin-stub.component').then(m => m.AdminStubComponent), data: { section: 'analytics' } },
    { path: 'payments',  loadComponent: () => import('./pages/admin/stub/admin-stub.component').then(m => m.AdminStubComponent), data: { section: 'payments' } },
    { path: 'email',     loadComponent: () => import('./pages/admin/stub/admin-stub.component').then(m => m.AdminStubComponent), data: { section: 'email' } },
  ],
}
```

The stub component reads its section from `ActivatedRoute.data` rather than taking it as a real `@Input` so it works as a routed component without a parent template.

## SCSS Tokens

Add to the existing admin tokens file (or create a small `_admin-submenu.scss` partial co-located with the component):

- Panel surface — reuse the existing admin card surface token.
- Panel border-radius — reuse existing card radius.
- Active item left-rail — `var(--color-accent)` (orange), 3px wide.
- Active item text — `var(--color-accent)`.
- Disabled item — opacity `0.4`, `cursor: not-allowed`, no hover background.
- Hover-grace timeout — `150ms` constant in the TS file (named `HOVER_GRACE_MS`).

## Accessibility

- ADMIN trigger is a `<button>` with `aria-haspopup="menu"`, `aria-expanded="{open()}"`, `aria-controls="admin-submenu-panel"`.
- Panel is `<ul role="menu">` with `id="admin-submenu-panel"`. Items are `role="menuitem"`.
- Disabled items have `aria-disabled="true"` and remain focusable so screen-reader users hear them.
- Escape key (when panel is pinned-open and focus is inside) clears `pinned` and returns focus to the ADMIN trigger.

## Testing

| Spec file | Coverage |
|---|---|
| `admin-submenu.component.spec.ts` | Open-state matrix (hover / pin / route), 150ms grace timer (use fakeAsync), disabled items don't navigate, active item gets active class, mobile inline rendering, `itemClicked` emits on enabled item only |
| `admin-stub.component.spec.ts` | Renders correct label for each of the 4 section keys, reads section from route data |
| `admin-landing.component.spec.ts` | Smoke render, no console errors |
| `section-nav.component.spec.ts` (existing) | Add: admin submenu shown only when role === 'ADMIN'; admin submenu hidden for USER and anonymous; `closeSidebar()` called when submenu emits `itemClicked` |
| `admin-content-page.component.spec.ts` (renamed from existing) | Update import paths and selector; otherwise unchanged |
| `app.routes.spec.ts` (if present) | All 6 child routes resolve; guards still run on parent |

All tests must pass (`npm test -- --watch=false`) and `ng build` must succeed before each commit.

## Acceptance Criteria

1. Logged-in ADMIN sees ADMIN entry in main nav with chevron icon.
2. Logged-in USER and anonymous visitors do **not** see ADMIN entry.
3. Desktop: hovering ADMIN reveals the panel; clicking pins it; clicking again unpins; navigating to any `/admin/*` route forces it open.
4. Mobile: tapping ADMIN expands inline list below; tapping a section navigates and closes the sidebar.
5. Content Management is the only clickable submenu item; the other 4 are greyed out and non-interactive.
6. `/admin` shows the landing page. `/admin/content` shows the existing dashboard + modules + lesson modal. Each stub route shows the "Coming soon" placeholder with the correct section label.
7. Active route (e.g. `/admin/content`) is highlighted in the submenu with the orange left-rail.
8. Direct URL entry (typing `/admin/users` in the address bar) works and shows the stub page; guards still apply.
9. All new and existing tests pass; production build succeeds.

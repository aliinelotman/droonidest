# Testing Strategy

## Purpose

This document defines how we test the backend and frontend against the MVP user stories in [PRODUCT.md](PRODUCT.md).

The strategy focuses on the stories that are in current scope:

- Authentication: `US-01` to `US-03`
- Module catalogue: `US-04`, `US-05`
- Lessons: `US-07` to `US-09`
- Progress tracking: `US-13`, `US-14`

Stories explicitly marked in `PRODUCT.md` as not part of the current MVP, especially exercises/quizzes and admin features, are covered here as future test scope only.

## Quality Goals

- Protect the main learning flow from login through lesson completion.
- Catch regressions close to the layer where they are introduced.
- Keep tests fast enough to run routinely in local development and CI.
- Verify both business rules and user-visible behavior.
- Prioritize coverage for published-content visibility, ordering, authentication, and progress display.

## Test Pyramid

### Backend

- Unit tests: service logic, JWT logic, mapping logic, validation rules.
- Slice/integration tests: controller tests, repository tests, security/filter behavior, Flyway-backed persistence behavior.

### Frontend

- Unit tests: standalone components, view-model logic, formatting, fallbacks, status rendering.
- Integration tests: page + service + router behavior with mocked HTTP responses.

## Story-Based Coverage

### 1. Authentication

Stories: `US-01`, `US-02`, `US-03`

Backend must verify:

- Google login creates a new `users` row with `google_id`, `email`, `display_name`, and `avatar_url`.
- Repeat login matches the existing user by `google_id` instead of creating duplicates.
- Inactive users cannot authenticate.
- Auth endpoints return the expected token/session payload.
- Logout invalidates the current session/token contract used by the frontend.

Frontend must verify:

- Logged-in state shows `display_name` and avatar image.
- If `avatar_url` is missing, initials are rendered instead.
- Logout clears local auth state and redirects to the home page.

Recommended tests:

- Backend unit: `AuthService`, JWT service, token validation edge cases.
- Backend controller: success, invalid token, inactive user, malformed request.
- Frontend component/service: auth state rendering, initials fallback, logout flow.

### 2. Module Catalogue

Stories: `US-04`, `US-05`

Backend must verify:

- Only `published` modules are returned to regular users.
- Modules are returned in `sort_order`.
- Module card payload includes `title`, `description`, and `thumbnail_url`.
- Placeholder-compatible responses are returned when thumbnail is absent.

Frontend must verify:

- Header/burger menu renders all available published modules in backend order.
- Module cards render title, description, and thumbnail or placeholder.
- Sticky header/navigation behavior remains present during scroll.

Recommended tests:

- Backend service/repository: filtering by `content_status`, ordering by `sort_order`.
- Backend controller: correct response shape for catalogue endpoints.
- Frontend component: module card rendering with and without thumbnail.
- Frontend integration: menu uses API data and preserves ordering.

### 3. Lessons

Stories: `US-07`, `US-08`, `US-09`

Backend must verify:

- Only published lessons are visible to regular users.
- Lessons are returned in `sort_order`.
- Lesson response contains content needed to render markdown and optional video.
- `video_url = NULL` omits video data cleanly.

Frontend must verify:

- Lesson list appears in correct order.
- Lesson content renders correctly from backend data.
- Embedded video player appears only when a video URL exists.
- No empty/broken video section is shown when video is absent.

Recommended tests:

- Backend service/repository: lesson visibility and ordering.
- Backend controller: module lesson list and lesson detail responses.
- Frontend page/component: ordered lesson navigation, markdown rendering, conditional video rendering.

### 4. Progress Tracking

Stories: `US-13`, `US-14`

Backend must verify:

- Progress values are stored and returned per user per lesson.
- Progress percentage stays within valid bounds and maps to the correct status.
- Lesson status values are one of `not_started`, `in_progress`, `completed`.
- Module-level progress calculations stay consistent with lesson-level statuses.

Frontend must verify:

- Progress bar reflects `progress_pct`.
- Status icon color/state matches `status`.
- Progress display updates after refresh or revisiting a module.

Recommended tests:

- Backend unit: progress calculation and status transition rules.
- Backend integration: persistence of `user_lesson_progress` records.
- Frontend component: progress bar width/value and status icon mapping.
- Frontend integration: API response updates page state correctly.

## Non-MVP Future Coverage

The following stories should not block MVP delivery but should be added to the test plan when implementation begins:

- Exercises and quizzes: `US-16` to `US-18`, `AS-18` to `AS-25`
- Admin user management: `AS-01` to `AS-03`
- Admin module/lesson/attachment management: `AS-04` to `AS-17`

When these features are implemented, add:

- Permission matrix tests for admin vs user access.
- Validation tests for content creation and editing.
- File upload/delete tests for lesson attachments.
- Analytics query correctness tests.

## Backend Strategy

### Scope

Backend tests should cover:

- Services as the primary place for business rules.
- Controllers for request validation, response shape, and status codes.
- Security components for JWT handling and protected route behavior.
- Repositories for ordering/filtering queries that drive user-facing stories.
- Database migrations to ensure the schema supports story expectations.

### Tooling

- `spring-boot-starter-test`
- `spring-security-test`
- H2 for test database integration where behavior is compatible with production usage
- Flyway migrations loaded in integration tests

### Minimum backend suites

- `AuthService` and `AuthController`
- `ModuleService` and `ModuleController`
- `LessonService`
- `UserService` where it contributes to authenticated user/profile behavior
- `JwtService` and `JwtAuthFilter`
- Repository tests for module and lesson ordering/filtering

### Backend test data rules

- Prefer factory/builders for `User`, `Module`, `Lesson`, and `UserLessonProgress`.
- Keep fixtures explicit about `content_status`, `sort_order`, `video_url`, and progress status.
- Use separate tests for `published` visibility vs ordering so failures are easy to diagnose.

## Frontend Strategy

### Scope

Frontend tests should cover:

- Header/auth UI state
- Burger navigation and module list rendering
- Module cards
- Lesson page rendering
- Progress indicators and status badges/icons
- Route guards and auth/session handling when implemented

### Tooling

- Angular TestBed
- Jasmine + Karma
- Component tests for standalone components
- Router and HTTP testing utilities for page-level integration tests

### Minimum frontend suites

- Header/auth presentation tests
- Module card tests
- Module page/lesson page tests
- Progress indicator tests
- Service tests for auth and module-loading flows

### Frontend test design rules

- Prefer testing rendered behavior over internal implementation details.
- Mock HTTP at the service boundary for component/page tests.
- Assert visible states from the user stories: ordering, fallbacks, conditional sections, and progress states.
- Keep CSS assertions limited to behavior-critical states such as sticky header presence and status-state classes.

## CI Expectations

- Backend unit and integration tests run on every pull request.
- Frontend unit and integration tests run on every pull request.
- New story implementations should add or update tests in the same branch.

## Coverage Priorities

If time is limited, test in this order:

1. Authentication and security boundaries
2. Published/unpublished visibility rules
3. Sort ordering for modules and lessons
4. Progress calculation and display
5. UI fallbacks such as missing avatar or thumbnail

## Definition of Done for Testing

A story is not done until:

- Relevant backend business rules are covered by automated tests.
- Relevant frontend user-visible behavior is covered by automated tests.
- Happy path and at least one meaningful failure or edge case are tested.
- Existing tests still pass.
- Any newly discovered regression gets a test before or with the fix.

# Product

> Features, user stories, acceptance criteria

---

## Features Overview

### Authentication
- Google OAuth login/logout
- Role-based access (user / admin)
- Soft-delete user deactivation

### Module Catalogue
- Browse published modules with thumbnails and descriptions
- Free preview modules visible without login
- Admin-controlled sort order

### Lessons
- Markdown content with optional embedded video
- Downloadable file attachments (PDF, images)

### Exercises (Will not be implemented)
- Optional per-lesson exercise (multiple choice, true/false)
- Post-answer explanations
- Unlimited retakes with full attempt history

### Progress Tracking
- Per-lesson progress percentage with auto-save
- Status tracking (not started / in progress / completed)
- Module-level completion derived from lesson statuses

---

## User Stories

### Tavakasutaja (User)

#### Registreerimine ja autentimine

| ID    | Story | Acceptance Criteria |
|-------|-------|---------------------|
| US-01 | Kasutajana soovin sisse logida oma Google kontoga, et ma ei peaks eraldi parooli meelde jätma. | Google OAuth redirect toimib; kasutaja rida luuakse `users` tabelis `google_id`, `email`, `display_name` ja `avatar_url` väärtustega; korduval sisselogimisel tuvastatakse olemasolev konto `google_id` järgi. |
| US-02 | Kasutajana soovin näha oma profiilipilti ja nime peale sisselogimist, et teaksin, et olen õigesse kontole sisse loginud. | `display_name` ja `avatar_url` kuvatakse päises; kui `avatar_url` on NULL, kuvatakse initsiaalid. |
| US-03 | Kasutajana soovin välja logida, et keegi teine minu seadmes minu kontot ei kasutaks. | Sessioon lõpetatakse; kasutaja suunatakse avalehele. |

#### Moodulite sirvimine

| ID    | Story | Acceptance Criteria |
|-------|-------|---------------------|
| US-04 | Kasutajana soovin näha kõiki avaldatud mooduleid kataloogis, et valida, mida õppida. | Kuvatakse ainult `is_published = TRUE` moodulid `sort_order` järjekorras. |
| US-05 | Kasutajana soovin näha iga mooduli lühikirjeldust ja pisipilti, et otsustada, kas teema mind huvitab. | Mooduli kaardil kuvatakse `title`, `description` ja `thumbnail_url` (või vaikimisi placeholder). |

#### Tundide läbimine

| ID    | Story | Acceptance Criteria |
|-------|-------|---------------------|
| US-07 | Kasutajana soovin näha mooduli tundide nimekirja õiges järjekorras, et teaksin, kust alustada ja mis järgmiseks tuleb. | Tunnid kuvatakse `sort_order` järgi; ainult `is_published = TRUE` tunnid on nähtavad. |
| US-08 | Kasutajana soovin lugeda tunni sisu. |
| US-09 | Kasutajana soovin vaadata tunni videot (kui see on olemas), et saaksin visuaalset õppematerjali. | Kui `video_url` ei ole NULL, kuvatakse manustatud videopleier; kui NULL, siis video sektsiooni ei kuvata. |

#### Edasijõudmine ja progress

| ID    | Story | Acceptance Criteria |
|-------|-------|---------------------|
| US-13 | Kasutajana soovin näha oma edasijõudmist protsentides iga tunni kohta, et teaksin, kui palju on veel jäänud. | `progress_pct` kuvatakse progressiriba kujul iga tunni kõrval. |
| US-14 | Kasutajana soovin näha, millised tunnid on alustamata / pooleli / läbitud, et saaksin kiire ülevaate oma progressist. | Iga tunni kõrval kuvatakse `status` ikoon: hall (not_started), kollane (in_progress), roheline (completed). |

#### Testid ja viktoriinid (Hetkel ei implementeeri)

| ID    | Story | Acceptance Criteria |
|-------|-------|---------------------|
| US-16 | Kasutajana soovin pärast tunni läbimist teha testi, et kontrollida oma teadmisi. | Testi nupp kuvatakse ainult siis, kui tunnile on seotud `exercises` rida (`lesson_id` kaudu). |
| US-17 | Kasutajana soovin näha mitme valikuga ja tõene/väär küsimusi, et test oleks mitmekesine. | Küsimused renderdatakse `question_type` järgi: `multiple_choice` = raadionupud `options` JSONB-st; `true_false` = kaks valikut. |
| US-18 | Kasutajana soovin pärast vastuse esitamist näha selgitust, miks õige vastus on õige, et ma sellest õpiksin. | Pärast esitamist kuvatakse iga küsimuse `explanation` (kui ei ole NULL). |

---

### Admin (Hetkel ei implementeeri MVP osana)

#### Kasutajate haldamine

| ID    | Story | Acceptance Criteria |
|-------|-------|---------------------|
| AS-01 | Adminina soovin näha kõigi registreeritud kasutajate nimekirja, et omada ülevaadet platvormi kasutajaskonnast. | Kuvatakse `display_name`, `email`, `role`, `is_active`, `created_at`; sorteeritav ja filtreeritav. |
| AS-02 | Adminina soovin deaktiveerida kasutaja konto, et blokeerida sobimatute kasutajate juurdepääs ilma nende andmeid kustutamata. | `is_active` seatakse FALSE-iks; kasutaja ei saa enam sisse logida; progress ja created_by viited jäävad alles. |
| AS-03 | Adminina soovin määrata teisele kasutajale admini rolli, et delegeerida sisu haldamist. | `role` muudetakse 'admin'-iks; kasutajale ilmub admin-paneel järgmisel sisselogimisel. |

#### Moodulite haldamine

| ID    | Story | Acceptance Criteria |
|-------|-------|---------------------|
| AS-04 | Adminina soovin luua uue mooduli pealkirja, kirjelduse ja pisipildiga, et struktureerida õppematerjali. | Uus `modules` rida luuakse; `slug` genereeritakse automaatselt `title`-st; `created_by` seatakse praeguse admini ID-ks. |
| AS-05 | Adminina soovin muuta mooduli järjekorda, et kontrollida kataloogis kuvamise järjekorda. | `sort_order` väärtust saab muuta drag-and-drop või numbri sisestamisega; muudatus kajastub koheselt kataloogis. |
| AS-06 | Adminina soovin avaldada või peita mooduli, et kontrollida, millal sisu kasutajatele nähtavaks muutub. | `is_published` toggle; FALSE = nähtav ainult adminidele; TRUE = nähtav kõigile. |
| AS-07 | Adminina soovin märkida mooduli tasuta eelvaateks, et külastajad saaksid sisu enne registreerimist proovida. | `is_free_preview` toggle; TRUE = moodul ja selle tasuta tunnid on kättesaadavad ilma sisselogimata. |
| AS-08 | Adminina soovin muuta mooduli pealkirja, kirjeldust ja slug-i, et hoida sisu ajakohasena. | Väljad on muudetavad; slug-i muutmine kontrollib unikaalsust; `updated_at` uuendatakse trigeri kaudu. |

#### Tundide haldamine

| ID    | Story | Acceptance Criteria |
|-------|-------|---------------------|
| AS-09 | Adminina soovin lisada moodulisse uue tunni Markdown-sisuga, et pakkuda õppematerjali. | Uus `lessons` rida luuakse seotud `module_id`-ga; Markdown-redaktor eelvaatega. |
| AS-10 | Adminina soovin lisada tunnile video URL-i, et rikastada õppematerjali visuaalsete materjalidega. | `video_url` väli on muudetav; eelvaade näitab manustatud videot. |
| AS-11 | Adminina soovin muuta tundide järjekorda mooduli sees, et kontrollida õppimise loogilist voogu. | `sort_order` muudetav drag-and-drop kaudu; muudatus kajastub koheselt kasutajavaates. |
| AS-12 | Adminina soovin avaldada üksikuid tunde iseseisvalt mooduli olekust, et saaksin tunde ükshaaval avaldada. | `is_published` toggle tunni tasemel; ei sõltu mooduli `is_published` väärtusest. |
| AS-13 | Adminina soovin märkida konkreetse tunni tasuta eelvaateks, et pakkuda granulaarset kontrolli tasuta sisu üle. | `is_free_preview` toggle tunni tasemel; sõltumatu mooduli lipust. |
| AS-14 | Adminina soovin määrata tunnile hinnangulise kestuse minutites, et kasutajad saaksid aega planeerida. | `estimated_minutes` väli on muudetav; aktsepteerib ainult positiivseid täisarve. |

#### Manuste haldamine

| ID    | Story | Acceptance Criteria |
|-------|-------|---------------------|
| AS-15 | Adminina soovin lisada tunnile failimanuseid (PDF, pilt), et pakkuda allalaaditavaid lisamaterjale. | Faili üleslaadimine loob uue `lesson_attachments` rea; `file_name`, `file_url`, `file_type`, `file_size_bytes` täidetakse automaatselt. |
| AS-16 | Adminina soovin näha manuse failitüüpi ja suurust, et hoida failid organiseerituna. | Manuste nimekiri kuvab `file_name`, `file_type` ikooni ja loetavat failisuurust. |
| AS-17 | Adminina soovin kustutada manuseid, et eemaldada aegunud materjale. | Kustutamine eemaldab `lesson_attachments` rea ja kuvab kinnitusdialoogi enne kustutamist. |

#### Testide haldamine

| ID    | Story | Acceptance Criteria |
|-------|-------|---------------------|
| AS-18 | Adminina soovin luua tunnile testi koos läbimise lävendi protsentiga, et testida kasutajate teadmisi. | Uus `exercises` rida luuakse `lesson_id` viitega (UNIQUE); `passing_score_pct` vaikimisi 70, muudetav vahemikus 1-100. |
| AS-19 | Adminina soovin lisada testile mitme valikuga küsimusi koos vastusevariantide, õige vastuse ja selgitusega. | Uus `exercise_questions` rida `question_type = 'multiple_choice'`; `options` JSONB valideeritakse; `correct_answer` peab vastama ühele `options` key-le. |
| AS-20 | Adminina soovin lisada tõene/väär küsimusi, et pakkuda kiireid teadmiste kontrolle. | `question_type = 'true_false'`; `options` genereeritakse automaatselt `[{"key":"true","text":"Tõene"},{"key":"false","text":"Väär"}]`; `correct_answer` on "true" või "false". |
| AS-21 | Adminina soovin muuta küsimuste järjekorda testis, et kontrollida küsimuste esitamise loogikat. | `sort_order` muudetav drag-and-drop kaudu. |
| AS-22 | Adminina soovin muuta testi läbimise lävendit, et kohandada raskustaset. | `passing_score_pct` muudetav; CHECK constraint (1-100) kehtib. |

#### Analüütika

| ID    | Story | Acceptance Criteria |
|-------|-------|---------------------|
| AS-23 | Adminina soovin näha, mitu kasutajat on iga mooduli/tunni läbinud, et hinnata sisu populaarsust. | Päring: `COUNT(*) FROM user_lesson_progress WHERE status = 'completed'` grupeerituna tunni/mooduli kaupa. |
| AS-24 | Adminina soovin näha testide keskmisi tulemusi, et tuvastada liiga raskeid/kergeid küsimusi. | Päring: `AVG(score_pct)` ja `COUNT(*)` grupeerituna `exercise_id` kaupa tabelis `user_exercise_attempts`. |
| AS-25 | Adminina soovin näha, mitu korda kasutajad teste uuesti sooritavad, et hinnata sisu kvaliteeti. | Päring: `COUNT(*) / COUNT(DISTINCT user_id)` grupeerituna `exercise_id` kaupa annab keskmise katsete arvu kasutaja kohta. |

---

## Database-to-Story Mapping

| Table | Stories |
|-------|---------|
| `users` | US-01, US-02, US-03, AS-01, AS-02, AS-03 |
| `modules` | US-04, US-05, US-06, US-15, AS-04 — AS-08 |
| `lessons` | US-07 — US-11, AS-09 — AS-14 |
| `lesson_attachments` | US-11, AS-15 — AS-17 |
| `exercises` | US-16, US-19, AS-18, AS-22 |
| `exercise_questions` | US-17, US-18, AS-19 — AS-21 |
| `user_lesson_progress` | US-12 — US-15, AS-23 |
| `user_exercise_attempts` | US-19 — US-21, AS-24, AS-25 |

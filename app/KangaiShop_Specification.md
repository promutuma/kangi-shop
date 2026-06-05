# KANGAI SHOP
## Milk Tracking, Payments & Debt Management — Android App
### Detailed Product Specification & Development Guide

**Platform:** Android (Kotlin) | **Scale:** 1–3 Users, Single Shop | **Architecture:** Offline-First with Cloud Sync  
**Prepared by Eelam Innovations**

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Key Concepts & Terminology](#2-key-concepts--terminology)
3. [System Architecture](#3-system-architecture)
4. [Database Schema](#4-database-schema)
5. [Feature Specifications](#5-feature-specifications)
6. [App Settings](#6-app-settings)
7. [UI/UX Design & Screen Definitions](#7-uiux-design--screen-definitions)
8. [Sync & Multi-User Architecture](#8-sync--multi-user-architecture)
9. [Development Phases](#9-development-phases)
10. [Gradle Dependencies](#10-gradle-dependencies)
11. [Testing Strategy](#11-testing-strategy)
12. [Android Development Skill (Claude Code)](#12-android-development-skill-claude-code)
13. [Future Enhancements (v2.0)](#13-future-enhancements-v20)

---

## 1. Project Overview

Kangai Shop is an Android application whose **sole focus is tracking milk deliveries, recording payments, and managing debts** — for both suppliers (people who deliver milk) and customers (people who take goods on credit). The same person can be both.

The app is designed for a small Kenyan shop with intermittent internet. It works fully offline and syncs across devices when connectivity is available.

> **Out of scope for v1:** Full inventory management, product catalogue, expense tracking. These are complex to maintain correctly and are not the shop's primary need. A simple "goods from shop" payment entry (description + amount) covers the supplier payment-in-goods case without requiring a full inventory system.

### 1.1 Core Objectives

- Track morning and evening milk deliveries per supplier
- Record all payments: cash, M-Pesa, or goods picked from the shop
- Track customer credit and repayments
- Unified ledger per person — one clear history of everything
- Identify people by phone number; pick from Android contacts
- Send SMS via device SIM (free, no API)
- Daily automated backup to Google Drive
- Support 1–3 users, synced via Firebase (free tier)

### 1.2 Application Identity

| Attribute | Value |
|---|---|
| App Name | Kangai Shop |
| Package Name | com.eeelaminnovations.kangaishop |
| Language | Kotlin |
| Min Android | Android 7.0 (API 24) |
| Target Android | Android 14 (API 34) |

### 1.3 Cost: Everything Free

| Service | Why it's free |
|---|---|
| Firebase Firestore | Free tier: 50,000 reads + 20,000 writes/day — far more than a small shop needs |
| Firebase Auth | Free tier: up to 10,000 verifications/month |
| SMS | Android `SmsManager` — uses device SIM airtime, no API or account needed |
| Google Drive backup | Uses owner's existing Google account, `drive.file` scope (only sees its own files) |
| App distribution | APK sideloaded — no Play Store fees |

---

## 2. Key Concepts & Terminology

### 2.1 Who Is a Person?

Everyone in the app is a **Person** — one profile, one phone number, one ledger. Their role is not set manually; it is determined automatically by what transactions exist for them:

| Role | How it's assigned | What it means |
|---|---|---|
| **Supplier** | A milk delivery has been recorded for them | They bring milk; shop owes them money |
| **Customer** | A credit transaction has been recorded for them | They owe the shop money |
| **Both** | Both types of transactions exist | Same person, single ledger showing all activity |
| **Contact only** | Person added but no transactions yet | Shows as a contact with no role badge yet |

> There are no checkboxes. The app figures out roles from the data. A person's role badges update automatically as transactions are recorded.

### 2.2 The Single Ledger

Every person has **one ledger** that shows all transactions in chronological order — milk deliveries, payments made to them, credit given to them, repayments from them. Each entry has a clear label and direction so it's easy to read at a glance.

```
KAMAU NJOROGE  —  Ledger
─────────────────────────────────────────
  Net balance:  Shop owes Kamau  KES 6,960
─────────────────────────────────────────
  4 Jun  🌇 Evening milk (18L)      +1,080  ← shop owes more
  4 Jun  🌅 Morning milk (22L)      +1,320
  3 Jun  💵 Cash paid to Kamau     -3,000   ← debt reduced
  3 Jun  🌇 Evening milk (22L)      +1,320
  2 Jun  🛒 Goods: 2kg Sugar         -240   ← goods offset
  2 Jun  🌅 Morning milk (20L)      +1,200
  1 Jun  📱 Kamau paid (credit)     -2,000  ← Kamau repays shop
  1 Jun  🛍 Credit: Unga 2kg         +300   ← shop gave him credit
```

The ledger uses positive (+) for amounts that increase what the shop owes a supplier, or increase what a customer owes the shop. Negative (−) for amounts that reduce the debt. The net balance line at the top summarises everything.

### 2.3 Debt Directions

| Situation | Net Balance Label |
|---|---|
| Shop owes person more than person owes shop | "Shop owes [Name] KES X" |
| Person owes shop more than shop owes them | "[Name] owes shop KES X" |
| Balanced | "Settled — KES 0" |

### 2.4 Milk Price

Prices are configured in App Settings with two independent tiers — morning and evening milk often differ in fat content and volume, so they are priced separately.

| Price setting | Where set | How it works |
|---|---|---|
| **Morning default price** | App Settings → Milk Pricing | Pre-filled on every morning delivery form |
| **Evening default price** | App Settings → Milk Pricing | Pre-filled on every evening delivery form |
| **Per-delivery override** | Delivery form (editable field) | Change the price for one entry only — does not affect the global default |

Examples:
- Global morning = KES 65/L, global evening = KES 60/L
- Kamau has a negotiated rate of KES 70/L → attendant edits the price field at the time of recording his delivery
- Changing the global price in Settings does not retroactively alter past delivery records

### 2.5 Payment Methods

**Shop paying a supplier (reducing what shop owes):**
- Cash
- M-Pesa (with reference)
- Goods from shop — quick entry: description + KES amount (no inventory system required)

**Customer paying the shop (reducing what customer owes):**
- Cash
- M-Pesa (with reference)

---

## 3. System Architecture

### 3.1 Offline-First

All writes go to local Room (SQLite) first. The app never waits for internet. A WorkManager background job pushes pending changes to Firestore when connectivity is available.

> **Sync status** always shown in the toolbar: ✅ Synced / 🔄 Pending / ☁✗ Offline

### 3.2 Tech Stack

| Layer | Technology | Notes |
|---|---|---|
| UI | Jetpack Compose + Material3 | Light + Dark theme |
| Architecture | MVVM + Clean Architecture | |
| Local DB | Room (SQLite) | Single source of truth |
| Cloud Sync | Firebase Firestore (free tier) | Multi-device sync |
| Auth | Firebase Authentication (free tier) | PIN login |
| DI | Hilt | |
| Async | Kotlin Coroutines + Flow | |
| Navigation | Jetpack Navigation Compose | |
| Settings | DataStore Preferences | Theme, global price, etc. |
| SMS | Android `SmsManager` (built-in) | No API needed |
| Contacts | Android `ContactsContract` | Pick phone numbers from contacts |
| Backup | Google Drive REST API | Nightly JSON backup |
| Charts | MPAndroidChart | Summary charts |

### 3.3 Package Structure

```
com.eeelaminnovations.kangaishop/
├── data/
│   ├── local/          — Room DB, entities, DAOs
│   ├── remote/         — Firestore repositories
│   └── workers/        — SyncWorker, BackupWorker
├── domain/
│   ├── model/          — Person, Delivery, Transaction
│   ├── usecase/        — Business logic
│   └── repository/     — Interfaces
├── ui/
│   ├── home/           — Dashboard
│   ├── milk/           — Milk recording screens
│   ├── people/         — Person profiles and ledgers
│   ├── reports/        — Summary reports
│   └── settings/       — App settings screens
└── utils/              — SMS sender, contacts picker, formatters
```

---

## 4. Database Schema

Every table includes base fields: `id` (UUID), `createdAt`, `lastModifiedAt`, `isDeleted`, `syncStatus`, `deviceId`.

### 4.1 App Users

| Column | Type | Description |
|---|---|---|
| id | String (UUID) | Primary key |
| name | String | Full name |
| phone | String | Phone number |
| pin | String (hashed) | 4-digit PIN |
| role | Enum | `OWNER` / `ATTENDANT` |
| isActive | Boolean | Active account |

### 4.2 People

One table for everyone. **No role flags** — roles are derived from transactions, not stored.

| Column | Type | Description |
|---|---|---|
| id | String (UUID) | Primary key |
| name | String | Full name |
| phone | String (unique) | Phone number — the primary identifier |
| smsEnabled | Boolean | Send SMS to this person |
| notes | String | Optional notes |

> **Roles (supplier / customer / both) are computed at runtime** by checking whether milk deliveries or credit transactions exist for this person. They are never stored as fields.

### 4.3 Milk Deliveries

Morning and evening are separate records — not optional columns.

| Column | Type | Description |
|---|---|---|
| id | String (UUID) | Primary key |
| personId | String (FK) | Reference to People |
| deliveryDate | Long | Date (epoch ms) |
| session | Enum | `MORNING` / `EVENING` |
| litres | Double | Litres delivered |
| pricePerLitre | Double | Price used for this delivery (defaults to global, editable) |
| totalValue | Double | Computed: litres × pricePerLitre |
| quality | Enum | `GOOD` / `REJECTED` / `PARTIAL` |
| rejectedLitres | Double | Litres rejected (if PARTIAL) |
| notes | String | Optional notes |
| recordedBy | String (FK) | App user |

### 4.4 Ledger Transactions

A single transactions table covers all money movement for all people.

| Column | Type | Description |
|---|---|---|
| id | String (UUID) | Primary key |
| personId | String (FK) | Reference to People |
| type | Enum | See transaction types below |
| direction | Enum | `CREDIT` (reduces net balance) / `DEBIT` (increases net balance) |
| amount | Double | KES value |
| milkDeliveryId | String (FK) | Linked delivery record (if type = MILK_DELIVERY) |
| goodsDescription | String | Description of goods (if type = GOODS_PAYMENT) |
| mpesaRef | String | M-Pesa reference (if applicable) |
| transactionDate | Long | Date (epoch ms) |
| runningBalance | Double | Net balance for this person after this entry |
| smsSent | Boolean | Whether SMS was sent |
| notes | String | Optional notes |
| recordedBy | String (FK) | App user |

**Transaction types:**

| Type | Direction | Who it applies to | Meaning |
|---|---|---|---|
| `MILK_DELIVERY` | DEBIT | Supplier | Milk delivered; shop owes more |
| `PAYMENT_CASH` | CREDIT | Supplier | Shop paid supplier in cash |
| `PAYMENT_MPESA` | CREDIT | Supplier | Shop paid supplier via M-Pesa |
| `PAYMENT_GOODS` | CREDIT | Supplier | Supplier took goods; shop debt reduced |
| `CREDIT_ISSUED` | DEBIT | Customer | Shop gave customer goods on credit |
| `CUSTOMER_PAYMENT_CASH` | CREDIT | Customer | Customer paid shop in cash |
| `CUSTOMER_PAYMENT_MPESA` | CREDIT | Customer | Customer paid shop via M-Pesa |

> The same transaction types work for a person who is both supplier and customer — the ledger shows all of them together with clear labels, and the net balance reflects everything.

### 4.5 App Settings (DataStore — not a DB table)

Stored as key-value pairs in Android DataStore:

| Key | Type | Default | Description |
|---|---|---|---|
| `global_milk_price_morning` | Double | 65.0 | Default price for morning milk (KES/L) |
| `global_milk_price_evening` | Double | 60.0 | Default price for evening milk (KES/L) |
| `shop_name` | String | "Kangai Shop" | Displayed in SMS and receipts |
| `shop_mpesa_number` | String | "" | Shop's M-Pesa till / number |
| `sms_enabled_global` | Boolean | true | Master SMS on/off switch |
| `debt_alert_threshold` | Double | 5000.0 | Alert when balance exceeds this (KES) |
| `customer_overdue_days` | Int | 7 | Days before a credit is flagged overdue |
| `backup_enabled` | Boolean | true | Auto backup on/off |
| `backup_time_hour` | Int | 2 | Hour for nightly backup (0–23) |
| `backup_google_account` | String | "" | Google account email for Drive backup |
| `theme` | Enum | `SYSTEM` | `LIGHT` / `DARK` / `SYSTEM` |
| `app_user_id` | String | "" | Currently logged-in app user |

### 4.6 SMS Log

| Column | Type | Description |
|---|---|---|
| id | String (UUID) | Primary key |
| recipientPhone | String | Destination number |
| recipientName | String | Person's name |
| message | String | Full SMS text sent |
| status | Enum | `SENT` / `FAILED` |
| sentAt | Long | Timestamp |
| relatedTransactionId | String (FK) | Transaction that triggered the SMS |
| errorMessage | String | Failure reason if failed |

### 4.7 Backup Log

| Column | Type | Description |
|---|---|---|
| id | String (UUID) | Primary key |
| backupDate | Long | Timestamp |
| status | Enum | `SUCCESS` / `FAILED` |
| driveFileId | String | Google Drive file ID |
| fileSizeBytes | Long | Backup file size |
| errorMessage | String | Failure reason if failed |

---

## 5. Feature Specifications

### 5.1 Authentication

| Permission | Owner | Attendant |
|---|:---:|:---:|
| Record milk deliveries | ✅ | ✅ |
| Record credit transactions | ✅ | ✅ |
| View today's summary | ✅ | ✅ |
| View full ledgers | ✅ | ❌ |
| View reports | ✅ | ❌ |
| Add / edit people | ✅ | ❌ |
| Record & adjust payments | ✅ | ❌ |
| Manage app settings | ✅ | ❌ |
| Export / print | ✅ | ❌ |

**Login:** User selects their profile → enters 4-digit PIN. Session lasts 8 hours or until manual logout.

---

### 5.2 People Management

#### Adding a Person

1. Tap **+ Add Person**
2. Enter name
3. Enter phone number — or tap 📞 to open Android contacts picker → select contact → number fills automatically
4. App checks for duplicates instantly: if the number already exists, *"This number belongs to [Name]. Update their profile instead?"*
5. SMS preference (on/off)
6. Optional notes
7. Save

No role selection. The person starts as a contact with no role. Their roles appear automatically once the first delivery or credit transaction is recorded.

#### Role Badges (auto-assigned)

| Badge | Condition |
|---|---|
| 🥛 Supplier | At least one `MILK_DELIVERY` transaction exists |
| 👥 Customer | At least one `CREDIT_ISSUED` transaction exists |
| 🥛👥 Both | Both types exist |
| *(no badge)* | No transactions yet |

---

### 5.3 Milk Tracking *(Primary Feature)*

#### Recording a Delivery

Morning and evening are recorded as separate actions. Both are available as quick-tap buttons on the Home screen and the Milk screen.

1. Tap **+ Morning Milk** or **+ Evening Milk**
2. Select or search for the supplier — or type their number / pick from contacts if new
3. Date defaults to today (can be changed)
4. Enter litres received
5. Price per litre shows the global default (editable for this entry only)
6. Total value computed live: litres × price
7. Quality: Good / Rejected / Partial (if partial, enter rejected litres)
8. Optional notes
9. Save → delivery record created, ledger transaction (`MILK_DELIVERY`) created, running balance updated

#### Daily Display per Supplier

```
Kamau Njoroge
  🌅 Morning:  22 L  ·  KES 1,320
  🌇 Evening:  18 L  ·  KES 1,080
  ────────────────────────────────
  Day total:   40 L  =  KES 2,400
```

If only one session is recorded, the other shows as *"not yet recorded"* — never zero.

---

### 5.4 Ledger & Payments *(Primary Feature)*

#### Viewing the Ledger

Tapping any person → their ledger. Shows:
- Net balance (prominent, at top)
- All transactions in reverse-chronological order
- Each entry: date, icon, label, amount (+ or −), running balance

#### Recording a Supplier Payment

1. Open person's ledger → **+ Record Payment**
2. Choose: **Cash**, **M-Pesa**, or **Goods from Shop**
3. For M-Pesa: enter reference code
4. For Goods from Shop: enter description + KES amount (one or more line items)
5. Live preview: *"New balance after payment: KES X"*
6. Save → `PAYMENT_CASH` / `PAYMENT_MPESA` / `PAYMENT_GOODS` transaction created, balance updated
7. SMS sent if enabled

#### Recording a Customer Credit

1. Open person's ledger → **+ Record Credit**
2. Enter description (what they took) + KES amount
3. App checks: does this push them over their credit limit? If yes → warning shown (owner can proceed)
4. Save → `CREDIT_ISSUED` transaction created, balance updated, SMS sent

#### Recording a Customer Payment

1. Open person's ledger → **+ Record Payment**
2. Choose: **Cash** or **M-Pesa**
3. Enter amount (+ M-Pesa reference if applicable)
4. Save → `CUSTOMER_PAYMENT_CASH` / `CUSTOMER_PAYMENT_MPESA` created, balance updated, SMS sent

---

### 5.5 SMS Notifications (Device SMS — Free)

Uses Android `SmsManager.sendTextMessage()` — no API, no account, no cost beyond SIM airtime. Works offline.

#### SMS Triggers & Templates

| Trigger | Template |
|---|---|
| Milk delivery recorded | *"Kangai Shop: {session} milk received — {litres}L @ KES {price}/L = KES {amount}. Balance: KES {balance}. {date}"* |
| Cash/M-Pesa payment to supplier | *"Kangai Shop: Payment of KES {amount} made. Balance: KES {balance}. {date}"* |
| Goods offset recorded | *"Kangai Shop: Goods KES {amount} deducted. Balance: KES {balance}. {date}"* |
| Credit issued to customer | *"Kangai Shop: Credit of KES {amount} recorded. You owe: KES {balance}. {date}"* |
| Customer payment received | *"Kangai Shop: Payment of KES {amount} received. You owe: KES {balance}. Thank you! {date}"* |

#### SMS Behaviour

- Sent immediately after saving the transaction
- Delivery receipt monitored via `PendingIntent`, result logged to SMS Log
- Failed SMS shown in Settings → SMS History with retry option
- Global on/off in Settings; per-person on/off on their profile

---

### 5.6 Google Drive Backup (Free)

- Nightly at 2:00 AM by default (configurable in Settings)
- Manual trigger: Settings → Backup → **Back Up Now**
- Exports all Room tables as a single structured JSON file
- Stored in Google Drive folder: **Kangai Shop Backups**
- Last 30 files retained; older deleted automatically
- Restore: Settings → Backup → Restore → pick file → confirm → imports into Room

**Backup filename:** `kangaishop_backup_YYYYMMDD_HHMMSS.json`

**Backup status shown in Settings:**
- ✅ Green — last backup < 24 hours ago
- 🟠 Orange — last backup 24–48 hours ago
- 🔴 Red — last backup > 48 hours ago or failed

---

### 5.7 Reports

| Report | Period | Access |
|---|---|---|
| Milk received — total + per supplier | Daily / Weekly / Monthly | Owner |
| Morning vs evening breakdown | Daily / Weekly / Monthly | Owner |
| Supplier reliability report | Weekly / Monthly | Owner |
| Supplier debt summary | On demand | Owner |
| Customer credit summary | On demand | Owner |
| All transactions for a person | On demand | Owner |
| SMS log | On demand | Owner |

All reports exportable as PDF or shareable via WhatsApp / Email.

#### Supplier Reliability Report

Tracks each supplier's consistency and volume trends over time. Helps the owner spot unreliable suppliers or seasonal patterns.

**Per-supplier metrics shown:**

| Metric | Description |
|---|---|
| Total litres this month | Sum of all morning + evening deliveries |
| Morning vs evening split | % of deliveries in each session |
| Average litres per delivery | Consistency indicator |
| Delivery frequency | Days delivered vs days expected in the period |
| Missed sessions | Count of expected sessions (AM or PM) where nothing was recorded |
| Price paid (average) | Average KES/L across all deliveries in the period |
| Volume trend | Month-on-month comparison (↑ up / ↓ down / → stable) |
| Reliability score | Simple score: deliveries made ÷ expected deliveries × 100% |

**Visualisation:**
- Bar chart: litres per day for selected period, split by morning (light blue) and evening (dark blue)
- Trend line across months
- Supplier comparison: side-by-side bar chart if multiple suppliers selected

**Example view for one supplier:**
```
Kamau Njoroge  —  June 2026
────────────────────────────────────────
Total:          1,240 L   (↑ 8% vs May)
Morning avg:    22 L/session
Evening avg:    19 L/session
Deliveries:     56 / 60 expected  (93% ✅)
Missed sessions: 4  (2 morning, 2 evening)
Avg price paid: KES 62.5 / L
────────────────────────────────────────
[Bar chart — daily litres, AM/PM split]
```

---

## 6. App Settings

Settings are accessible from the top menu (⋮) on any screen. Owner only.

### 6.1 Shop Info
- Shop name (used in SMS messages and report headers)
- M-Pesa number / till number (shown on receipts)

### 6.2 Milk Pricing
- **Morning default price (KES/L)** — pre-filled on every morning delivery form (e.g. KES 65/L)
- **Evening default price (KES/L)** — pre-filled on every evening delivery form (e.g. KES 60/L)
- Both are editable per delivery at record time — for suppliers with negotiated rates
- Changing these defaults does not retroactively affect past records

### 6.3 Appearance
- Theme: **Light** / **Dark** / **Follow System**
- Saved immediately to DataStore

### 6.4 SMS Notifications
- Master on/off toggle
- View SMS log (sent, failed, timestamps)
- Retry failed SMS

### 6.5 Alerts & Thresholds
- **Debt alert threshold (KES):** show red badge when a person's balance exceeds this amount (default: KES 5,000)
- **Customer overdue (days):** flag customers with no payment in X days (default: 7)

### 6.6 Google Drive Backup
- Google account connected (sign in / sign out)
- Backup schedule: daily at [time]
- **Back Up Now** button
- View backup history (date, file size, status)
- **Restore from Backup** — shows list of available Drive backups

### 6.7 App Users (Owner Only)
- View existing users
- Add new user (name, phone, role, PIN)
- Reset a user's PIN
- Deactivate a user

### 6.8 Sync
- Last sync timestamp
- **Force Sync Now** button
- Sync status detail (how many records pending)

### 6.9 About
- App version
- Built by Eelam Innovations

---

## 7. UI/UX Design & Screen Definitions

### 7.1 Design Philosophy

| Principle | In practice |
|---|---|
| **2-tap rule** | Morning Milk, Evening Milk, and Record Payment reachable in 2 taps from Home |
| **Plain language** | "Shop owes Kamau" not "Debit Balance". "Goods from shop" not "In-Kind Settlement" |
| **Big targets** | All rows and buttons minimum 56dp tall |
| **Instant feedback** | Every save shows a success banner with the key numbers |
| **Nothing hidden** | Alerts, missing evening entry, overdue customers all surface on Home |
| **Offline never blocks** | App always works; sync status is shown, not enforced |

---

### 7.2 Design System

#### Light Theme Colors

| Role | Hex | Usage |
|---|---|---|
| Primary | `#1A56A0` | Buttons, active nav, headers |
| Primary Container | `#D6E4F7` | Card accents, chips |
| Secondary | `#E07B00` | Badges, highlights |
| Background | `#F8F9FA` | Screen background |
| Surface | `#FFFFFF` | Cards, sheets |
| Surface Variant | `#EEF2F7` | Input fields, list stripes |
| Error | `#D32F2F` | Overdue, validation errors |
| Success | `#2E7D32` | Saved, synced |
| Warning | `#F57C00` | Pending sync, partial |
| Text | `#1A1A2E` | Body text |
| Outline | `#B0BEC5` | Borders |

#### Dark Theme Colors

| Role | Hex | Usage |
|---|---|---|
| Primary | `#90B8F8` | Buttons, active nav, headers |
| Primary Container | `#004880` | Card accents, chips |
| Secondary | `#FFB74D` | Badges, highlights |
| Background | `#121212` | Screen background |
| Surface | `#1E1E1E` | Cards, sheets |
| Surface Variant | `#2A2A2A` | Input fields, list stripes |
| Error | `#EF9A9A` | Overdue, validation errors |
| Success | `#81C784` | Saved, synced |
| Warning | `#FFB74D` | Pending sync, partial |
| Text | `#E1E1E1` | Body text |
| Outline | `#4A4A4A` | Borders |

#### Typography (Roboto)

| Style | Size | Weight | Used for |
|---|---|---|---|
| Headline Large | 28sp | Bold | Net balance figure |
| Headline Medium | 24sp | SemiBold | Screen totals, section headers |
| Title Large | 20sp | Medium | Toolbar titles |
| Title Medium | 16sp | SemiBold | Card titles, form labels |
| Body Large | 16sp | Regular | Notes, descriptions |
| Body Medium | 14sp | Regular | List subtitles, timestamps |
| Label Large | 14sp | Medium | Button text |
| Label Small | 11sp | Medium | Badges, status chips |

#### Component Shapes

| Component | Corner Radius |
|---|---|
| Cards | 12dp |
| Buttons (filled) | Pill |
| FAB | 16dp |
| Bottom sheets / Dialogs | 28dp top corners |
| Input fields | 8dp (outlined) |

---

### 7.3 Navigation

```
Bottom Navigation Bar
├── 🏠  Home
├── 🥛  Milk
├── 👥  People
└── 📊  Reports

Top AppBar (persistent)
├── Page title or back arrow  (left)
├── Sync status icon          (right, tappable)
├── Theme toggle 🌙           (right)
└── ⋮ Menu → Settings
```

Forms open as **bottom sheets** (for quick single-field entries) or **full-screen routes** (for multi-field forms like adding a person). No tiny pop-up dialogs.

---

### 7.4 Screen Definitions

---

#### SCREEN 01 — Login / Profile Select

```
┌────────────────────────────────────┐
│                                    │
│          KANGAI SHOP               │  ← Large, centered
│              🏪                    │
│                                    │
│    Who is using the app?           │
│                                    │
│   ┌─────────────┐ ┌─────────────┐ │
│   │   👤 Mama   │ │   👤 John   │ │  ← User profile cards
│   │    Owner    │ │  Attendant  │ │
│   └─────────────┘ └─────────────┘ │
│                                    │
└────────────────────────────────────┘
```

Tapping a profile → PIN sheet slides up from bottom:

```
┌────────────────────────────────────┐
│  Enter PIN — Mama                  │
│                                    │
│          ● ● ● ●                  │  ← Dots fill as typed
│                                    │
│    [ 1 ]    [ 2 ]    [ 3 ]         │
│    [ 4 ]    [ 5 ]    [ 6 ]         │  ← Large numpad, 56dp keys
│    [ 7 ]    [ 8 ]    [ 9 ]         │
│          [ ⌫ ]  [ 0 ]             │
└────────────────────────────────────┘
```

Wrong PIN → dots shake, *"Wrong PIN. Try again."*  
Correct PIN → navigate to Home.

---

#### SCREEN 02 — Home Dashboard

```
┌────────────────────────────────────┐
│ Kangai Shop          ✅☁  🌙  ⋮  │
├────────────────────────────────────┤
│  Good morning, Mama 👋             │
│  Thursday, 4 June 2026             │
│                                    │
│  ┌──────────────┬───────────────┐  │
│  │  🌅 Morning  │  🌇 Evening   │  │
│  │    62 L      │  not yet ⚠   │  │  ← Orange if past 5PM & not recorded
│  └──────────────┴───────────────┘  │
│                                    │
│  ┌──────────────┬───────────────┐  │
│  │ 💰 Owed out  │ 💳 Owed in   │  │
│  │ KES 16,600   │  KES 3,800   │  │
│  │ to suppliers │  from customers│ │
│  └──────────────┴───────────────┘  │
│                                    │
│  ⚠ Needs Attention                 │  ← Hidden when no alerts
│  • Kamau balance: KES 8,200 🔴     │
│  • Evening milk not yet recorded   │
│  • Wanjiru overdue 9 days 🔴       │
│                                    │
│  Quick Actions                     │
│  ┌──────────────┐ ┌─────────────┐  │
│  │ + Morning 🌅 │ │ + Evening 🌇│  │
│  └──────────────┘ └─────────────┘  │
│  ┌──────────────┐ ┌─────────────┐  │
│  │ + Payment    │ │ + Credit    │  │
│  └──────────────┘ └─────────────┘  │
└────────────────────────────────────┘
```

**Notes:** Tapping any summary card navigates to the relevant section. Alerts section disappears entirely when everything is clear.

---

#### SCREEN 03 — Milk Screen

```
┌────────────────────────────────────┐
│ ← Milk               ✅☁  ⋮     │
├────────────────────────────────────┤
│  Today: 118 L  ·  KES 7,080       │
│                                    │
│  [ Today ▼ ]  [ All Suppliers ▼ ]  │
│                                    │
│  ┌──────────────────────────────┐  │
│  │  👤 Kamau Njoroge           │  │
│  │  🌅 Morning: 22 L · KES 1,320│  │
│  │  🌇 Evening: 18 L · KES 1,080│  │
│  │  Today: 40 L = KES 2,400     │  │
│  │  Balance: KES 8,200 🔴       │  │
│  └──────────────────────────────┘  │
│  ┌──────────────────────────────┐  │
│  │  👤 Njeri Wanjiku            │  │
│  │  🌅 Morning: 40 L · KES 2,400│  │
│  │  🌇 Evening: not yet ⚠       │  │  ← Clearly flagged
│  │  So far: 40 L = KES 2,400    │  │
│  │  Balance: KES 4,200          │  │
│  └──────────────────────────────┘  │
│                                    │
│  ┌─────────────┐ ┌──────────────┐  │
│  │ + Morning 🌅│ │ + Evening 🌇 │  │
│  └─────────────┘ └──────────────┘  │
└────────────────────────────────────┘
```

---

#### SCREEN 04 — Record Milk Delivery (Bottom Sheet)

Opened from Home quick actions or Milk screen buttons. Title shows the session.

```
┌────────────────────────────────────┐
│  ▬  Record Morning Milk 🌅         │
│                                    │
│  Supplier                          │
│  ┌──────────────────────────────┐  │
│  │  Kamau Njoroge            ▼  │  │  ← Searchable dropdown
│  └──────────────────────────────┘  │
│                                    │
│  Date                              │
│  ┌──────────────────────────────┐  │
│  │  Thu, 4 June 2026        📅  │  │  ← Defaults to today
│  └──────────────────────────────┘  │
│                                    │
│  Litres Received *                 │
│  ┌──────────────────────────────┐  │
│  │  22                          │  │  ← Numeric keyboard
│  └──────────────────────────────┘  │
│                                    │
│  Price per Litre (KES)             │
│  ┌──────────────────────────────┐  │
│  │  65               (morning)  │  │  ← Pre-filled from morning default in Settings
│  └──────────────────────────────┘  │
│                                    │
│  22 L × KES 60  =  KES 1,320       │  ← Live computed, read-only
│                                    │
│  Quality                           │
│  ◉ Good    ○ Rejected    ○ Partial │
│                                    │
│  Notes (optional)                  │
│  ┌──────────────────────────────┐  │
│  └──────────────────────────────┘  │
│                                    │
│  ┌──────────────────────────────┐  │
│  │        SAVE DELIVERY         │  │
│  └──────────────────────────────┘  │
└────────────────────────────────────┘
```

**On save:** *"✅ Morning milk saved. Kamau: 22L — KES 1,320. Balance: KES 9,520."*  
If SMS enabled: second line *"SMS sent to Kamau."*

---

#### SCREEN 05 — People Screen

```
┌────────────────────────────────────┐
│ People                   ✅☁  ⋮  │
├────────────────────────────────────┤
│  🔍 Search by name or number...    │
│                                    │
│  [ All ]  [ Suppliers ]  [ Customers ]│  ← Filter tabs
│                                    │
│  ┌──────────────────────────────┐  │
│  │  👤 Kamau Njoroge           │  │
│  │  📞 0722 123 456            │  │
│  │  🥛 Supplier  👥 Customer   │  │  ← Auto role badges
│  │  Net: Shop owes KES 6,960   │  │
│  └──────────────────────────────┘  │
│  ┌──────────────────────────────┐  │
│  │  👤 Wanjiru Mwangi           │  │
│  │  📞 0733 456 789            │  │
│  │  👥 Customer                │  │
│  │  Net: Owes shop KES 1,400 🟠│  │
│  └──────────────────────────────┘  │
│                                    │
│                     [+ Add Person] │  ← FAB
└────────────────────────────────────┘
```

---

#### SCREEN 06 — Add / Edit Person

```
┌────────────────────────────────────┐
│ ← Add Person                      │
├────────────────────────────────────┤
│                                    │
│  Name *                            │
│  ┌──────────────────────────────┐  │
│  │  Kamau Njoroge               │  │
│  └──────────────────────────────┘  │
│                                    │
│  Phone Number *                    │
│  ┌─────────────────────┐  [ 📞 ]  │  ← 📞 opens contacts picker
│  │  0722 123 456        │          │
│  └─────────────────────┘          │
│  ✅ Number not registered yet      │  ← Live duplicate check
│                                    │
│  Send SMS Notifications            │
│  ◉ Yes    ○ No                     │
│                                    │
│  Notes (optional)                  │
│  ┌──────────────────────────────┐  │
│  └──────────────────────────────┘  │
│                                    │
│  ┌──────────────────────────────┐  │
│  │         SAVE PERSON          │  │
│  └──────────────────────────────┘  │
│                                    │
│  ℹ Role (Supplier / Customer) is   │
│  assigned automatically based on   │
│  transactions recorded.            │
└────────────────────────────────────┘
```

---

#### SCREEN 07 — Person Profile & Ledger

The main screen for any individual. Shows their auto-detected role(s), net balance, and full transaction history.

```
┌────────────────────────────────────┐
│ ← Kamau Njoroge          ✏ Edit   │
├────────────────────────────────────┤
│  📞 0722 123 456                   │
│  🥛 Supplier  ·  👥 Customer       │  ← Auto badges
│                                    │
│  ┌──────────────────────────────┐  │
│  │  Net balance:                │  │
│  │  Shop owes Kamau             │  │
│  │  KES 6,960                   │  │  ← Large, prominent
│  └──────────────────────────────┘  │
│                                    │
│  [ This Month ▼ ]                  │
│                                    │
│  DATE    DETAILS             KES   │
│  ──────────────────────────────    │
│  4 Jun  🌇 Evening milk 18L  +1,080│
│  4 Jun  🌅 Morning milk 22L  +1,320│
│  3 Jun  💵 Cash paid        -3,000 │
│  3 Jun  🌇 Evening milk 22L  +1,320│
│  2 Jun  🛒 Goods: 2kg Sugar   -240 │
│  2 Jun  🌅 Morning milk 20L  +1,200│
│  1 Jun  📱 Kamau paid credit -2,000│
│  1 Jun  🛍 Credit: Unga 2kg   +300 │
│                                    │
│  ┌──────────────┐ ┌─────────────┐  │
│  │ + Payment    │ │ + Credit    │  │  ← Contextual bottom buttons
│  └──────────────┘ └─────────────┘  │
└────────────────────────────────────┘
```

**Notes:**
- `+` amounts: shop owes supplier more, or customer owes shop more
- `−` amounts: a debt is being reduced
- The net balance line always reflects the current state after all entries
- Filter selector (This Month / All Time / custom date range)

---

#### SCREEN 08 — Record Supplier Payment (Bottom Sheet)

```
┌────────────────────────────────────┐
│  ▬  Record Payment — Kamau         │
│  Shop owes Kamau: KES 6,960        │
│                                    │
│  Payment Method *                  │
│  ○ Cash                            │
│  ○ M-Pesa                          │
│  ○ Goods from Shop                 │
│                                    │
│  ── Cash / M-Pesa ──────────────   │
│  Amount (KES) *                    │
│  ┌──────────────────────────────┐  │
│  │  3,000                       │  │
│  └──────────────────────────────┘  │
│  M-Pesa Reference (if M-Pesa)      │
│  ┌──────────────────────────────┐  │
│  └──────────────────────────────┘  │
│                                    │
│  ── Goods from Shop ────────────   │
│  Item description      Amount(KES) │
│  ┌───────────────────┐ ┌────────┐  │
│  │ 2kg Sugar         │ │  240   │  │
│  └───────────────────┘ └────────┘  │
│  [ + Add another item ]            │
│  Goods total: KES 240              │
│                                    │
│  ── Preview ─────────────────────  │
│  KES 6,960 − KES 3,000 = KES 3,960 │  ← Live
│                                    │
│  Notes (optional)                  │
│  ┌──────────────────────────────┐  │
│  └──────────────────────────────┘  │
│                                    │
│  ┌──────────────────────────────┐  │
│  │        SAVE PAYMENT          │  │
│  └──────────────────────────────┘  │
└────────────────────────────────────┘
```

---

#### SCREEN 09 — Record Customer Credit (Bottom Sheet)

```
┌────────────────────────────────────┐
│  ▬  Record Credit — Wanjiru        │
│  Wanjiru owes shop: KES 1,400      │
│  Credit limit: KES 2,000           │
│  ██████████░░░  70% used           │  ← Visual progress bar
│                                    │
│  What did they take?               │
│  ┌──────────────────────────────┐  │
│  │  Unga 2kg, cooking oil       │  │  ← Free text description
│  └──────────────────────────────┘  │
│                                    │
│  Amount (KES) *                    │
│  ┌──────────────────────────────┐  │
│  │  420                         │  │
│  └──────────────────────────────┘  │
│                                    │
│  KES 1,400 + KES 420 = KES 1,820   │  ← Live; turns red if over limit
│                                    │
│  Notes (optional)                  │
│  ┌──────────────────────────────┐  │
│  └──────────────────────────────┘  │
│                                    │
│  ┌──────────────────────────────┐  │
│  │        SAVE CREDIT           │  │
│  └──────────────────────────────┘  │
└────────────────────────────────────┘
```

---

#### SCREEN 10 — Reports

```
┌────────────────────────────────────┐
│ Reports                   ⋮      │
├────────────────────────────────────┤
│  [ Today ]  [ This Week ]  [ Month ▼]│
│                                    │
│  Milk Received                     │
│  ┌──────────────────────────────┐  │
│  │  1,240 L  ·  KES 74,400      │  │
│  │  🌅 Morning: 680L  🌇 Eve: 560L│ │
│  │  [Stacked bar — AM/PM split] │  │
│  └──────────────────────────────┘  │
│                                    │
│  Supplier Reliability              │
│  ┌──────────────────────────────┐  │
│  │  Kamau:  93% ✅  1,240 L ↑   │  │
│  │  Njeri:  78% 🟠   840 L →   │  │
│  │  [ View full report → ]      │  │
│  └──────────────────────────────┘  │
│                                    │
│  Supplier Debts                    │
│  ┌──────────────────────────────┐  │
│  │  Total owed out: KES 16,600  │  │
│  │  Kamau:   KES 8,200  🔴      │  │
│  │  Njeri:   KES 4,200          │  │
│  └──────────────────────────────┘  │
│                                    │
│  Customer Credit                   │
│  ┌──────────────────────────────┐  │
│  │  Total owed in: KES 3,800    │  │
│  │  Wanjiru: KES 1,400  🟠      │  │
│  │  Peter:   KES 2,400  🔴      │  │
│  └──────────────────────────────┘  │
│                                    │
│  ┌──────────────────────────────┐  │
│  │       📤 Export / Share      │  │
│  └──────────────────────────────┘  │
└────────────────────────────────────┘
```

---

#### SCREEN 11 — Settings

```
┌────────────────────────────────────┐
│ ← Settings                        │
├────────────────────────────────────┤
│                                    │
│  🏪 Shop Info                      │
│     Name · M-Pesa number           │
│                                    │
│  🥛 Milk Pricing                   │
│     Morning: KES 65 / litre        │  ← Two separate defaults
│     Evening: KES 60 / litre        │
│     (Both editable per delivery)   │
│                                    │
│  🌙 Appearance                     │
│     ◉ Light   ○ Dark   ○ System   │
│                                    │
│  📱 SMS Notifications              │
│     ◉ On   ○ Off                   │
│     View SMS log · Retry failed    │
│                                    │
│  ⚠ Alert Thresholds                │
│     Debt alert: KES 5,000          │
│     Customer overdue: 7 days       │
│                                    │
│  ☁ Google Drive Backup             │
│     Last: Today 2:03 AM ✅         │
│     Schedule: Daily at 2:00 AM     │
│     Back Up Now · Restore · History│
│                                    │
│  👥 App Users                      │
│     Add user · Reset PIN           │
│                                    │
│  🔄 Sync                           │
│     Last: 3 min ago ✅             │
│     Force Sync Now                 │
│                                    │
│  ℹ About  ·  v1.0.0               │
└────────────────────────────────────┘
```

---

### 7.5 Global UX Rules

- **Empty states:** Every list has an illustration and action prompt — *"No suppliers yet. Record the first morning milk delivery to add one automatically."*
- **Skeleton loading:** Shimmer placeholders while data loads — no blank screens, no spinners
- **Undo:** 5-second snackbar with **UNDO** button after every save
- **Destructive actions:** Always named confirmation dialog — *"Delete Kamau's payment of KES 3,000 on 3 Jun? This cannot be undone."*
- **Amounts:** Formatted with commas — KES 8,200 not KES 8200. Decimal supported for litres
- **Dates:** Human-readable — *"Thu, 4 Jun"* not *"1717459200000"*
- **Accessibility:** All elements labelled with `contentDescription`. WCAG AA contrast (4.5:1 minimum) in both themes
- **Auto role note:** Where roles are shown, a small note explains: *"Role assigned automatically from transactions"*

---

## 8. Sync & Multi-User Architecture

### 8.1 Sync Flow

1. Any write → saved to Room immediately. App never waits for internet.
2. Record marked `syncStatus = PENDING`, queued in WorkManager
3. `SyncWorker` fires when network available → pushes to Firestore → marks `SYNCED`
4. If offline → exponential backoff, retries on reconnection
5. Firestore listeners on other devices update local Room in real time

### 8.2 Conflict Resolution

- Each record carries `lastModifiedAt` + `deviceId`
- Two devices edit same record offline → newer `lastModifiedAt` wins
- Deliveries and transactions are **append-only** after sync — no edits after sync
- Running balance always recomputed from the full transaction log; never from a stored field

### 8.3 Firebase Security Rules

```javascript
match /shops/{shopId}/{document=**} {
  allow read, write: if request.auth != null
    && request.auth.token.shopId == shopId;
}
```

### 8.4 WorkManager Jobs

| Worker | Trigger | Purpose |
|---|---|---|
| `SyncWorker` | Network available + every 15 min | Push Room → Firestore |
| `BackupWorker` | Daily at configured time | Export DB → JSON → Google Drive |

SMS uses `SmsManager` directly (no worker needed — it's a synchronous call after each save).

---

## 9. Development Phases

### Phase 1 — Foundation *(Weeks 1–2)*
- Project: Kotlin, Hilt, Room, Compose, Navigation, DataStore
- Firebase Auth: profile select + PIN login
- All Room tables + DAOs
- Firestore sync layer + `SyncWorker`
- Navigation scaffold + Light/Dark theme + design tokens

### Phase 2 — People & Contacts *(Week 3)*
- People CRUD
- Contacts picker integration (`ContactsContract`)
- Duplicate phone detection
- Auto role badge logic (computed from transactions)

### Phase 3 — Milk Tracking *(Weeks 4–5)*
- Record morning / evening (separate entries)
- Milk screen with per-supplier daily view
- Ledger transaction created on each delivery
- Running balance calculation
- Home dashboard milk summary
- Missing evening delivery warning (after 5 PM)

### Phase 4 — Payments & Credit *(Weeks 6–7)*
- Person ledger screen (unified, all transaction types)
- Supplier payment recording: cash, M-Pesa, goods from shop
- Customer credit recording
- Customer payment recording
- Credit limit enforcement and alerts
- Debt alert threshold badges

### Phase 5 — SMS *(Week 8)*
- `SmsManager` integration
- All 5 SMS templates
- SMS log screen in Settings
- Per-person SMS toggle

### Phase 6 — Google Drive Backup *(Week 9)*
- Google Sign-In + `drive.file` scope
- `BackupWorker`: Room → JSON → Drive
- Backup settings, restore flow, history, retention (last 30)

### Phase 7 — Reports & Polish *(Week 10)*
- All report screens + MPAndroidChart charts
- PDF export + WhatsApp / Email share
- Full Settings screen (all sections)
- Empty states, skeleton loaders, Undo snackbars
- Sync stress test (3 devices)

### Phase 8 — Testing & Launch *(Weeks 11–12)*
- Unit tests: balance calculations, role detection, conflict logic
- Room DAO integration tests
- WorkManager sync tests
- SMS tested on real Kenyan numbers
- Drive backup + restore end-to-end
- Beta with actual shop staff
- Distribute via Firebase App Distribution or APK sideload

---

## 10. Gradle Dependencies

```kotlin
dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Room
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Firebase (free tier — no billing required)
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // DataStore (settings, theme preference)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Google Drive Backup
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.api-client:google-api-client-android:2.2.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev20240521-2.0.0")

    // Encrypted SharedPreferences (Drive token)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // JSON (backup export)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Image loading
    implementation("io.coil-kt:coil-compose:2.6.0")

    // NOTE: No SMS library needed. Uses Android built-in SmsManager.
    // NOTE: No inventory/POS library needed. Products & expenses deferred to v2.
}
```

---

## 11. Testing Strategy

| Test Type | Tools | Coverage |
|---|---|---|
| Unit Tests | JUnit5 + MockK | Balance calculations, role detection (computed from transactions), conflict resolution |
| Room DAO Tests | In-memory Room | All DAOs: insert, query, soft-delete, running balance queries |
| Sync Tests | WorkManager TestDriver | Offline → online, two-device conflict scenarios |
| SMS Tests | SmsManager mock | Correct template rendered, correct recipient, failure logging |
| Backup Tests | Drive API mock | JSON completeness, restore integrity |
| UI Tests | Compose Testing | Login, record morning milk, supplier payment (goods), customer credit |
| Manual QA | 3 real devices | Multi-device sync, SMS on real numbers, Drive backup, contacts picker |

---

## 12. Android Development Skill (Claude Code)

This project uses the **[dpconde/claude-android-skill](https://github.com/dpconde/claude-android-skill)** — a Claude Code skill that teaches Claude modern Android development patterns based on Google's official architecture guidance and the [NowInAndroid](https://github.com/android/nowinandroid) reference app.

### 12.1 What the Skill Provides

When this skill is active, Claude Code will automatically follow these patterns when generating, reviewing, or refactoring Kangai Shop code:

| Area | Pattern enforced |
|---|---|
| **Architecture** | Clean Architecture — UI / Domain / Data layers with clear boundaries |
| **ViewModels** | `@HiltViewModel`, `StateFlow<UiState>` exposed via `stateIn(WhileSubscribed(5_000))` |
| **Screens** | Route composable (wires ViewModel) + stateless Screen composable (pure UI) |
| **Repositories** | Offline-first: Room as source of truth, Firestore sync layer behind the interface |
| **Data flow** | Unidirectional: events flow down, state flows up (UDF pattern) |
| **DI** | Hilt throughout — no manual service locators |
| **Async** | Kotlin Coroutines + Flow everywhere; no RxJava, no callbacks |
| **Testing** | Interfaces + test doubles; avoid mocking frameworks where possible |
| **Module structure** | Feature modules with `api/` (public contracts) + `impl/` (internal logic) |

### 12.2 Installation

```bash
# Clone the skill into your Claude Code skills directory
git clone https://github.com/dpconde/claude-android-skill.git ~/.claude/skills/claude-android-skill
```

Claude Code detects and loads the skill automatically when you open an Android project. No further configuration needed.

### 12.3 Skill File Structure

```
claude-android-skill/
├── SKILL.md                    ← Main skill definition, loaded first
├── references/
│   ├── architecture.md         ← UI/Domain/Data layer patterns
│   ├── compose-patterns.md     ← Jetpack Compose best practices
│   ├── gradle-setup.md         ← Convention plugins, version catalogs
│   ├── modularization.md       ← Multi-module feature structure
│   └── testing.md              ← Testing strategies and patterns
├── assets/templates/
│   ├── libs.versions.toml.template
│   └── settings.gradle.kts.template
└── scripts/
    └── generate_feature.py     ← Generates a complete feature module scaffold
```

### 12.4 Generating a Feature Module

The skill includes a script to scaffold a new feature module (screen + ViewModel + UiState + Hilt + Gradle):

```bash
python ~/.claude/skills/claude-android-skill/scripts/generate_feature.py milk \
  --package com.eeelaminnovations.kangaishop \
  --path /path/to/KangaiShop
```

This generates:
```
feature/
└── milk/
    ├── api/        ← Navigation contracts (public)
    └── impl/       ← Screen, ViewModel, UiState, Hilt module (internal)
```

Repeat for each feature: `people`, `reports`, `settings`.

### 12.5 Code Patterns Claude Will Use

**ViewModel (MilkViewModel example):**
```kotlin
@HiltViewModel
class MilkViewModel @Inject constructor(
    private val recordDeliveryUseCase: RecordDeliveryUseCase,
    private val getTodayDeliveriesUseCase: GetTodayDeliveriesUseCase,
) : ViewModel() {

    val uiState: StateFlow<MilkUiState> = getTodayDeliveriesUseCase()
        .map { MilkUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MilkUiState.Loading,
        )

    fun recordDelivery(delivery: MilkDelivery) {
        viewModelScope.launch {
            recordDeliveryUseCase(delivery)
        }
    }
}
```

**Screen pattern:**
```kotlin
@Composable
internal fun MilkRoute(
    viewModel: MilkViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MilkScreen(
        uiState = uiState,
        onRecordDelivery = viewModel::recordDelivery,
    )
}

@Composable
internal fun MilkScreen(
    uiState: MilkUiState,
    onRecordDelivery: (MilkDelivery) -> Unit,
) { /* pure UI, no ViewModel reference */ }
```

**Offline-first repository:**
```kotlin
interface MilkDeliveryRepository {
    fun getDeliveriesForDate(date: LocalDate): Flow<List<MilkDelivery>>
    suspend fun recordDelivery(delivery: MilkDelivery)
}

internal class OfflineFirstMilkDeliveryRepository @Inject constructor(
    private val dao: MilkDeliveryDao,
    private val syncQueue: SyncQueue,
) : MilkDeliveryRepository {

    override fun getDeliveriesForDate(date: LocalDate): Flow<List<MilkDelivery>> =
        dao.getByDate(date).map { it.map(MilkDeliveryEntity::toDomain) }

    override suspend fun recordDelivery(delivery: MilkDelivery) {
        dao.insert(delivery.toEntity())
        syncQueue.enqueue(delivery.id)   // queue for Firestore push
    }
}
```

### 12.6 How This Skill Affects Kangai Shop Development

When using Claude Code with this skill on the Kangai Shop project, Claude will:

- Always propose the correct layer for new code (UI / domain use case / data repository)
- Generate `UiState` sealed classes (Loading / Success / Error) for every screen
- Route all data through `Flow` — never return one-shot results from repositories
- Place business rules (balance calculation, role detection, credit limit check) in use cases, not ViewModels
- Write tests using fakes/stubs over Mockito where possible
- Flag any code that violates the architecture (e.g. ViewModel accessing Room directly)

---

## 13. Future Enhancements (v2.0)

- Basic inventory / product list (for shops that want to track stock)
- Expense tracking
- M-Pesa Daraja API for real-time payment confirmation
- Swahili language support
- Multi-shop / branch support
- Web dashboard for remote monitoring

---

*Kangai Shop — Built for Kenya by Eelam Innovations*

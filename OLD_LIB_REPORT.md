I have now read all 65 files. Let me compile the comprehensive report.

# Calorie Bank — Full Reference Report (Flutter/Dart → Kotlin/Compose Rewrite)

This document is the complete, verbatim-detail reference for rewriting the Calorie Bank app. It covers every entity, formula, screen, repository, provider, route, color, and constant found in `OLD/lib`. All 65 Dart files were read in full.

---

## 1. Domain entities

All entities extend `Equatable` (value equality based on a `props` list — not all fields participate in equality, noted per class). Entities are plain immutable classes with `copyWith`.

### BankAccount (`domain/entities/bank_account.dart`)
```
userId       : String   (required)
balance      : int      (required)   // total saved calories
lastUpdated  : DateTime  (required)
```
- `copyWith({int? balance})` → returns new BankAccount with `lastUpdated` always reset to `DateTime.now()`.
- `props` (equality) = `[userId, balance]` (lastUpdated excluded).

### CalorieTransaction (`domain/entities/calorie_transaction.dart`)
```
id                : String    (required)
timestamp         : DateTime  (required)
type              : TransactionType (required)
calories          : int       (required)  // always positive; direction derived from type
label             : String    (required)
foodEntryId       : String?   (nullable)
exerciseEntryId   : String?   (nullable)
userId            : String    (required)
date              : String    (required)  // 'YYYY-MM-DD', for daily grouping
```
`enum TransactionType`: `foodWithdrawal, exerciseDeposit, bankWithdrawal, bankDeposit, dailySavingsDeposit, manualAdjustment`

Extension `TransactionTypeExtension` on `TransactionType`:
- `isPositive` (bool):
  - `true` for: `exerciseDeposit`, `bankWithdrawal` (bank→daily adds to daily), `dailySavingsDeposit`
  - `false` for: `foodWithdrawal`, `bankDeposit` (daily→bank removes from daily), `manualAdjustment`
- `label` (String): `foodWithdrawal`→"Food", `exerciseDeposit`→"Exercise", `bankWithdrawal`→"Bank Withdrawal", `bankDeposit`→"Savings Deposit", `dailySavingsDeposit`→"Daily Savings", `manualAdjustment`→"Manual Adjustment"
- `icon` (String, emoji): Food 🍽️, Exercise 🏃, Bank Withdrawal 🏦, Savings Deposit 💰, Daily Savings 📈, Manual Adjustment ✏️

Instance getters on `CalorieTransaction`:
- `signedCalories` (int) — used for **daily** balance math:
  - `foodWithdrawal` → `-calories`
  - `exerciseDeposit` → `+calories`
  - `bankWithdrawal` → `+calories` (adds to daily balance)
  - `bankDeposit` → `-calories` (removes from daily balance)
  - `dailySavingsDeposit` → `0` (bank-only, no daily effect)
  - `manualAdjustment` → `+calories`
- `bankSignedCalories` (int) — used for **bank** balance math:
  - `bankWithdrawal` → `-calories`
  - `bankDeposit` → `+calories`
  - `dailySavingsDeposit` → `+calories`
  - default (all others) → `0`

`props` = `[id, timestamp, type, calories]`.

### DailySummary (`domain/entities/daily_summary.dart`)
```
userId              : String    (required)
date                : DateTime  (required)
budget              : int       (required)
consumed            : int       (default 0)
exerciseBonus       : int       (default 0)   // calories earned from exercise
bankBonus           : int       (default 0)   // calories drawn from bank
macros              : Macros    (default const Macros())
micros              : Micros    (default const Micros())
endOfDayProcessed   : bool      (default false)
```
Convenience getters (legacy compat): `proteinConsumedG → macros.proteinG`, `carbsConsumedG → macros.carbsG`, `fatConsumedG → macros.fatG`, `fiberConsumedG → macros.fiberG`.

**Calorie math getters (exact formulas):**
```
totalAvailable   = budget + exerciseBonus + bankBonus
remaining        = (totalAvailable - consumed).clamp(0, totalAvailable)
deficit          = (consumed - totalAvailable).clamp(0, consumed)
isOverBudget     = consumed > totalAvailable
consumedPercent  = totalAvailable > 0 ? consumed / totalAvailable : 0
```
`copyWith(...)` accepts `consumed, exerciseBonus, bankBonus, macros, micros, endOfDayProcessed` plus **legacy overrides** `proteinConsumedG, carbsConsumedG, fatConsumedG, fiberConsumedG` which, if passed, are folded into a new `Macros` via `this.macros.copyWith(...)` (macros object wins if explicitly passed).

`props` = `[userId, date, consumed, budget]`.

### ExerciseEntry (`domain/entities/exercise_entry.dart`)
```
id                : String    (required)
timestamp         : DateTime  (required)
exerciseName      : String    (required)
durationMinutes   : int       (required)
caloriesBurned    : int       (required)
notes             : String?   (nullable)
userId            : String    (required)
```
`props` = `[id, timestamp, exerciseName, caloriesBurned]`.
**Note**: There is no persistence table/repository for `ExerciseEntry` as its own row — exercise logging is recorded purely as a `CalorieTransaction` (type `exerciseDeposit`); the entity itself is constructed transiently in chat/manual-entry flows and not saved to SQLite directly.

### FoodEntry / FoodItem (`domain/entities/food_entry.dart`)

**FoodItem**
```
name       : String   (required)
quantity   : String   (required)
calories   : int      (required)
macros     : Macros   (default const Macros())
micros     : Micros   (default const Micros())
```
Convenience getters: `proteinG, carbsG, fatG, fiberG, sugarG, saturatedFatG, cholesterolMg` (from `macros`), `sodiumMg` (from `micros`).
`toJson()` → `{name, quantity, calories, ...macros.toJson(), ...micros.toJson()}` (flattened single map).
`fromJson(j)` → parses `name` (default ''), `quantity` (default ''), `calories` (default 0), `macros: Macros.fromJson(j)`, `micros: Micros.fromJson(j)`.
`props` = `[name, quantity, calories]`.

**FoodEntry**
```
id             : String        (required)
timestamp      : DateTime      (required)
mealType       : String        (required)  // breakfast|lunch|dinner|snack
foods          : List<FoodItem> (required)
totalCalories  : int           (required)
totalMacros    : Macros        (default const Macros())
totalMicros    : Micros        (default const Micros())
aiSessionId    : String?       (nullable)
userId         : String        (required)
```
Convenience getters: `totalProteinG, totalCarbsG, totalFatG, totalFiberG` (from `totalMacros`).
Factory `FoodEntry.fromFoods({id, mealType, foods, aiSessionId?, userId})`:
- `totalMacros = foods.fold(const Macros(), (acc,f) => acc + f.macros)`
- `totalMicros = foods.fold(const Micros(), (acc,f) => acc + f.micros)`
- `totalCalories = foods.fold(0, (sum,f) => sum + f.calories)`
- `timestamp = DateTime.now()`
`mealTypeLabel` getter: breakfast→"Breakfast", lunch→"Lunch", dinner→"Dinner", snack→"Snack", else→raw mealType.
`props` = `[id, timestamp, totalCalories]`.

### Nutrition (`domain/entities/nutrition.dart`) — Macros, Micros, DailyReferenceValues

**Macros**
```
proteinG        : double (default 0)
carbsG          : double (default 0)
fatG            : double (default 0)
fiberG          : double (default 0)
sugarG          : double (default 0)
saturatedFatG   : double (default 0)
transFatG       : double (default 0)
cholesterolMg   : double (default 0)
```
`operator +` sums all fields. `copyWith(...)` all fields overridable. `toJson()` keys: `protein_g, carbs_g, fat_g, fiber_g, sugar_g, saturated_fat_g, trans_fat_g, cholesterol_mg`. `fromJson(j)` parses same keys via helper `_d(v) => v==null?0.0:(v as num).toDouble()`. `props` includes all 8 fields.

**Micros**
```
sodiumMg      : double (default 0)   // mg
potassiumMg   : double (default 0)   // mg
calciumMg     : double (default 0)   // mg
ironMg        : double (default 0)   // mg
magnesiumMg   : double (default 0)   // mg
zincMg        : double (default 0)   // mg
phosphorusMg  : double (default 0)   // mg
vitaminCMg    : double (default 0)   // mg
vitaminDUg    : double (default 0)   // μg
vitaminB12Ug  : double (default 0)   // μg
folateMcg     : double (default 0)   // μg (DFE)
vitaminAUg    : double (default 0)   // μg RAE
vitaminEMg    : double (default 0)   // mg
vitaminKUg    : double (default 0)   // μg
```
`operator +`, `copyWith`, `toJson`/`fromJson` (snake_case keys matching field names, e.g. `sodium_mg`, `vitamin_d_ug`, `vitamin_b12_ug`, `folate_mcg`) — same pattern as Macros. `props` = all 14 fields.

**DailyReferenceValues** (static const, FDA/WHO reference daily values):
```
fiberG = 28
sugarG = 50            // <10% of 2000 kcal
saturatedFatG = 20      // <10% of 2000 kcal
cholesterolMg = 300
sodiumMg = 2300
potassiumMg = 4700
calciumMg = 1300
ironMg = 18
magnesiumMg = 420
zincMg = 11
phosphorusMg = 1250
vitaminCMg = 90
vitaminDUg = 20
vitaminB12Ug = 2.4
folateMcg = 400
vitaminAUg = 900
vitaminEMg = 15
vitaminKUg = 120
```

### UserProfile (`domain/entities/user_profile.dart`)
```
id                    : String    (required)
email                 : String    (required)
displayName           : String    (required)
age                   : int       (required)
gender                : String    (required)   // 'male'|'female'|'other'
heightCm              : double    (required)
currentWeightKg       : double    (required)
goalWeightKg          : double    (required)
activityLevel         : String    (required)   // sedentary|light|moderate|active|very_active
goal                  : String    (required)   // weight_loss|maintenance|weight_gain
dailyCalorieBudget    : int       (required)
dailyProteinGoalG     : double    (required)
dailyCarbsGoalG       : double    (required)
dailyFatGoalG         : double    (required)
dailyFiberGoalG       : double    (default 28)
dailySugarGoalG       : double    (default 50)
isPremium             : bool      (default false)
createdAt             : DateTime  (required)
updatedAt             : DateTime  (required)
```
`copyWith(...)` accepts all fields except `id/email/createdAt` (immutable); always resets `updatedAt = DateTime.now()`.
`props` = `[id, email, dailyCalorieBudget, isPremium]`.

### ChatMessage / AiResponse (`domain/entities/chat_message.dart`)
```
enum ChatRole { user, assistant, system }
enum ChatMessageStatus { sending, sent, error }
```
**ChatMessage**
```
id             : String              (required)
sessionId      : String              (required)
role           : ChatRole             (required)
content        : String              (required)
timestamp      : DateTime            (required)
status         : ChatMessageStatus   (default ChatMessageStatus.sent)
hasFoodLog     : bool                (default false)  // did this message create a food entry?
hasExerciseLog : bool                (default false)
```
Getters: `isUser => role==ChatRole.user`, `isAssistant => role==ChatRole.assistant`.
`copyWith({status?, hasFoodLog?, hasExerciseLog?})`.
`props` = `[id, role, content, status]`.

**AiResponse** (not Equatable, plain class)
```
message  : String                (required)
action   : String                (required)   // food_log|exercise_log|bank_withdraw|clarify|none
data     : Map<String, dynamic>? (nullable)
```
Getters: `isFoodLog, isExerciseLog, isBankWithdraw, isClarify` (string equality checks against `action`).

---

## 2. Core business logic (`core/utils/calorie_calculator.dart`)

Static-only class `CalorieCalculator` (private constructor, cannot be instantiated).

### BMR — Mifflin-St Jeor Equation (exact)
```dart
base = (10 * weightKg) + (6.25 * heightCm) - (5 * age)
BMR  = gender.toLowerCase()=='male' ? base + 5 : base - 161
```
(Any gender other than 'male', including 'female' or 'other', uses `base - 161`.)

### TDEE
```dart
TDEE = BMR * activityMultiplier(activityLevel)
```
Activity multipliers (from `AppConstants`, matched via `activityLevel.toLowerCase()`, default→moderate if unrecognized):
```
sedentary    = 1.2
light        = 1.375
moderate     = 1.55
active       = 1.725
very_active  = 1.9
```

### Daily calorie budget
```dart
adjustment = goalAdjustment(goal)   // kcal
dailyBudget = (TDEE + adjustment).round().clamp(1200, 5000)
```
Goal adjustments (matched via `goal.toLowerCase()`, default→maintenance if unrecognized):
```
weight_loss   = -500
maintenance   = 0
weight_gain   = +300
```
Clamp bounds: **min 1200, max 5000** kcal.

### Macro goals (grams), derived from percentages of `dailyCalories`
```
Protein goal (g) = (dailyCalories * proteinPercent) / proteinKcalPerGram
Carbs   goal (g) = (dailyCalories * carbsPercent)   / carbsKcalPerGram
Fat     goal (g) = (dailyCalories * fatPercent)     / fatKcalPerGram
```
Constants (from `AppConstants`):
```
proteinPercent = 0.30      carbsPercent = 0.40      fatPercent = 0.30   (sums to 1.0)
proteinKcalPerGram = 4.0   carbsKcalPerGram = 4.0   fatKcalPerGram = 9.0
```

### `calculateFromProfile(...)` — orchestrates all of the above
```dart
bmr = calculateBMR(weightKg, heightCm, age, gender)
tdee = calculateTDEE(bmr, activityLevel)
dailyCalories = calculateDailyBudget(tdee, goal)
return NutritionGoals(
  dailyCalories: dailyCalories,
  proteinG: calculateProteinGoal(dailyCalories),
  carbsG:   calculateCarbsGoal(dailyCalories),
  fatG:     calculateFatGoal(dailyCalories),
)
```
`NutritionGoals` is a plain immutable holder: `{dailyCalories: int, proteinG: double, carbsG: double, fatG: double}`.

No fiber/sugar goal calculation function exists in the calculator — `UserProfile.dailyFiberGoalG`/`dailySugarGoalG` just use hardcoded defaults (28g / 50g) unless later edited.

---

## 3. The "calorie bank" concept end to end

### Conceptual model
The app tracks calories like a bank account with two ledgers:
1. **Daily ledger** (`DailySummary`): budget for today = `budget + exerciseBonus + bankBonus`. Consuming food reduces "remaining" (via `consumed`). Exercise increases available calories for today (`exerciseBonus`). Withdrawing from the bank also increases available calories for today (`bankBonus`).
2. **Bank ledger** (`BankAccount.balance`, persisted separately, cross-day): calories saved from **past** days that were under-budget. Deposits happen automatically at end-of-day; withdrawals happen manually or via AI chat, and add to today's `bankBonus`.

### End-of-day processing (`endOfDayCheckerProvider` in `home_provider.dart`)
Runs (as a `FutureProvider`) every time `HomeScreen` builds, using `prefs.lastActiveDate`:
- If `lastActiveDate` exists and is not today (calendar-day compare on Y/M/D):
  1. Fetch yesterday's `DailySummary`. If it exists and `!endOfDayProcessed`:
     - `remaining = summary.remaining` (uses the same clamp-based getter above).
     - If `remaining > 0`: call `bankRepo.deposit(userId, remaining, 'Daily Savings — {monthAbbrev} {day}')`.
     - Mark `summary.copyWith(endOfDayProcessed: true)` and persist via `updateSummary`.
  2. If today's summary doesn't exist yet, create one from the user's `dailyCalorieBudget` (fresh `DailySummary(userId, date: today, budget: profile.dailyCalorieBudget)` — all other fields default to 0/empty).
  3. Set `prefs.lastActiveDate = today`.
- If `remaining <= 0` (over budget), **nothing is deposited** — no penalty/negative-bank mechanic exists.

### BankRepositoryImpl (`data/repositories/bank_repository_impl.dart`) — sqflite-backed
Table `bank_accounts (user_id PK, balance INTEGER, last_updated TEXT)`.
- `getBankAccount(userId)`: queries by `user_id`; if no row, **auto-creates** one with `balance=0` and returns a fresh `BankAccount`.
- `updateBankBalance(userId, newBalance)`: `UPDATE bank_accounts SET balance=?, last_updated=now() WHERE user_id=?`.
- `deposit(userId, calories, label)`:
  - reads current account, `newBalance = balance + calories`, calls `updateBankBalance`.
  - inserts a `calorie_transactions` row with `type = dailySavingsDeposit`, `calories = calories` (positive), `label`.
- `withdraw(userId, calories, label)`:
  - `newBalance = (balance - calories).clamp(0, balance)` (cannot go negative; if `calories > balance` the clamp silently floors at `0`, it does **not** throw — but UI layers separately validate `amount > bank.balance` before calling this).
  - calls `updateBankBalance`, then inserts a `calorie_transactions` row with `type = bankWithdrawal`, `calories = calories` (positive), `label`.
- `watchBankAccount(userId)`: **polling stream** — `yield await getBankAccount(userId)` every 2 seconds in an infinite loop (`async*` + `Future.delayed`). This is the general pattern for all "watch" streams in this app (no real reactive DB).
- `getBankTransactions(userId, {limit=30})`: `WHERE user_id=? AND (type='dailySavingsDeposit' OR type='bankWithdrawal') ORDER BY timestamp DESC LIMIT ?`.

### BankScreen (`presentation/bank/bank_screen.dart`)
- `SliverAppBar` titled "Calorie Bank".
- `_BankBalanceCard`: displays `bank.balance.kcalFormatted` in a large `displaySmall`-styled (44px) white text on a dark blue→navy gradient (`0xFF0F1E3D` → `0xFF091426`), accent-colored border/glow. Header row: icon badge + "Savings Account" / "Calorie Bank" labels. Below balance: `GradientButton` "Withdraw Calories" (accent gradient `[accent, 0xFF5A45CC]`) — disabled (`onPressed: null`) when `balance == 0`; navigates to `/manual/bank-withdraw`.
- "Recent Bank Activity" section: watches `todaysTransactionsProvider` (note: this actually only shows **today's** transactions filtered client-side by `type.name.contains('bank') || type.name.contains('savings')` — it does NOT use `getBankTransactions` from the repo, so historical (non-today) bank transactions are not shown here despite the repo supporting it). Empty state: savings icon + "No bank activity yet" / "Save calories today and they'll appear here".
- Each `_BankTxRow`: icon (down-arrow if deposit/savings, up-arrow if withdrawal), label, signed amount (`+`/`-` colored positive/negative).
- `_HowItWorksCard`: static 3-step explainer: (1) "Stay under budget → unused calories are auto-saved" (positive color), (2) "Savings accumulate in your bank balance" (accent color), (3) "Withdraw when you want a cheat meal or special occasion" (warning color).

### BalanceCard widget (home screen) (`presentation/home/widgets/balance_card.dart`)
Displays a 2×2 grid inside a dark gradient card (`0xFF131929`→`0xFF0E1622`):
- **Daily Budget** = `summary.totalAvailable` (primary color, wallet icon)
- **Consumed** = `summary.consumed` (negative color, restaurant icon)
- **Remaining** = `summary.remaining` (positive color, or negative+warning icon if `isOverBudget`)
- **Bank Balance** = `bankBalance` param (accent color, account_balance icon)
- Header shows "Today's Account" label + today's date pill (`d MMM` format).
- Bottom: `_ProgressBar` using `summary.consumedPercent` — bar turns negative-colored if `progress > 1.0` (over budget), shows `"{percent}%"` label (percent clamped 0–999 for display only, bar value clamped 0.0–1.0).

### MacroProgressCard widget (home screen) (`presentation/home/widgets/macro_progress_card.dart`)
Tappable card (navigates to `/nutrition`), shows:
- 4 `_MacroRing` mini pie-chart rings (72×72, `fl_chart` `PieChart`, `centerSpaceRadius: 24`) for **Protein**, **Carbs**, **Fat**, **Fiber**:
  - `progress = goal>0 ? (consumed/goal).clamp(0,1) : 0`
  - Each ring shows percent in center, consumed grams below, "/ {goal}g" beneath that.
  - Colors: Protein=`AppColors.protein` (#7B61FF), Carbs=`AppColors.carbs` (#FF8C42), Fat=`AppColors.fat` (#FF4757), Fiber=`0xFF66BB6A` (hardcoded green, goal = `DailyReferenceValues.fiberG` = 28).
- Below: a `Wrap` of 5 `_MicroChip`s showing Na, K, Ca, Fe, Vit C values (from `summary.micros`) — sodium chip turns negative-red if `sodiumMg > DailyReferenceValues.sodiumMg` (2300), others always muted-gray regardless of value.

---

## 4. Every screen

### Home (`presentation/home/home_screen.dart`)
- `ConsumerWidget`; watches `endOfDayCheckerProvider` (triggers side-effect), `todaysSummaryProvider`, `bankAccountProvider`, `todaysFoodEntriesProvider`, `todaysTransactionsProvider`, `userProfileProvider`.
- `CustomScrollView` with a floating `SliverAppBar` (custom, 80px expanded height) showing time-of-day greeting ("Good morning ☀️" <12h, "Good afternoon 🌤️" <17h, else "Good evening 🌙") + user's `displayName` (or "Calorie Banker" fallback) + circular avatar button (accent gradient, person icon) navigating to `/profile`.
- Body (in order): `BalanceCard`, `MacroProgressCard` (if profile+summary present), `_AiChatBanner` ("Log with CalBot" / "Just tell me what you ate", purple gradient `0xFF1A1035`→`0xFF0F0A2A`, navigates `/chat`), "Today's Meals" section (`SectionHeader` + list of `_FoodEntryTile`, each showing meal emoji, meal label, food names, `-{calories} kcal`) if any food today, "Transactions" section (`SectionHeader` "All"→`/transactions`, first 5 `_TransactionTile`s with icon/label/time/signed amount).
- `FloatingActionButton.extended` "Manual Log" (outline style) → `/manual/food`.
- Uses `flutter_animate` fade/slide entrance animations throughout with staggered delays (100–400ms).

### Bank — see Section 3 above.

### Chat (`presentation/chat/chat_screen.dart`)
- `ConsumerStatefulWidget`. `AppBar` with CalBot avatar icon (accent gradient), title "CalBot"/"Your nutrition assistant", info button opens a bottom sheet listing "How to use CalBot" (Log food / Log exercise / Bank withdrawal + reminder to set API key).
- Empty state: animated icon + "Hi! I'm CalBot 👋" + description + 4 tappable suggestion chips (prefilled sample messages that auto-send on tap).
- Message list: `_ChatBubble` per `ChatMessage` — right-aligned accent-tinted bubble for user, left-aligned with bot avatar for assistant; shows a green checkmark + "Logged to your account" / "Exercise logged" caption if `hasFoodLog`/`hasExerciseLog`; timestamp below each bubble.
- Typing indicator: 3 pulsing dots shown while `chatState.isSending`.
- Input bar: multiline `TextField` (max 4 lines) + circular send button (accent gradient, spinner while sending).
- All actual logic delegated to `chatProvider` (see Section 7).

### Manual Entry
Three modal routes, all `ConsumerStatefulWidget`s pushed over the shell (routes: `/manual/food`, `/manual/exercise`, `/manual/bank-withdraw`).

**ManualFoodScreen** (`manual_food_screen.dart`):
- Meal-type toggle row (breakfast/lunch/dinner/snack pills).
- Fields: Food Name (required), Quantity (default "1 serving"), Calories (required, int), Protein/Carbs/Fat grams (each default "0").
- On save: builds one `FoodItem` → `FoodEntry.fromFoods(...)` → `foodLogRepositoryProvider.addFoodEntry` → inserts a `foodWithdrawal` transaction → updates (or creates) today's `DailySummary` via legacy `copyWith` params (`proteinConsumedG` etc, which add onto existing macro values through the `Macros.copyWith` merge logic in the entity).

**ManualExerciseScreen** (`manual_exercise_screen.dart`):
- Fields: Exercise name (required), Duration minutes (default "30"), Calories Burned (default "200").
- On save: inserts a `CalorieTransaction` of type `exerciseDeposit` directly (no `ExerciseEntry` row is persisted); updates today's summary `exerciseBonus += calories`.

**ManualBankWithdrawScreen** (`manual_bank_withdraw_screen.dart`):
- Shows available balance banner (accent gradient).
- Fields: Withdraw Amount (kcal, required, must be >0 and ≤ current bank balance — validated client-side before calling repo), Reason (default "Cheat meal").
- On submit: `bankRepositoryProvider.withdraw(userId, amount, reason)`, then updates today's summary `bankBonus += amount`.

### Nutrition Detail (`presentation/nutrition/nutrition_detail_screen.dart`)
- Watches `todaysSummaryProvider` + `userProfileProvider`.
- `_CalorieSummaryPill`: 3 values in a primary-gradient bar — Consumed / Remaining / Budget (=`totalAvailable`).
- "Macronutrients" section: 2-column grid (`_MacroGrid`/`_MacroTile`) of 8 items: Protein, Carbs, Fat, Fiber, Sugar, Sat. Fat, Trans Fat, Cholesterol — each shows value, progress bar vs its goal (profile goals for P/C/F; `DailyReferenceValues` for fiber/sugar/satfat/cholesterol; trans fat has goal=0 i.e. no bar), warning icon if `current > goal*1.05`.
- "Minerals" section (`_MicroList`): Sodium, Potassium, Calcium, Iron, Magnesium, Zinc, Phosphorus — each a row with label, progress bar (value/RDV clamped to 1.2 max), value text, "{percent}% DV" caption. Sodium coloring is inverted ("lower is better": red if `value > rdv*1.1`).
- "Vitamins" section (`_MicroList`): Vitamin C, Vitamin D, Vitamin B12, Folate, Vitamin A, Vitamin E, Vitamin K.
- Empty state if no summary: "🥗 No food logged today".

### Onboarding
**IntroScreen** (`onboarding/intro_screen.dart`) — first-run only (gated by `prefs.isIntroSeen`), 3-page `PageView` carousel:
1. "Welcome to Calorie Bank" / "Manage your calories just like money in a bank account." 🏦 (primary color)
2. "Save for later" / "Eat less today, save the surplus to spend on a weekend feast." 💰 (accent color)
3. "Log with AI" / "Just tell CalBot what you ate. No more searching for items." 🤖 (protein color)
- Dot page indicator + `GradientButton` ("Continue" / "Get Started" on last page) → sets `prefs.isIntroSeen=true` → navigates `/login`.

**OnboardingScreen** (`onboarding/onboarding_screen.dart`) — 4-page `PageView` (non-swipeable, `NeverScrollableScrollPhysics`), driven by `onboardingProvider`:
1. **Personal Info**: Full Name (required — blocks advancing with a snackbar if empty), Age (+/- stepper, clamped 10–100, default 25), Gender (male/female/other pills, default male).
2. **Body Metrics**: unit toggle (Metric/US), sliders for Height (140–220cm / converted to inches for US), Current Weight (40–200kg / lbs, ×2.20462 conversion), Goal Weight (same range) — internally always stored in metric (kg/cm).
3. **Goal**: goal selection cards — Lose Weight🔥(weight_loss, "-500 kcal/day"), Maintain Weight⚖️(maintenance, "No adjustment"), Gain Weight💪(weight_gain, "+300 kcal/day"); Activity Level list — Sedentary(desk job)/Light(1-3d/wk)/Moderate(3-5d/wk)/Active(6-7d/wk)/Very Active(physical job+exercise).
4. **Summary**: shows computed `state.calculatedGoals` (via `CalorieCalculator.calculateFromProfile`) — big daily-calorie-budget card (primary gradient), 3 macro cards (Protein/Carbs/Fat grams), info rows (Goal, Activity, Current weight, Target weight).
- Header: back-arrow (if page>0) + "{page+1} / 4" counter + linear progress bar.
- Footer: `GradientButton` "Continue" / "Get Started 🚀" on last page; submitting calls `onboardingProvider.notifier.submit()` which builds a `UserProfile`, saves it, creates today's `DailySummary`, sets `onboardingComplete=true` and `lastActiveDate=today`, then navigates `/home`.
- `PopScope`: back button only pops the route on page 0; otherwise goes to previous onboarding page.

### Premium (`presentation/premium/premium_screen.dart`)
- Static marketing screen. Hero card (purple gradient, star icon) "Premium" / "Unlimited AI logging, no ads, full access".
- 5 feature rows: Unlimited AI Messages, No Advertisements, Advanced Analytics (weekly/monthly trends), Cloud Backup, Priority Support.
- `GradientButton` "Subscribe — $4.99 / month" — currently just shows a snackbar "Play Billing integration coming soon" (no real IAP wired up).
- Footer text: "Cancel anytime. Billed monthly via Google Play."

### Profile (`presentation/profile/profile_screen.dart`)
- Watches `userProfileProvider`, `bankAccountProvider`, `todaysSummaryProvider`.
- Avatar (80×80, primary gradient, person icon), display name, email, "Premium" pill badge if `isPremium`.
- 3 `_StatCard`s: Today (`consumed` kcal, primary), Bank (`balance` kcal, accent), Budget (`dailyCalorieBudget` kcal, positive).
- Menu sections: **Account** → Edit Profile (`/profile/edit`), Upgrade to Premium (`/profile/premium`, accent color); **App** → AI Provider Settings (`/profile/ai-settings`), Settings (`/profile/settings`); **Account** (again) → Sign Out (negative color, calls `authActionsProvider.signOut()` then navigates `/login`).

### Edit Profile (`presentation/profile/edit_profile_screen.dart`)
**Stub only** — literally just "Edit profile coming soon." text + a "Save Changes" button that pops the screen. No real field editing wired to `UserProfileRepository` in this codebase; this is a known gap that must be implemented properly in the rewrite (name/age/gender/height/weight/goal/activity editing → recompute nutrition goals).

### Settings (`presentation/settings/settings_screen.dart`)
- Plain `ListView` of `ListTile`s:
  - (only if guest user, i.e. `userId` starts with `guest_`) "Sign in with Google" → `authActionsProvider.signInWithGoogle()`.
  - "AI Provider" → configure AI API key (navigates via `Navigator.pushNamed` — note this is inconsistent with the rest of the app's go_router usage, likely a bug/dead code since there's no named route `/profile/ai-settings` registered with Navigator, only with GoRouter).
  - "Notifications" → static `Switch` (value always `true`, `onChanged` no-op — not wired to any real notification system).
  - "App Version" → static "1.0.0" text.

### AI Provider Settings (`presentation/settings/ai_provider_settings_screen.dart`)
Two sections:
1. **Your Backend Server** (`_useCustomBackend` toggle): Backend URL field, Bearer Token field (obscured, toggle visibility), "Test Connection" button (calls `CustomBackendDataSource.testConnection()` → GET `{url with path replaced to /health}`, shows Connected✓/failure state), an `_ApiContractCard` showing the exact JSON request/response shape as reference documentation.
2. **Direct Provider (fallback)** (dimmed via `Opacity(0.45)` when custom backend enabled): provider picker (gemini🤖/openai⚡/claude🧠/openrouter🔀 — from `AppConstants.supportedProviders`), API Key field (obscured, toggle visibility).
- "Save Settings" button persists: `useCustomBackend`, `backendUrl`, `backendToken` (secure storage, only if non-empty), `aiProvider`, `aiApiKey` (secure storage, only if non-empty). Shows "✓ Saved!" for 2 seconds (button turns positive-green).

### Transactions (`presentation/transactions/transactions_screen.dart`)
- Watches `todaysTransactionsProvider` (today only — there is no "all-time" or historical browsing anywhere in the UI despite `getRecentTransactions` existing on the repo interface).
- `_DailySummaryBanner`: two side-by-side cards — "Earned" (sum of all `tx.type.isPositive` calories, positive-green) and "Spent" (sum of the rest, negative-red).
- `_LedgerRow` list: icon (emoji per type), label, time, signed calories. Toolbar action icon → `/bank`.
- Empty state: "No transactions today" / "Log food or exercise to see transactions".

### Auth / Login (`presentation/auth/login_screen.dart`)
- Full-screen dark gradient background. Logo (wallet icon, primary gradient), "Welcome to Calorie Bank" / "Your personal calorie savings account." tagline.
- "Continue with Google" button (white pill, mock 'G' letter as logo placeholder, calls `authActionsProvider.signInWithGoogle()` which is a **mock**: 1-second delay then sets `userId = 'google_user_{millis}'`, no real OAuth).
- "Login later (Continue as Guest)" text button → `authActionsProvider.continueAsGuest()` sets `userId = 'guest_{millis}'` → navigates `/onboarding`.
- Footer legal text (Terms/Privacy, non-functional links).
- Error banner shown if sign-in throws.

### App Shell / bottom nav (`presentation/shared/shell/app_shell.dart`)
`AppShell` wraps a `child` (from GoRouter `ShellRoute`) in a `Scaffold` with a custom 64px-tall `_BottomNav` (5 items, icon+label, selected=primary color / unselected=muted):
```
index 0: Home         icon home_rounded            → /home
index 1: AI           icon auto_awesome_rounded    → /chat
index 2: History      icon receipt_long_rounded    → /transactions
index 3: Bank         icon account_balance_rounded → /bank
index 4: Profile      icon person_rounded          → /profile
```
Current index derived from URL prefix match (`location.startsWith('/home')` etc., default 0).

### Shared widgets
- **GradientButton** (`shared/widgets/gradient_button.dart`): 54px-tall full-width button; gradient defaults to `[primary, primaryDark]` unless `gradientColors` passed; grey/disabled (`AppColors.textMuted`, no shadow) when `onPressed==null`; shows spinner if `isLoading`; optional leading icon + label text (`labelLarge`, 15px, `textOnPrimary` color).
- **SectionHeader** (`shared/widgets/section_header.dart`): title (titleLarge) + optional right-aligned tappable action label (primary color).

---

## 5. Data layer

### `data/datasources/local/isar_service.dart`
**Despite the filename, this is NOT Isar** — it's a `sqflite`-based `DatabaseService` (the file was renamed but never moved; comment confirms "Isar models have been replaced with sqflite repositories").
- Singleton `Database` accessed via `DatabaseService.instance` (lazy-initialized on first access, DB file `calorie_bank.db`, current version **2**).
- **Schema (5 tables)**:
```sql
user_profiles (
  id TEXT PRIMARY KEY, email TEXT NOT NULL, display_name TEXT NOT NULL,
  age INTEGER NOT NULL, gender TEXT NOT NULL, height_cm REAL NOT NULL,
  current_weight_kg REAL NOT NULL, goal_weight_kg REAL NOT NULL,
  activity_level TEXT NOT NULL, goal TEXT NOT NULL,
  daily_calorie_budget INTEGER NOT NULL,
  daily_protein_goal_g REAL NOT NULL, daily_carbs_goal_g REAL NOT NULL, daily_fat_goal_g REAL NOT NULL,
  daily_fiber_goal_g REAL NOT NULL DEFAULT 28, daily_sugar_goal_g REAL NOT NULL DEFAULT 50,
  is_premium INTEGER NOT NULL DEFAULT 0,
  created_at TEXT NOT NULL, updated_at TEXT NOT NULL
)

food_entries (
  id TEXT PRIMARY KEY, user_id TEXT NOT NULL, timestamp TEXT NOT NULL, date_key TEXT NOT NULL,
  meal_type TEXT NOT NULL, foods_json TEXT NOT NULL, total_calories INTEGER NOT NULL,
  macros_json TEXT NOT NULL DEFAULT '{}', micros_json TEXT NOT NULL DEFAULT '{}'
)

calorie_transactions (
  id TEXT PRIMARY KEY, user_id TEXT NOT NULL, timestamp TEXT NOT NULL, date_key TEXT NOT NULL,
  type TEXT NOT NULL, calories INTEGER NOT NULL, label TEXT NOT NULL,
  food_entry_id TEXT, exercise_entry_id TEXT
)

bank_accounts (
  user_id TEXT PRIMARY KEY, balance INTEGER NOT NULL DEFAULT 0, last_updated TEXT NOT NULL
)

daily_summaries (
  id TEXT PRIMARY KEY, user_id TEXT NOT NULL, date_key TEXT NOT NULL, date TEXT NOT NULL,
  budget INTEGER NOT NULL, consumed INTEGER NOT NULL DEFAULT 0,
  exercise_bonus INTEGER NOT NULL DEFAULT 0, bank_bonus INTEGER NOT NULL DEFAULT 0,
  macros_json TEXT NOT NULL DEFAULT '{}', micros_json TEXT NOT NULL DEFAULT '{}',
  end_of_day_processed INTEGER NOT NULL DEFAULT 0,
  UNIQUE(user_id, date_key)
)
```
- Indexes: `idx_food_user_date(food_entries: user_id, date_key)`, `idx_tx_user_date(calorie_transactions: user_id, date_key)`, `idx_summary_user_date(daily_summaries: user_id, date_key)`.
- `_onUpgrade` (v1→v2): adds `macros_json`/`micros_json` columns to `food_entries` and `daily_summaries`, and `daily_fiber_goal_g`/`daily_sugar_goal_g` to `user_profiles`.
- `date_key` format everywhere: `'{year}-{month:2digits}-{day:2digits}'` (local helper `_dateKey` duplicated in each repo file).
- Macros/Micros are stored as JSON blobs (`jsonEncode(macros.toJson())`), not individual columns — legacy rows without JSON fall back to recomputing from `foods_json`.

### `data/datasources/local/preferences_service.dart`
Wraps `SharedPreferences` (plain prefs) + `FlutterSecureStorage` (for secrets). Keys used:
```
onboarding_completed   (bool)   — AppConstants.onboardingCompletedKey
intro_seen             (bool)   — literal 'intro_seen'
user_id                (String) — AppConstants.userIdKey
ai_api_key             (secure) — AppConstants.aiApiKeyKey
ai_provider            (String) — AppConstants.aiProviderKey, default 'gemini'
backend_url            (String) — AppConstants.backendUrlKey, default ''
backend_token          (secure) — AppConstants.backendTokenKey
use_custom_backend     (bool)   — AppConstants.useCustomBackendKey, default false
daily_budget           (unused directly in code but key defined) — AppConstants.dailyBudgetKey
last_active_date       (String, ISO8601) — AppConstants.lastActiveDateKey
ai_usage_today         (int)    — literal 'ai_usage_today'
ai_usage_date          (String, yyyy-MM-dd) — literal 'ai_usage_date'
```
Methods: `isOnboardingComplete`/`setOnboardingComplete`, `isIntroSeen`/`setIntroSeen`, `userId`/`setUserId`, `aiProvider`/`setAiProvider`, `getAiApiKey`/`setAiApiKey`/`clearAiApiKey` (secure), `useCustomBackend`/`setUseCustomBackend`, `backendUrl`/`setBackendUrl` (trims input), `getBackendToken`/`setBackendToken`/`clearBackendToken` (secure), `lastActiveDate`/`setLastActiveDate`, `aiUsageToday`/`aiUsageDate`/`incrementAiUsage()` (resets counter to 1 if the stored date ≠ today, else increments)/`resetAiUsage()`, `clear()` (wipes both prefs and secure storage — used on sign-out).

### `data/datasources/remote/ai_datasource.dart` — direct provider calls
Constructed with `{provider, apiKey, model?}`; `Dio` client with connect/receive timeouts from `ApiConstants` (30s/60s).
`sendMessage({history, userMessage})` dispatches by provider:
- **gemini** (also default/fallback for unknown providers): `POST {geminiBaseUrl}/models/{model}:generateContent?key={apiKey}` where `model` defaults to `gemini-2.0-flash`. Body:
```json
{
  "system_instruction": {"parts": [{"text": AppConstants.aiSystemPrompt}]},
  "contents": [ {"role": "user"|"model", "parts": [{"text": "..."}]}, ... ],
  "generationConfig": {"responseMimeType": "application/json", "temperature": 0.7, "maxOutputTokens": 1024}
}
```
  History messages with role containing "system" are skipped; user messages map to `role:"user"`, assistant to `role:"model"`. Response parsed from `response.data['candidates'][0]['content']['parts'][0]['text']`.
- **openai** / **openrouter** (shared `_sendOpenAiCompatible`): `POST {baseUrl}/chat/completions` with `Authorization: Bearer {apiKey}`. `baseUrl`/`model` = `openAiBaseUrl`/`gpt-4o-mini` for openai, `openRouterBaseUrl`/`openai/gpt-4o-mini` for openrouter (unless caller overrides `model`). Body:
```json
{
  "model": "...", "messages": [{"role":"system","content": AppConstants.aiSystemPrompt}, ...history..., {"role":"user","content": userMessage}],
  "response_format": {"type": "json_object"}, "temperature": 0.7, "max_tokens": 1024
}
```
  Response parsed from `response.data['choices'][0]['message']['content']`.
- **claude**: no explicit case in the switch — falls to `default: return _sendGemini(...)`. (Claude/Anthropic constants exist in `ApiConstants` — base URL `https://api.anthropic.com/v1`, model `claude-3-haiku-20240307`, path `/messages`, version header `2023-06-01` — but no actual Claude-format request is implemented; this is a bug/gap to fix in the rewrite if Claude support is desired.)
- `_parseResponse(rawText)`: `jsonDecode` → `AiResponse(message, action, data)`; on parse failure, wraps the raw text as `message` with `action:'none'`.

### `data/datasources/remote/custom_backend_datasource.dart` — user's own backend
`enum BackendErrorType { noUrlConfigured, connectionRefused, timeout, unauthorized, serverError, invalidResponse, noNetwork }` with a `userMessage` getter producing friendly emoji-prefixed strings for the chat UI (exact strings shown in section-4 chat/settings descriptions above).
`sendMessage({sessionId, history, userMessage, userContext?})`:
- Guards: throws `noUrlConfigured` if `backendUrl.isEmpty`.
- **Request** — `POST {backendUrl}`:
```json
{
  "session_id": "...",
  "message": "...",
  "history": [{"role":"user"|"assistant", "content":"..."}, ...]  // only messages with status==sent
  "user_context": { ... } ,
  "system_prompt": AppConstants.aiSystemPrompt
}
```
  Headers: `Content-Type: application/json`, `X-Client: CalorieBank/1.0`, `Authorization: Bearer {token}` if token non-empty.
- **Response** expected: `{message: String, action: String, data: Map?}`; throws `invalidResponse` if `message` is empty.
- Dio timeouts: connect 20s, receive 60s, send 15s.
- Error mapping: connection/send/receive timeout→`timeout`; connectionError→`connectionRefused`; 401/403→`unauthorized`; ≥500 or other bad status→`serverError` (with `HTTP {status}` detail); `SocketException`→`noNetwork`.
- `testConnection()`: GET `{backendUrl with path replaced to '/health'}` (`Uri.replace(path:'/health')`); returns `BackendHealthResult(isOk, message, latencyMs?)`. Latency read from `response.extra['latency_ms']` if present (not actually populated by Dio automatically — likely dead code / always `-1`).

**`user_context` payload actually sent** (built in `chat_provider.dart`):
```json
{
  "daily_budget": <today's DailySummary.budget or 0>,
  "consumed_today": <today's DailySummary.consumed or 0>,
  "remaining_today": <today's DailySummary.remaining or 0>,
  "bank_balance": <BankAccount.balance>,
  "user_id": "<userId>"
}
```

---

## 6. Repository implementations

All repos are sqflite-backed, singleton-per-call (`DatabaseService.instance`), no caching layer — every read hits SQLite directly. There is **no separate "model" layer**: `data/models/*.dart` are all empty stub files (comments only) — entities are mapped directly to/from SQLite rows inside each repository impl via private `_toRow`/`_fromRow` methods. This is an important simplification for the Kotlin rewrite: domain entities can map straight to Room `@Entity` classes, no separate DTO layer needed.

- **UserProfileRepositoryImpl**: `getProfile(userId)` (query by `id`), `saveProfile` (INSERT OR REPLACE), `updateProfile` (just calls `saveProfile`), `watchProfile` (5-second polling stream). Row mapping is 1:1 with the `user_profiles` schema above; `is_premium` stored as `0`/`1` int.
- **FoodLogRepositoryImpl**: `addFoodEntry` (INSERT OR REPLACE, serializes `foods` list to `foods_json` via `FoodItem.toJson()`, `macros_json`/`micros_json` from `totalMacros`/`totalMicros`), `deleteFoodEntry(entryId)`, `getEntriesForDate(userId, date)` (by `date_key`, ordered `timestamp ASC`), `watchEntriesForDate` (2s polling). `_fromRow` recomputes macros/micros from `foods` if JSON columns are missing/legacy/empty (`'{}'`or empty string) via try/catch fallback.
- **TransactionRepositoryImpl**: `addTransaction` (INSERT OR REPLACE), `getTransactionsForDate` (by `date_key`, `timestamp DESC`), `getRecentTransactions(userId, {limit=20})` (all-time, `timestamp DESC LIMIT`), `watchTransactionsForDate` (2s polling). `type` stored as the enum's `.name` string; parsed back via `TransactionType.values.firstWhere(name==, orElse: manualAdjustment)`.
- **BankRepositoryImpl**: see full description in Section 3.
- **DailySummaryRepositoryImpl**: `getSummaryForDate` (by `user_id`+`date_key`), `saveSummary` (INSERT OR REPLACE, generates a fresh row `id` via uuid every time — meaning re-saving via `saveSummary` doesn't preserve the original row id, though `date_key` uniqueness + REPLACE keeps one row per user/day), `updateSummary` (if no existing row, delegates to `saveSummary`; otherwise `UPDATE` only mutable fields: `consumed, exercise_bonus, bank_bonus, macros_json, micros_json, end_of_day_processed`), `watchSummaryForDate` (2s polling).
- **AiRepositoryImpl**: routing wrapper — no DB. If `prefs.useCustomBackend && backendUrl.isNotEmpty` → delegates to `CustomBackendDataSource`. Else, reads `prefs.getAiApiKey()`; if empty, throws `AiBackendException(noUrlConfigured)` (same error type reused as a generic "not configured" signal); otherwise delegates to `AiDataSource` with `prefs.aiProvider`.

---

## 7. State management (Riverpod)

All Riverpod (`flutter_riverpod`). Two flavors: `Provider`/`StreamProvider`/`FutureProvider` (simple derived/async state) and `NotifierProvider` (mutable state machines).

### `core/di/providers.dart` — infrastructure & repository providers
```
sharedPreferencesProvider   : Provider<SharedPreferences>       — throws if not overridden (must be overridden in main() with the real instance)
secureStorageProvider       : Provider<FlutterSecureStorage>    — const FlutterSecureStorage(androidOptions: encryptedSharedPreferences:true)
preferencesServiceProvider  : Provider<PreferencesService>      — built from the two above
userProfileRepositoryProvider    : Provider<UserProfileRepository>    → UserProfileRepositoryImpl()
foodLogRepositoryProvider        : Provider<FoodLogRepository>        → FoodLogRepositoryImpl()
transactionRepositoryProvider    : Provider<TransactionRepository>    → TransactionRepositoryImpl()
bankRepositoryProvider           : Provider<BankRepository>           → BankRepositoryImpl()
dailySummaryRepositoryProvider   : Provider<DailySummaryRepository>   → DailySummaryRepositoryImpl()
aiRepositoryProvider              : Provider<AiRepository>            → AiRepositoryImpl(prefs)
```

### `presentation/auth/providers/auth_provider.dart`
```dart
authStateProvider : StateProvider<AuthState>  // derived from prefs.userId at creation time (not reactive to later changes except via ref.invalidate)
```
`AuthState` (sealed-ish via private subclasses): `{isAuthenticated: bool, userId: String?, error: String?}`; factories `AuthState.authenticated(userId)`, `AuthState.unauthenticated()`, `AuthState.error(msg)`.
`authActionsProvider : Provider<AuthActions>` exposing:
- `signInWithGoogle()` — **mocked**: 1s delay, sets `userId = 'google_user_{millisSinceEpoch}'`, invalidates `authStateProvider`. (Real GoogleSignIn code is commented out.)
- `continueAsGuest()` — sets `userId = 'guest_{millisSinceEpoch}'`, invalidates `authStateProvider`.
- `isGuest` (getter) — `prefs.userId?.startsWith('guest_') ?? false`.
- `signOut()` — `prefs.clear()` (wipes all prefs+secure storage), invalidates `authStateProvider`.

### `presentation/chat/providers/chat_provider.dart`
`ChatState { sessionId: String, messages: List<ChatMessage> (default []), isSending: bool (default false), error: String? }` with `copyWith`.
`ChatNotifier extends Notifier<ChatState>`:
- `build()` → `ChatState(sessionId: uuid.v4())` (new session id each app/provider (re)build).
- `sendMessage(String userText)` — full flow:
  1. Trim-check empty → no-op.
  2. Append user `ChatMessage` (status `sent`) immediately, set `isSending=true`.
  3. Check daily AI usage: `limit = profile.isPremium ? premiumTierAiDailyLimit(200) : freeTierAiDailyLimit(10)`; if `prefs.aiUsageToday >= limit`, appends a warning assistant message and returns early (no AI call made, no usage increment).
  4. Builds `userContext` (see Section 5) from today's `DailySummary` + `BankAccount`.
  5. Calls `aiRepositoryProvider.sendMessage(...)`; on success, `prefs.incrementAiUsage()`.
  6. Dispatches on `response.action`: `food_log` → `_processFoodLog`, `exercise_log` → `_processExerciseLog`, `bank_withdraw` → `_processBankWithdraw` (each returns whether it succeeded, used to set `hasFoodLog`/`hasExerciseLog` flags on the resulting assistant message).
  7. Appends assistant `ChatMessage` with `response.message`, flags set, `isSending=false`.
  8. Catches `AiBackendException` → appends an error-status assistant message with `.userMessage`; catches generic `Exception` → strips `"Exception:"` prefix, prefixes with ❌.
- `_processFoodLog(userId, data)`: parses `meal_type` (default 'snack') and `foods` list (`FoodItem.fromJson` each) from AI response `data`; builds `FoodEntry.fromFoods(...)`, saves via `foodLogRepositoryProvider`; creates a `foodWithdrawal` transaction with label `"{mealTypeLabel} • {food names joined by ', '}"`; calls `_updateDailySummary` (adds `calories` to `consumed`, adds `entry.totalMacros`/`totalMicros` to summary's macros/micros — creates the summary if it doesn't exist yet, using `profile?.dailyCalorieBudget ?? 2000` as budget). Returns `true`/`false` (catches all exceptions silently → `false`).
- `_processExerciseLog(userId, data)`: parses `exercise_name` (default 'Exercise'), `duration_minutes`, `calories_burned`, `notes`; creates an `exerciseDeposit` transaction with label `"{name} • {duration} min"`; if today's summary exists, adds `calories_burned` to `exerciseBonus` (does **not** create a summary if one doesn't exist — silent no-op in that edge case, unlike food log which does create one).
- `_processBankWithdraw(userId, data)`: parses `calories`, `reason` (default 'Bank Withdrawal'); calls `bankRepositoryProvider.withdraw`; if today's summary exists, adds `calories` to `bankBonus` (same caveat — no summary created if missing).
- `clearError()` — resets `error` to null.
`chatProvider = NotifierProvider<ChatNotifier, ChatState>`.

### `presentation/home/providers/home_provider.dart`
```
currentUserIdProvider     : Provider<String?>                — prefs.userId
todaysSummaryProvider     : StreamProvider<DailySummary?>     — dailySummaryRepo.watchSummaryForDate(userId, now)
bankAccountProvider       : StreamProvider<BankAccount>       — bankRepo.watchBankAccount(userId)
todaysFoodEntriesProvider : StreamProvider<List<FoodEntry>>   — foodLogRepo.watchEntriesForDate(userId, now)
todaysTransactionsProvider: StreamProvider<List<CalorieTransaction>> — transactionRepo.watchTransactionsForDate(userId, now)
userProfileProvider       : StreamProvider<UserProfile?>      — userProfileRepo.watchProfile(userId)
endOfDayCheckerProvider   : FutureProvider<void>              — see Section 3 for exact logic
```
All the `*Provider`s above return `Stream.empty()` if `userId == null`.

### `presentation/onboarding/providers/onboarding_provider.dart`
`OnboardingState`:
```
currentPage       : int     (default 0)
name              : String  (default '')
email             : String  (default '')
age               : int     (default 25)
gender            : String  (default 'male')
unitSystem        : String  (default 'metric')
heightCm          : double  (default 170)
currentWeightKg   : double  (default 70)
goalWeightKg      : double  (default 65)
activityLevel     : String  (default 'moderate')
goal              : String  (default 'weight_loss')
isSubmitting      : bool    (default false)
error             : String? (nullable)
```
`calculatedGoals` getter → `CalorieCalculator.calculateFromProfile(...)` using current state fields.
`OnboardingNotifier extends Notifier<OnboardingState>` exposes: `nextPage()`, `prevPage()` (clamped 0..3), `updateName/Email/Age/Gender/UnitSystem/Height/CurrentWeight/GoalWeight/ActivityLevel/Goal(v)` (each simple `copyWith`), and:
- `submit()` async: sets `isSubmitting=true`; gets/creates `userId` (`prefs.userId ?? uuid.v4()`); computes `goals`; builds and saves a `UserProfile` (fiber/sugar goals left at entity defaults 28/50 since onboarding doesn't set them); saves today's `DailySummary(userId, date: today, budget: goals.dailyCalories)`; sets `onboardingComplete=true` and `lastActiveDate=today`; returns `true`/`false` (catches exceptions, stores `.toString()` in `error`).

---

## 8. App-level structure

### `app/app.dart` — `CalorieBankApp`
`ConsumerWidget`. Sets `SystemUiOverlayStyle.light` with transparent status bar and `systemNavigationBarColor: 0xFF0A0E1A`. Builds `MaterialApp.router` with `title: 'Calorie Bank'`, `debugShowCheckedModeBanner: false`, `theme: AppTheme.darkTheme`, `routerConfig: ref.watch(routerProvider)`.

### `main.dart`
`WidgetsFlutterBinding.ensureInitialized()` → get `SharedPreferences.getInstance()` → `runApp(ProviderScope(overrides:[sharedPreferencesProvider.overrideWithValue(sharedPrefs)], child: CalorieBankApp()))`. SQLite DB initializes lazily on first access.

### `app/router/app_router.dart` — routes (go_router)
`routerProvider`, `initialLocation: '/home'`.

**Redirect logic** (evaluated on every navigation):
1. If `!isIntroSeen` and path≠`/intro` → redirect `/intro`.
2. Else if `!isLoggedIn` (i.e. `prefs.userId == null`): allow `/login` and `/intro`; otherwise redirect `/login`.
3. Else if `!isOnboarded` and path doesn't start with `/onboarding` → redirect `/onboarding`.
4. Else if logged in and path==`/login` → redirect `/home`.
5. Else no redirect.

**Full route table:**
```
/intro                          → IntroScreen                       (standalone, no shell)
/login                          → LoginScreen                       (standalone)
/onboarding                     → OnboardingScreen                  (standalone)

[ShellRoute wrapping AppShell — bottom nav]
  /home                         → HomeScreen
  /chat                         → ChatScreen
  /transactions                 → TransactionsScreen
  /bank                         → BankScreen
  /profile                      → ProfileScreen
    /profile/edit                → EditProfileScreen
    /profile/settings             → SettingsScreen
    /profile/ai-settings           → AiProviderSettingsScreen
    /profile/premium               → PremiumScreen
  /nutrition                    → NutritionDetailScreen

[Modal / standalone routes, pushed over the shell]
  /manual/food                  → ManualFoodScreen
  /manual/exercise               → ManualExerciseScreen
  /manual/bank-withdraw           → ManualBankWithdrawScreen
```
Note: `SettingsScreen` internally navigates to AI settings via `Navigator.pushNamed(context, '/profile/ai-settings')` (a `Navigator`-style call) whereas everywhere else the app uses `context.go`/`context.push` (go_router style) — this is an inconsistency/likely-dead code path in the original app since no `MaterialApp.routes` named-route table is registered; in the rewrite, use a single consistent navigation approach (Compose Navigation) throughout.

### Theme — `app/theme/app_colors.dart` (exact hex values)
```
Backgrounds:
  background        = #0A0E1A
  surface            = #131929
  surfaceElevated    = #1A2235
  cardBorder         = #1E2D45

Brand:
  primary            = #00D4AA
  primaryDark        = #00A882
  primaryLight       = #33DDBB
  accent             = #7B61FF
  accentLight        = #9B85FF

Semantic:
  positive           = #00C97B
  negative           = #FF4757
  warning            = #FFB347
  info               = #4ECDC4

Text:
  textPrimary        = #F0F4FF
  textSecondary      = #8896B0
  textMuted          = #4A5568
  textOnPrimary      = #0A0E1A

Chart/Progress:
  protein            = #7B61FF
  carbs              = #FF8C42
  fat                = #FF4757
  fiber              = #00C97B

Gradients (LinearGradient, topLeft→bottomRight unless noted):
  primaryGradient    = [#00D4AA → #00A882]
  accentGradient      = [#7B61FF → #5A45CC]
  bankGradient        = [#1A2235 → #0D1526]  (topCenter→bottomCenter)
  cardGradient        = [#1A2235 → #131929]
  positiveGradient    = [#00C97B → #00A862]
  negativeGradient    = [#FF4757 → #CC3344]
```
Additional inline (non-AppColors) gradients/colors seen in screens:
- AI banner / Premium hero gradient: `[#1A1035 → #0F0A2A]`
- BalanceCard background gradient: `[#131929 → #0E1622]`
- BankBalanceCard background gradient: `[#0F1E3D → #091426]`
- Login/Onboarding background gradient: `[#0A0E1A → #0D1526 → #0A0E1A]` (login, 3-stop, topCenter→bottomCenter) / `[#0A0E1A → #0D1526]` (onboarding, 2-stop)
- Fiber ring color (macro_progress_card): `#66BB6A` (hardcoded, not in AppColors)
- Nutrition detail extra colors: Sugar `#FFB300`, Sat.Fat `#EF5350`, Trans Fat `#F44336`, Cholesterol `#AB47BC`
- Code block bg in AI settings: `#0A0E1A`; code text color `#7FDBCA`

### Typography — `app/theme/app_text_styles.dart`
Font family: **Google Fonts "Inter"** (`GoogleFonts.inter(...)`) throughout.
```
displayLarge     : 48px, w700, letterSpacing -1.5, color textPrimary
displayMedium    : 36px, w700, letterSpacing -1.0, color textPrimary
displaySmall     : 28px, w700, letterSpacing -0.5, color textPrimary
headlineLarge    : 24px, w600, letterSpacing -0.25, color textPrimary
headlineMedium   : 20px, w600, letterSpacing -0.15, color textPrimary
headlineSmall    : 18px, w600, color textPrimary
titleLarge       : 16px, w600, color textPrimary
titleMedium      : 14px, w500, color textPrimary
titleSmall       : 12px, w500, letterSpacing 0.5, color textSecondary
bodyLarge        : 16px, w400, color textPrimary
bodyMedium       : 14px, w400, color textPrimary
bodySmall        : 12px, w400, color textSecondary
labelLarge       : 14px, w600, letterSpacing 0.1, color textPrimary
labelMedium      : 12px, w500, letterSpacing 0.5, color textSecondary
labelSmall       : 10px, w500, letterSpacing 0.8, color textMuted

// Special
calorieDisplay   : 40px, w700, letterSpacing -1.0, height 1.0, color primary
bankBalance      : 32px, w700, letterSpacing -0.5, height 1.1, color textPrimary
transactionAmount: 16px, w600, letterSpacing -0.25   (color set per-usage)
inputLabel       : 13px, w500, letterSpacing 0.3, color textSecondary
```

### Theme (`app/theme/app_theme.dart`) — `AppTheme.darkTheme`
Material 3, `Brightness.dark`, `scaffoldBackgroundColor = background`.
`ColorScheme.dark`: primary=primary, secondary=accent, surface=surface, error=negative, onPrimary=textOnPrimary, onSecondary/onSurface/onError=textPrimary, outline=cardBorder.
Component themes (exact values):
- **AppBar**: transparent bg, elevation 0, `scrolledUnderElevation:0`, light system overlay, icon color textPrimary, title style headlineSmall, `centerTitle:false`.
- **Card**: color surface, elevation 0, `borderRadius:16`, border `cardBorder` width 1, `margin:zero`.
- **Input decoration**: filled `surfaceElevated`, content padding h16/v16, all borders `OutlineInputBorder(radius:12)`; enabled/focus/error borders use cardBorder(1)/primary(1.5)/negative(1)/negative(1.5) respectively; hint=bodyMedium+textMuted, label=inputLabel, error=bodySmall+negative.
- **ElevatedButton**: bg primary, fg textOnPrimary, min size `(double.infinity, 54)`, radius 12, text=labelLarge@15px, elevation 0.
- **TextButton**: fg primary, text labelLarge.
- **OutlinedButton**: fg primary, min size `(double.infinity, 54)`, radius 12, side primary width 1.5, text labelLarge@15px.
- **BottomNavigationBar**: bg surface, selected primary, unselected textMuted, elevation 0, type fixed.
- **Divider**: color cardBorder, thickness 1, space 1.
- **SnackBar**: bg surfaceElevated, content bodyMedium, radius 12, floating behavior.
- **Icon** default: color textSecondary, size 22.
- **ListTile**: iconColor textSecondary, textColor textPrimary, tileColor transparent, radius 12.
- **Switch**: thumb primary when selected else textMuted; track primary@30%alpha when selected else cardBorder.
- **ProgressIndicator**: color primary, track cardBorder.
- **Dialog**: bg surface, radius 20, title headlineMedium, content bodyMedium.
- **BottomSheet**: bg surface, top radius 24, drag handle shown, handle color cardBorder.
- **Chip**: bg surfaceElevated, selected primary@20%alpha, label labelMedium, side cardBorder, radius 8.

---

## 9. Constants

### `core/constants/api_constants.dart`
```
geminiBaseUrl               = "https://generativelanguage.googleapis.com/v1beta"
geminiDefaultModel          = "gemini-2.0-flash"
geminiGenerateContentPath   = "/models/{model}:generateContent"

openAiBaseUrl               = "https://api.openai.com/v1"
openAiDefaultModel          = "gpt-4o-mini"
openAiChatPath              = "/chat/completions"

claudeBaseUrl               = "https://api.anthropic.com/v1"
claudeDefaultModel          = "claude-3-haiku-20240307"
claudeMessagesPath          = "/messages"
claudeVersion               = "2023-06-01"

openRouterBaseUrl           = "https://openrouter.ai/api/v1"
openRouterDefaultModel      = "openai/gpt-4o-mini"

connectTimeoutSeconds       = 30
receiveTimeoutSeconds       = 60
sendTimeoutSeconds          = 30
maxRetries                  = 3
```

### `core/constants/app_constants.dart`
```
appName                       = "Calorie Bank"
appVersion                    = "1.0.0"

onboardingCompletedKey        = "onboarding_completed"
userIdKey                     = "user_id"

aiApiKeyKey                   = "ai_api_key"
aiProviderKey                 = "ai_provider"
defaultAiProvider             = "gemini"

backendUrlKey                 = "backend_url"
backendTokenKey                = "backend_token"     // secure storage
useCustomBackendKey            = "use_custom_backend"
defaultBackendUrl              = ""

dailyBudgetKey                 = "daily_budget"
lastActiveDateKey               = "last_active_date"

defaultDailyCalories           = 2000
bankMaxBalance                 = 50000     // defined but not enforced anywhere in the read code (no cap check found)

freeTierAiDailyLimit           = 10
premiumTierAiDailyLimit         = 200

sedentaryMultiplier             = 1.2
lightMultiplier                  = 1.375
moderateMultiplier               = 1.55
activeMultiplier                  = 1.725
veryActiveMultiplier               = 1.9

weightLossAdjustment            = -500
maintenanceAdjustment            = 0
weightGainAdjustment              = 300

proteinPercent                   = 0.30
carbsPercent                      = 0.40
fatPercent                        = 0.30

proteinKcalPerGram                = 4.0
carbsKcalPerGram                   = 4.0
fatKcalPerGram                      = 9.0

usersCollection                    = "users"          // vestigial Firestore-era constant, unused by sqflite code
bankAccountsCollection               = "bank_accounts"  // same

supportedProviders                  = ["gemini", "openai", "claude", "openrouter"]

aiSystemPrompt  = <verbatim multi-line prompt below>
```

**`aiSystemPrompt`** (verbatim, this is the exact system prompt sent to the LLM and should be reused/ported as-is for CalBot in the rewrite):
```
You are CalBot, the nutrition assistant for Calorie Bank — a banking-style calorie tracking app.
Your role is to help users log food and exercise through natural conversation.

Rules:
1. Always respond in JSON with: "message" (string), "action" (string), optionally "data" (object).
2. The "action" must be one of: "food_log", "exercise_log", "bank_withdraw", "clarify", "none"
3. Use "clarify" when you need ONE more piece of info. Ask exactly one focused question.
4. Use "food_log" once you can estimate calories accurately.
5. Use "exercise_log" for physical activity.
6. Use "none" for general chat/greetings.
7. Be friendly, concise, and conversational — never clinical or form-like.
8. When estimating, use average restaurant/home-cooking portions unless specified.
9. ALWAYS populate macros AND micros for every food item as accurately as possible.
   If a micro is truly unknown, use 0.

food_log data format — include ALL fields:
{
  "meal_type": "breakfast|lunch|dinner|snack",
  "foods": [
    {
      "name": "...",
      "quantity": "...",
      "calories": 0,
      "protein_g": 0,
      "carbs_g": 0,
      "fat_g": 0,
      "fiber_g": 0,
      "sugar_g": 0,
      "saturated_fat_g": 0,
      "trans_fat_g": 0,
      "cholesterol_mg": 0,
      "sodium_mg": 0,
      "potassium_mg": 0,
      "calcium_mg": 0,
      "iron_mg": 0,
      "magnesium_mg": 0,
      "zinc_mg": 0,
      "phosphorus_mg": 0,
      "vitamin_c_mg": 0,
      "vitamin_d_ug": 0,
      "vitamin_b12_ug": 0,
      "folate_mcg": 0,
      "vitamin_a_ug": 0,
      "vitamin_e_mg": 0,
      "vitamin_k_ug": 0
    }
  ],
  "total_calories": 0
}

exercise_log data format:
{
  "exercise_name": "...",
  "duration_minutes": 0,
  "calories_burned": 0,
  "notes": "..."
}

bank_withdraw data format:
{
  "calories": 0,
  "reason": "..."
}
```

### Core extensions (utility helpers to reimplement, exact behavior)
`core/extensions/date_extensions.dart` (`DateTimeExtensions`):
- `isSameDay(other)` — Y/M/D equality.
- `startOfDay` — midnight of same day.
- `endOfDay` — `23:59:59.999` of same day.
- `isToday` / `isYesterday`.
- `relativeLabel` — "Today"/"Yesterday"/`"{MonthAbbrev} {day}"`.
- `timeLabel` — 12-hour `"h:mm AM/PM"` format (hour 0→12, hour>12 subtract 12, minute zero-padded).

`core/extensions/num_extensions.dart` (`NumExtensions` on `num`, `IntExtensions` on `int`):
- `kcalFormatted`: if ≥1000, `"{thousands},{remainder padded to 3}"` (e.g. 1400→"1,400"); else rounded plain integer string.
- `kcalShort`: if ≥1000, `"{value/1000 to 1 decimal}k"` (e.g. 1400→"1.4k"); else rounded integer string.
- `gramFormatted`: `"{1 decimal}g"`.
- `clampedProgress`: clamp to `[0.0, 1.0]`.
- `percentOf(total)`: `total==0 ? 0 : (this/total*100).clamp(0,999)`.
- `signedKcal` (int only): `"+{kcalFormatted} kcal"` if ≥0, else `"-{abs.kcalFormatted} kcal"`.

---

## Summary of gaps/inconsistencies worth resolving in the rewrite
1. **Edit Profile** is an unimplemented stub — must design and build real profile editing (should recompute nutrition goals via `CalorieCalculator` on save, matching onboarding logic).
2. **Claude/Anthropic provider** is declared in constants/UI but not actually implemented in `AiDataSource` (falls through to Gemini).
3. **Notifications toggle** in Settings is non-functional (static switch, no-op).
4. **Google Sign-In** and **Play Billing** (Premium subscribe) are fully mocked/stubbed — no real backend auth or payment integration exists to port; these need real design decisions for the Android rewrite.
5. **"Watch" streams** are all naive polling loops (2s or 5s `Future.delayed` in an infinite `async*` generator) rather than true reactive DB observers — in Kotlin/Room this should become a proper `Flow`-based reactive query instead of polling.
6. **Bank screen's "Recent Bank Activity"** only shows today's transactions (bug: filters `todaysTransactionsProvider` client-side) despite `BankRepository.getBankTransactions` existing for true bank history — decide whether to fix this in the rewrite (recommended: use the full-history method).
7. **`bankMaxBalance` (50000)** constant is defined but never enforced — decide whether to enforce a cap in the rewrite.
8. Exercise/bank-withdraw AI actions silently no-op if no `DailySummary` exists yet for today (unlike food-log which creates one) — a minor inconsistency to fix.
9. `Micros.props`/`Macros.props` participate in Equatable equality (all fields), while several parent entities (`CalorieTransaction`, `UserProfile`, `DailySummary`, `FoodEntry`, `BankAccount`) use partial-field equality — preserve or intentionally revisit this per-entity when defining `equals()`/`data class` equality in Kotlin.
# CalBot AI setup

CalBot's default backend is **Firebase AI Logic** (Gemini, called directly from the app,
gated by your own Firebase project) — no per-user API key required. Users can still override this
with their own BYOK provider key or a custom backend server in Settings → AI Provider Settings;
those paths are unaffected by anything below.

## Why it doesn't work yet

`app/build.gradle.kts` only applies the `google-services` Gradle plugin when
`app/google-services.json` is present, and `FirebaseAiDataSource` checks
`FirebaseApp.getApps(context)` before calling Gemini. Without a real config file, the app still
builds and runs fine — CalBot just returns a clear error ("Firebase AI Logic isn't set up yet...")
until you complete the steps below, and BYOK/custom-backend chat is unaffected.

## One-time setup

1. Go to the [Firebase console](https://console.firebase.google.com/) and create a project (or use
   an existing one).
2. Add an Android app to it with package name `com.roy.caloriebank`.
3. Download the generated `google-services.json` and place it at `app/google-services.json`
   (same folder as `app/build.gradle.kts`).
4. In the Firebase console, open **Build → AI Logic** and enable the **Gemini Developer API**
   (fastest to set up, has a free tier) or **Vertex AI Gemini API** (if you already use GCP
   billing). Either works with `GenerativeBackend.googleAI()` used in
   `FirebaseAiDataSource.kt` — switch to `GenerativeBackend.vertexAI()` there if you pick Vertex
   AI instead.
5. Rebuild the app. The `google-services` plugin activates automatically once the file exists, and
   CalBot starts routing through Firebase AI Logic for any user who hasn't set a BYOK key.

## Notes

- The Gemini model used is `gemini-2.5-flash` (`FirebaseAiDataSource.kt`, `MODEL_NAME`) — change it
  there if you want a different model.
- The system prompt enforces a strict JSON response contract (`food_log` / `exercise_log` /
  `bank_withdraw` / `clarify` / `none`) matching what `ChatViewModel` already parses — no app code
  changes needed if you swap models/backends, as long as the response stays JSON in that shape.
- Consider adding [Firebase App Check](https://firebase.google.com/docs/app-check) before shipping
  to production, so only your real app (not a scraped API key) can call the Gemini backend.

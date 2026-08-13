# Upstream issue triage

This fork tracks the open issues on [b-lam/Resplash](https://github.com/b-lam/Resplash/issues).
Every open upstream issue is listed below with what has been done about it here.

Status key:

- **Fixed** — addressed in this fork, with the commit that did it
- **Planned** — actionable, understood, not implemented yet
- **Needs info** — cannot be acted on without a reproduction or more detail
- **Out of scope** — nothing the app's code can do about it

## Fixed

| # | Title | What was wrong |
|---|---|---|
| [165](https://github.com/b-lam/Resplash/issues/165), [179](https://github.com/b-lam/Resplash/issues/179), [180](https://github.com/b-lam/Resplash/issues/180) | Auto Wallpaper / widget stops updating | Pressing "next wallpaper" cancelled the recurring job and left a delayed worker to recreate it an interval later. Losing that worker (force stop, OEM battery manager) left no scheduled work at all, so the wallpaper only changed by hand from then on. The recurring job is now re-enqueued immediately. |
| [167](https://github.com/b-lam/Resplash/issues/167), [182](https://github.com/b-lam/Resplash/issues/182) | Same photos come back around | Auto Wallpaper now checks the last 50 entries of its own history and asks Unsplash again, up to five times, when it gets a photo the user has recently seen. |
| [170](https://github.com/b-lam/Resplash/issues/170) | Notifications not working | `POST_NOTIFICATIONS` is requested through the activity result API, and every `notify()` goes through a helper that checks the app is allowed to post. Previously the call threw out of the Auto Wallpaper worker and took the wallpaper change with it. |
| [121](https://github.com/b-lam/Resplash/issues/121) | Auto Wallpaper resolution is wrong on the home screen | New "Screen size" wallpaper quality: Unsplash resizes the photo to the dimensions the launcher asks for, so the framework no longer downsamples a full resolution image. |
| [126](https://github.com/b-lam/Resplash/issues/126) | Screen size wallpaper to save bandwidth | Same "Screen size" quality option. |
| [136](https://github.com/b-lam/Resplash/issues/136) | Use the new blurhash property | The decoder was in the tree but nothing called it. Photo lists, collection lists and the photo detail screen now show the decoded blur hash while the image loads. |

Auto Wallpaper's quick settings tile also called `startActivityAndCollapse(Intent)`, which throws on
Android 14 — it hands over a `PendingIntent` there now.

## Planned

| # | Title | Notes |
|---|---|---|
| [135](https://github.com/b-lam/Resplash/issues/135) | Migrate to Paging 3 | The app is still on Paging 2 with hand-rolled `DataSource.Factory` classes. Mechanical but touches every list screen. |
| [134](https://github.com/b-lam/Resplash/issues/134) | Implement the topics endpoint | Needs a new tab and a topic detail screen. |
| [138](https://github.com/b-lam/Resplash/issues/138) | Auto Wallpaper intervals under 15 minutes | WorkManager's floor is 15 minutes, so this needs `AlarmManager` and a warning about battery use. |
| [152](https://github.com/b-lam/Resplash/issues/152), [161](https://github.com/b-lam/Resplash/issues/161) | Change the wallpaper at an absolute time | A time-of-day preference feeding the initial delay of the recurring job. |
| [150](https://github.com/b-lam/Resplash/issues/150) | Different wallpapers for light and dark mode | Needs a second source configuration plus a listener on the UI mode. |
| [149](https://github.com/b-lam/Resplash/issues/149), [156](https://github.com/b-lam/Resplash/issues/156) | Blur / dark overlay for wallpapers | The blur transformation added for the banner images can be reused on the Auto Wallpaper path. |
| [154](https://github.com/b-lam/Resplash/issues/154) | Several sources for Auto Wallpaper | The source preference is a single value today. |
| [177](https://github.com/b-lam/Resplash/issues/177) | Blocklist terms for Auto Wallpaper | Can be filtered client side against the photo tags. |
| [174](https://github.com/b-lam/Resplash/issues/174) | Force a wallpaper change | There is a FAB, a quick settings tile and a home screen widget for this; the request is for it to be reachable from the main screen too. |
| [90](https://github.com/b-lam/Resplash/issues/90) | Remember the scroll position | |
| [46](https://github.com/b-lam/Resplash/issues/46) | Like on double tap | |
| [153](https://github.com/b-lam/Resplash/issues/153) | Save to the SD card | Needs a tree URI picker; scoped storage rules out a plain path preference. |
| [160](https://github.com/b-lam/Resplash/issues/160) | Back up the account | |
| [172](https://github.com/b-lam/Resplash/issues/172) | A better widget | |
| [194](https://github.com/b-lam/Resplash/issues/194) | Position the photo before setting it | Needs a crop and pan screen. |
| [53](https://github.com/b-lam/Resplash/issues/53) | Animated transition into the photo details | |
| [71](https://github.com/b-lam/Resplash/issues/71) | Custom text overlay | |
| [113](https://github.com/b-lam/Resplash/issues/113) | Histogram on the photo details | |
| [137](https://github.com/b-lam/Resplash/issues/137) | Account switcher | Needs multi-account token storage. |
| [14](https://github.com/b-lam/Resplash/issues/14) | More languages | Open to translation contributions; the strings are all in `strings.xml`. |

## Needs info

| # | Title | Notes |
|---|---|---|
| [193](https://github.com/b-lam/Resplash/issues/193) | No pictures are shown | Consistent with the app's Unsplash API key being rate limited or suspended rather than with a bug in the client. Needs the error the app reports. |
| [151](https://github.com/b-lam/Resplash/issues/151) | Searching returns an error | The search request looks correct; without the response code this cannot be told apart from a rate limit. |
| [173](https://github.com/b-lam/Resplash/issues/173) | Photos download without their metadata | Unsplash serves images through an image proxy that strips EXIF, so this may not be recoverable client side. Unclear whether the report is about the file or about the details screen. |
| [118](https://github.com/b-lam/Resplash/issues/118) | Saving to a private collection fails | |
| [175](https://github.com/b-lam/Resplash/issues/175) | Cannot add collections | |
| [147](https://github.com/b-lam/Resplash/issues/147) | Wallpaper shows only half | Possibly the same crop hint problem as #121; worth retesting with the "Screen size" quality. |
| [195](https://github.com/b-lam/Resplash/issues/195) | Wallpaper does not scroll | Expected with the centre crop option, which produces an image exactly the size of the screen. Needs confirmation of which crop setting was in use. |
| [70](https://github.com/b-lam/Resplash/issues/70) | Random sort | The Unsplash listing endpoints only sort by latest, oldest and popular. |
| [197](https://github.com/b-lam/Resplash/issues/197) | trabajo programación 10 3 | Not a report. |

## Device specific

These are reported against a single manufacturer's wallpaper implementation. The app asks the
platform to set the lock screen with `WallpaperManager.FLAG_LOCK`, which is all it can do; several
OEM skins ignore it.

- [127](https://github.com/b-lam/Resplash/issues/127) — Honor / EMUI
- [145](https://github.com/b-lam/Resplash/issues/145) — LG G7
- [157](https://github.com/b-lam/Resplash/issues/157) — Samsung Fold 4, both screens
- [162](https://github.com/b-lam/Resplash/issues/162) — MIUI
- [125](https://github.com/b-lam/Resplash/issues/125) — setting the lock screen, which the app does support

## Out of scope

| # | Title | Why |
|---|---|---|
| [192](https://github.com/b-lam/Resplash/issues/192) | Play builds refuse non-GMS devices | A Play Console integrity setting on the upstream listing. This fork does build and run without Google Play services: the Firebase Gradle plugins are only applied when a `google-services.json` is present, and every Firebase call site is guarded. |
| [171](https://github.com/b-lam/Resplash/issues/171) | Pro without Google Play | Entitlements come from Play Billing; there is no other purchase channel. |
| [196](https://github.com/b-lam/Resplash/issues/196) | Cannot restore a purchase | Depends on the upstream Play account that sold the purchase. |
| [176](https://github.com/b-lam/Resplash/issues/176) | Unsplash+ collection is empty | Unsplash+ content is not served to third party API clients. |
| [159](https://github.com/b-lam/Resplash/issues/159) | Sources other than Unsplash | Outside what the app is. |

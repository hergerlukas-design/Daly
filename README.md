# Daly

Native Android-App für das **Tagesbriefing**: Sie zeigt jeden Morgen ein kurzes Briefing mit Terminen aus der
Kalender-App, wichtigen ungelesenen Mails aus MailFinder, einem hochgeladenen Wochenplan, eigenen Terminen und To-dos.
Alles läuft **auf dem Gerät**: Daly hat keine `INTERNET`-Permission, und die Zusammenfassung schreibt das lokale Modell
(Gemma), das schon in MailFinder liegt.

Paket `app.daly`, Anzeigename „Daly“. Zielgerät wie die anderen Apps: Android 13+ (minSdk 33), arm64.

## Zusammenspiel

| App | Repo | Was Daly nutzt |
|---|---|---|
| Kalender (`dev.kalender.app`) | `iOS-Android-Kalender-sync` | Provider `dev.kalender.app.briefing`: Termine lesen, Termine anlegen (`call createEvent`), Deep Link `kalender://event/<id>` |
| MailFinder (`app.mailfinder`) | `AIndroid` | Provider `app.mailfinder.briefing`: ungelesene/wichtige Mails (nur Absender, Betreff, Vorschau), Deep Link `mailfinder://message/<id>`; Service `BIND_LOCAL_AI`: lokale KI (Gemma) und Texterkennung (PaddleOCR) |

Fehlt eine App oder ist sie zu alt (`CONTRACT_VERSION`), blendet Daly den Bereich aus und zeigt einen Hinweis; der Rest
funktioniert weiter. Ohne KI schreibt Daly ein regelbasiertes Briefing.

### Vertrag

`app/src/main/kotlin/briefing/contract/BriefingContract.kt` und `app/src/main/aidl/briefing/contract/*.aidl` liegen
**unverändert in allen drei Repos**; `BriefingContractTest` pinnt die Werte. Eine kopierte Datei statt eines
Git-Submodules, weil alle Repos privat sind (ein Submodule bräuchte in jeder CI ein zusätzliches Token) und der Vertrag
klein ist. Änderungen nur ergänzend; was bricht, erhöht `CONTRACT_VERSION` in allen drei Repos im selben Schritt.

## Signierschlüssel einrichten (einmalig)

Die drei Apps haben **verschiedene Schlüssel**. Kalender und MailFinder behalten ihre (sonst müssten sie neu installiert
werden: verschlüsselte Datenbank, Modell und Passkeys gingen verloren). Stattdessen sind ihre Permissions
`signature|knownSigner` und erlauben zusätzlich genau das Zertifikat von Daly.

1. Schlüssel erzeugen (auf deinem Rechner, nicht im Repo), langes Passwort wählen, Datei und Passwort sicher aufbewahren:

   ```bash
   keytool -genkeypair -storetype PKCS12 -keystore daly.p12 -alias daly \
     -keyalg RSA -keysize 4096 -validity 10000 -dname "CN=Daly"
   base64 -w0 daly.p12 > daly.p12.b64          # macOS: base64 -i daly.p12 -o daly.p12.b64
   keytool -list -v -storetype PKCS12 -keystore daly.p12 -alias daly | grep SHA256
   ```

2. In **diesem** Repo (Settings → Secrets and variables → Actions → *Secrets*): `DALY_KEYSTORE_BASE64` (Inhalt von
   `daly.p12.b64`) und `DALY_KEYSTORE_PASSWORD`. Danach `daly.p12.b64` löschen.
3. In **AIndroid** und **iOS-Android-Kalender-sync** (Settings → Secrets and variables → Actions → *Variables*, kein
   Secret): `DALY_CERT_SHA256` = der SHA256-Fingerabdruck aus Schritt 1 (mit oder ohne Doppelpunkte).
4. Kalender und MailFinder einmal neu bauen lassen und installieren, dann Daly installieren.

Ohne `DALY_CERT_SHA256` steht in Kalender und MailFinder ein Platzhalter, und **keine** fremde App bekommt Zugriff.

## Stand

| Phase | Inhalt | Status |
|---|---|---|
| 0 | Analyse der bestehenden Apps | ✅ |
| 1 | Schnittstellen in Kalender (0.10.0) und MailFinder (0.23.0), Vertrag | ✅ |
| 2 | Grundgerüst: Room, Repositories, Screen „Heute“, regelbasiertes Briefing | offen |
| 3 | Mails, KI-Briefing, Worker, Benachrichtigung, Glance-Widget | offen |
| 4 | Wochenplan (Foto/PDF → OCR → Gemma → Prüfliste) | offen |
| 5 | Feinschliff | offen |

package briefing.contract

/*
 * Schnittstellen-Vertrag zwischen
 *   - Kalender   (dev.kalender.app,  Repo iOS-Android-Kalender-sync)
 *   - MailFinder (app.mailfinder,    Repo AIndroid)
 *   - Daly       (app.daly,          Repo Daly)
 *
 * Diese Datei liegt in allen drei Repos unverändert unter briefing/contract/BriefingContract.kt
 * (dazu in MailFinder und Daly die AIDL-Dateien ILocalAi.aidl und ILocalAiCallback.aidl).
 * Ein Test je Repo (BriefingContractTest) pinnt die Werte.
 *
 * Regeln:
 *  - Nur ergänzen. Was entfernt oder umgedeutet wird, erhöht CONTRACT_VERSION, und zwar in allen
 *    drei Repos im selben Schritt.
 *  - Keine Android-Abhängigkeiten (die Datei wird auch in reinen JVM-Modulen gebaut).
 *
 * Sicherheit: Alle Permissions sind `signature|knownSigner`. Die Apps haben verschiedene Schlüssel;
 * jede anbietende App erlaubt zusätzlich den Zertifikat-Fingerabdruck von Daly (android:knownCerts).
 * Andere Apps bekommen die Permissions nicht.
 *
 * Zeiten sind epoch ms. Ganztägige Termine: start = 00:00 des ersten Tages, end = 00:00 nach dem
 * letzten Tag (exklusiv), jeweils in der Zeitzone des Geräts.
 */
object BriefingContract {
    const val CONTRACT_VERSION = 1

    // --- Gemeinsam: jeder Provider hat content://<authority>/info mit genau einer Zeile ----------

    const val PATH_INFO = "info"
    const val COL_CONTRACT_VERSION = "contract_version"
    const val COL_APP_VERSION = "app_version"
    /** 1 = einsatzbereit (Kalender: angemeldet; MailFinder: Konto eingerichtet), sonst 0. */
    const val COL_READY = "ready"
    /** Kurzer deutscher Hinweis für die Anzeige, leer wenn alles in Ordnung ist. */
    const val COL_STATUS = "status"
    val INFO_COLUMNS = arrayOf(COL_CONTRACT_VERSION, COL_APP_VERSION, COL_READY, COL_STATUS)

    // --- Fehlercodes (call-Ergebnisse und KI-Callbacks) ------------------------------------------

    const val ERROR_NOT_READY = "not_ready"
    const val ERROR_OFFLINE = "offline"
    const val ERROR_INVALID_ARGUMENT = "invalid_argument"
    const val ERROR_NO_MODEL = "no_model"
    const val ERROR_FAILED = "failed"

    object Daly {
        const val PACKAGE = "app.daly"
        /** Herkunft von Terminen, die Daly anlegt (Kalender-Argument ARG_SOURCE). */
        const val SOURCE_TAG = "tagesbriefing"
    }

    // --- Kalender ---------------------------------------------------------------------------------

    object Calendar {
        const val PACKAGE = "dev.kalender.app"
        const val AUTHORITY = "dev.kalender.app.briefing"
        const val PERMISSION_READ = "dev.kalender.app.permission.READ_BRIEFING_DATA"
        const val PERMISSION_WRITE = "dev.kalender.app.permission.WRITE_EVENTS"

        /**
         * content://AUTHORITY/events?from=<ms>&to=<ms>: alle Termine, die [from, to) berühren, nach Beginn
         * sortiert. Quelle ist der lokale Stand der Kalender-App; ist er alt oder deckt den Zeitraum nicht ab,
         * lädt sie im Hintergrund nach und meldet danach notifyChange auf diese URI.
         * Höchstens 62 Tage pro Abfrage.
         */
        const val PATH_EVENTS = "events"
        const val PARAM_FROM = "from"
        const val PARAM_TO = "to"
        const val MAX_RANGE_DAYS = 62

        /** String: eindeutig je Vorkommen (Serien teilen sich sonst die id). */
        const val COL_ID = "id"
        const val COL_TITLE = "title"
        const val COL_START = "start"
        const val COL_END = "end"
        /** 0/1 */
        const val COL_ALL_DAY = "all_day"
        const val COL_LOCATION = "location"
        const val COL_CALENDAR_NAME = "calendar_name"
        /** URI, öffnet genau diesen Termin: Intent(ACTION_VIEW, uri).setPackage(PACKAGE) */
        const val COL_DEEP_LINK = "deep_link"
        val EVENT_COLUMNS = arrayOf(COL_ID, COL_TITLE, COL_START, COL_END, COL_ALL_DAY, COL_LOCATION, COL_CALENDAR_NAME, COL_DEEP_LINK)

        /** Cursor.getExtras() von events: Long, Zeitpunkt des lokalen Stands (0 = keiner). */
        const val EXTRA_FETCHED_AT = "fetched_at"
        /** Boolean: Der lokale Stand deckt den angefragten Zeitraum vollständig ab. */
        const val EXTRA_COVERED = "covered"
        /** Boolean: Es wird gerade nachgeladen, danach folgt notifyChange. */
        const val EXTRA_REFRESHING = "refreshing"

        /**
         * ContentResolver.call(uri, METHOD_CREATE_EVENT, null, extras), braucht PERMISSION_WRITE.
         * Argumente: ARG_TITLE (Pflicht), ARG_START/ARG_END (Long ms, Pflicht, end > start),
         * ARG_ALL_DAY (Boolean), ARG_LOCATION, ARG_NOTE, ARG_SOURCE.
         * Ergebnis: RESULT_OK (Boolean); bei Erfolg RESULT_ID, sonst RESULT_ERROR_CODE + RESULT_ERROR_MESSAGE.
         * Blockiert bis der Server antwortet: nicht auf dem Hauptthread aufrufen.
         */
        const val METHOD_CREATE_EVENT = "createEvent"
        /** Lädt den Standard-Zeitraum neu (asynchron); Ergebnis RESULT_OK. Braucht PERMISSION_READ. */
        const val METHOD_REFRESH = "refresh"
        const val ARG_TITLE = "title"
        const val ARG_START = "start"
        const val ARG_END = "end"
        const val ARG_ALL_DAY = "all_day"
        const val ARG_LOCATION = "location"
        const val ARG_NOTE = "note"
        const val ARG_SOURCE = "source"
        const val RESULT_OK = "ok"
        const val RESULT_ID = "id"
        const val RESULT_ERROR_CODE = "error_code"
        const val RESULT_ERROR_MESSAGE = "error_message"

        /** kalender://event/<id, URL-codiert> */
        const val DEEP_LINK_SCHEME = "kalender"
        const val DEEP_LINK_HOST = "event"
    }

    // --- MailFinder ------------------------------------------------------------------------------

    object Mail {
        const val PACKAGE = "app.mailfinder"
        const val AUTHORITY = "app.mailfinder.briefing"
        const val PERMISSION_READ = "app.mailfinder.permission.READ_BRIEFING_DATA"

        /**
         * content://AUTHORITY/mails?since=<ms>&limit=<n>: ungelesene Mails seit `since`, wichtige zuerst,
         * dann die neuesten übrigen; je Gruppe neueste zuerst. Nur Kurzinfos, kein Mailtext, keine Anhänge.
         * „Wichtig“ = in einem Projekt- oder Kundenordner, offener Regel-Treffer in „Zu prüfen“ oder eine
         * erkannte, nicht ausgeblendete Frist ab heute.
         */
        const val PATH_MAILS = "mails"
        const val PARAM_SINCE = "since"
        const val PARAM_LIMIT = "limit"
        const val DEFAULT_SINCE_MS = 48L * 60 * 60 * 1000
        const val DEFAULT_LIMIT = 5
        const val MAX_LIMIT = 20
        const val PREVIEW_MAX_CHARS = 200

        /** Long */
        const val COL_ID = "id"
        const val COL_FROM = "from"
        const val COL_SUBJECT = "subject"
        const val COL_PREVIEW = "preview"
        const val COL_RECEIVED_AT = "received_at"
        /** 0/1 */
        const val COL_IS_UNREAD = "is_unread"
        /** 0/1 */
        const val COL_IS_IMPORTANT = "is_important"
        /** URI, öffnet genau diese Mail: Intent(ACTION_VIEW, uri).setPackage(PACKAGE) */
        const val COL_DEEP_LINK = "deep_link"
        val MAIL_COLUMNS = arrayOf(COL_ID, COL_FROM, COL_SUBJECT, COL_PREVIEW, COL_RECEIVED_AT, COL_IS_UNREAD, COL_IS_IMPORTANT, COL_DEEP_LINK)

        /** mailfinder://message/<id> */
        const val DEEP_LINK_SCHEME = "mailfinder"
        const val DEEP_LINK_HOST = "message"
    }

    // --- Lokale KI (gebundener Service in MailFinder, AIDL briefing.contract.ILocalAi) -------------

    object Ai {
        const val PACKAGE = Mail.PACKAGE
        /** bindService(Intent(ACTION_BIND).setPackage(PACKAGE), …) */
        const val ACTION_BIND = "app.mailfinder.action.BIND_LOCAL_AI"
        const val PERMISSION_USE = "app.mailfinder.permission.USE_LOCAL_AI"

        /** ILocalAi.status(): Boolean, ein Modell ist importiert und aktiv. */
        const val STATUS_AVAILABLE = "available"
        /** ILocalAi.status(): Dateiname des aktiven Modells, z. B. "gemma-4-E2B-it.litertlm". */
        const val STATUS_MODEL_NAME = "model_name"
        /** ILocalAi.status(): Boolean, Texterkennung (PaddleOCR) ist verfügbar. */
        const val STATUS_OCR_AVAILABLE = "ocr_available"

        /** ILocalAi.generate(request): Systemanweisung (String). */
        const val REQ_SYSTEM = "system"
        /** ILocalAi.generate(request): Nutzertext (String, Pflicht). */
        const val REQ_USER = "user"
        /** ILocalAi.generate(request): optionales JSON-Schema; dann ist die Antwort garantiert JSON. */
        const val REQ_JSON_SCHEMA = "json_schema"
        /** Obergrenze für system + user (Zeichen). */
        const val MAX_PROMPT_CHARS = 12_000
        /** Obergrenze für Dateien an recognizeText (Bild JPG/PNG oder PDF, höchstens 3 Seiten). */
        const val MAX_FILE_BYTES = 20L * 1024 * 1024
        const val MAX_PDF_PAGES = 3
    }
}

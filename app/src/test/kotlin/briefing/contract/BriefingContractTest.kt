package briefing.contract

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pinnt den Vertrag (identisch in Kalender, MailFinder und Daly). Schlägt dieser Test fehl, wurde
 * BriefingContract.kt geändert: dann in allen drei Repos gleich ändern und hier mitziehen.
 */
class BriefingContractTest {
    @Test
    fun version() = assertEquals(1, BriefingContract.CONTRACT_VERSION)

    @Test
    fun calendar() {
        val c = BriefingContract.Calendar
        assertEquals("dev.kalender.app.briefing", c.AUTHORITY)
        assertEquals("dev.kalender.app.permission.READ_BRIEFING_DATA", c.PERMISSION_READ)
        assertEquals("dev.kalender.app.permission.WRITE_EVENTS", c.PERMISSION_WRITE)
        assertEquals(
            listOf("id", "title", "start", "end", "all_day", "location", "calendar_name", "deep_link"),
            c.EVENT_COLUMNS.toList(),
        )
        assertEquals("createEvent", c.METHOD_CREATE_EVENT)
        assertEquals(listOf("ok", "id", "error_code", "error_message"), listOf(c.RESULT_OK, c.RESULT_ID, c.RESULT_ERROR_CODE, c.RESULT_ERROR_MESSAGE))
    }

    @Test
    fun mail() {
        val m = BriefingContract.Mail
        assertEquals("app.mailfinder.briefing", m.AUTHORITY)
        assertEquals("app.mailfinder.permission.READ_BRIEFING_DATA", m.PERMISSION_READ)
        assertEquals(
            listOf("id", "from", "subject", "preview", "received_at", "is_unread", "is_important", "deep_link"),
            m.MAIL_COLUMNS.toList(),
        )
        assertEquals(200, m.PREVIEW_MAX_CHARS)
    }

    @Test
    fun ai() {
        val a = BriefingContract.Ai
        assertEquals("app.mailfinder.action.BIND_LOCAL_AI", a.ACTION_BIND)
        assertEquals("app.mailfinder.permission.USE_LOCAL_AI", a.PERMISSION_USE)
    }

    @Test
    fun info() = assertEquals(listOf("contract_version", "app_version", "ready", "status"), BriefingContract.INFO_COLUMNS.toList())
}

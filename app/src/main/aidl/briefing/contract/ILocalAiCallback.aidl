// Teil des Vertrags briefing.contract (identisch in MailFinder und Daly), siehe BriefingContract.Ai.
package briefing.contract;

interface ILocalAiCallback {
    oneway void onResult(String text);

    /** code: BriefingContract.ERROR_*; message: kurzer deutscher Text für die Anzeige. */
    oneway void onError(String code, String message);
}

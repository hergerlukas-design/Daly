// Teil des Vertrags briefing.contract (identisch in MailFinder und Daly), siehe BriefingContract.Ai.
package briefing.contract;

import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import briefing.contract.ILocalAiCallback;

interface ILocalAi {
    /** BriefingContract.CONTRACT_VERSION des Anbieters. */
    int contractVersion();

    /** STATUS_AVAILABLE, STATUS_MODEL_NAME, STATUS_OCR_AVAILABLE. Schnell, lädt kein Modell. */
    Bundle status();

    /** Text oder (mit REQ_JSON_SCHEMA) JSON erzeugen. Genau ein Callback-Aufruf. */
    oneway void generate(in Bundle request, ILocalAiCallback callback);

    /** Text in einem Bild (JPG/PNG) oder PDF erkennen (PaddleOCR, offline). Genau ein Callback-Aufruf. */
    oneway void recognizeText(in ParcelFileDescriptor file, ILocalAiCallback callback);
}

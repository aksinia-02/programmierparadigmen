package aufgabe4;

import org.jetbrains.annotations.NotNull;

// "Ein Messgerät oder ein Werkzeug, unabhängig davon, ob es Bestandteil eines Formicariums sein kann oder nicht.
// Nicht jedes Objekt von Instrument ist zusammen mit Formicarien verwendbar."
public interface Instrument {

    /**
     * Das Ergebnis eines Aufrufs der Methode quality besagt, ob das Objekt qualitativ
     * für die professionelle Verwendung ausgelegt ist, semiprofessionellen Ansprüchen genügt
     * oder eher nur für die gelegentliche Verwendung gedacht ist.
     * Postcondition: InstrumentQuality is not null
     */
    InstrumentQuality quality();
}

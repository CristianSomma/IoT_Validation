/**
 * Eccezione non controllata lanciata quando una lettura contiene un valore
 * sintatticamente corretto ma semanticamente non valido (ad esempio
 * un'umidità fuori dall'intervallo 0-100).
 */
public class InvalidReadingException extends IllegalArgumentException {

    /**
     * Crea l'eccezione con il messaggio che descrive il valore non valido.
     */
    public InvalidReadingException(String message) {
        super(message);
    }
}
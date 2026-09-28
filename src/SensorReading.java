import java.util.Optional;
import java.util.logging.Logger;

/**
 * Rappresenta una lettura di telemetria. Ogni campo è un wrapper: il valore
 * null indica che il sensore non ha fornito quel dato (o che era corrotto).
 */
public class SensorReading {

    private static final Logger LOGGER = Logger.getLogger(SensorReading.class.getName());

    private static final String FIELD_SEPARATOR = ";";
    private static final String KEY_VALUE_SEPARATOR = "=";
    private static final String KEY_TEMPERATURE = "temp";
    private static final String KEY_HUMIDITY = "umid";
    private static final String KEY_TIMESTAMP = "ts";
    private static final String KEY_LOW_BATTERY = "batt_low";
    private static final int MIN_HUMIDITY = 0;
    private static final int MAX_HUMIDITY = 100;

    private final Double temperature;
    private final Integer humidityPercentage;
    private final Long unixTimestamp;
    private final Boolean lowBattery;

    /**
     * Costruisce una lettura. Ogni parametro può essere null (dato assente).
     *
     * @throws InvalidReadingException se l'umidità non è compresa tra 0 e 100
     */
    public SensorReading(
            Double temperature,
            Integer humidityPercentage,
            Long unixTimestamp,
            Boolean lowBattery) {

        if (humidityPercentage != null
                && (humidityPercentage < MIN_HUMIDITY || humidityPercentage > MAX_HUMIDITY)) {
            throw new InvalidReadingException(
                    "Umidita' fuori range " + MIN_HUMIDITY + "-" + MAX_HUMIDITY + ": " + humidityPercentage);
        }
        this.temperature = temperature;
        this.humidityPercentage = humidityPercentage;
        this.unixTimestamp = unixTimestamp;
        this.lowBattery = lowBattery;
    }

    /**
     * Interpreta un pacchetto grezzo del tipo
     * "temp=23.5;umid=61;ts=1732000000;batt_low=false".
     * I campi mancanti o corrotti restano null e l'errore viene registrato nel log
     * senza interrompere la lettura degli altri campi. Restituisce Optional.empty()
     * se il pacchetto è nullo, vuoto o non contiene nessun campo utilizzabile.
     *
     * @throws InvalidReadingException se l'umidità è fuori dall'intervallo 0-100
     */
    public static Optional<SensorReading> parsePacket(String raw) {
        if (raw == null || raw.isBlank()) {
            LOGGER.warning("Pacchetto nullo o vuoto");
            return Optional.empty();
        }

        Double parsedTemperature = null;
        Integer parsedHumidity = null;
        Long parsedTimestamp = null;
        Boolean parsedLowBattery = null;

        for (String field : raw.split(FIELD_SEPARATOR)) {
            String[] keyAndValue = field.split(KEY_VALUE_SEPARATOR, 2);
            if (keyAndValue.length != 2) {
                LOGGER.warning("Campo malformato ignorato: '" + field + "'");
                continue;
            }
            String key = keyAndValue[0].trim();
            String value = keyAndValue[1].trim();

            switch (key) {
                case KEY_TEMPERATURE -> parsedTemperature = parseTemperature(value);
                case KEY_HUMIDITY -> parsedHumidity = parseHumidity(value);
                case KEY_TIMESTAMP -> parsedTimestamp = parseTimestamp(value);
                case KEY_LOW_BATTERY -> parsedLowBattery = parseLowBattery(value);
                default -> LOGGER.warning("Campo sconosciuto ignorato: '" + key + "'");
            }
        }

        if (parsedTemperature == null && parsedHumidity == null
                && parsedTimestamp == null && parsedLowBattery == null) {
            LOGGER.warning("Nessun campo utilizzabile nel pacchetto: '" + raw + "'");
            return Optional.empty();
        }
        return Optional.of(new SensorReading(parsedTemperature, parsedHumidity, parsedTimestamp, parsedLowBattery));
    }

    /**
     * Converte la temperatura; restituisce null se il testo non è un numero finito.
     */
    private static Double parseTemperature(String value) {
        try {
            Double parsedValue = Double.valueOf(value);
            if (parsedValue.isNaN() || parsedValue.isInfinite()) {
                throw new NumberFormatException("Valore non finito: " + value);
            }
            return parsedValue;
        } catch (NumberFormatException exception) {
            LOGGER.warning("Temperatura corrotta '" + value + "': " + exception.getMessage());
            return null;
        }
    }

    /**
     * Converte l'umidità; restituisce null se il testo non è un intero.
     * Il controllo del range è demandato al costruttore.
     */
    private static Integer parseHumidity(String value) {
        try {
            // Integer.parseInt restituirebbe un int primitivo, che andrebbe poi
            // autoboxato; Integer.valueOf restituisce direttamente un Integer
            // (sfruttando la cache -128..127), coerente con il tipo del campo.
            return Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            LOGGER.warning("Umidita' corrotta '" + value + "': " + exception.getMessage());
            return null;
        }
    }

    /**
     * Converte il timestamp Unix; restituisce null se il testo non è un intero long.
     */
    private static Long parseTimestamp(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            LOGGER.warning("Timestamp corrotto '" + value + "': " + exception.getMessage());
            return null;
        }
    }

    /**
     * Converte lo stato della batteria accettando solo "true" o "false"
     * (senza distinzione tra maiuscole e minuscole); altrimenti restituisce null.
     */
    private static Boolean parseLowBattery(String value) {
        // Boolean.valueOf("maybe") restituirebbe silenziosamente false: un dato
        // corrotto verrebbe scambiato per "batteria carica". Per questo il controllo è esplicito.
        if ("true".equalsIgnoreCase(value)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(value)) {
            return Boolean.FALSE;
        }
        LOGGER.warning("Stato batteria corrotto '" + value + "'");
        return null;
    }

    /**
     * Restituisce la temperatura, o null se assente.
     */
    public Double getTemperature() {
        return temperature;
    }

    /**
     * Restituisce l'umidità percentuale, o null se assente.
     */
    public Integer getHumidityPercentage() {
        return humidityPercentage;
    }

    /**
     * Restituisce il timestamp Unix, o null se assente.
     */
    public Long getUnixTimestamp() {
        return unixTimestamp;
    }

    /**
     * Restituisce lo stato della batteria (true = scarica), o null se assente.
     */
    public Boolean getLowBattery() {
        return lowBattery;
    }

    /**
     * Restituisce una rappresentazione testuale della lettura.
     */
    @Override
    public String toString() {
        return "SensorReading{temperature=" + temperature
                + ", humidityPercentage=" + humidityPercentage
                + ", unixTimestamp=" + unixTimestamp
                + ", lowBattery=" + lowBattery + "}";
    }
}
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.logging.Logger;

/**
 * Simula l'arrivo di pacchetti di telemetria e stampa un report riassuntivo.
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    /**
     * Elabora una serie di pacchetti (validi, corrotti, incompleti) e stampa
     * numero di letture valide, numero di errori e temperatura media.
     */
    public static void main(String[] args) {
        List<String> rawPackets = Arrays.asList(
                "temp=23.5;umid=61;ts=1732000000;batt_low=false",
                "temp=19.0;umid=55;ts=1732000060;batt_low=true",
                "umid=70;ts=1732000120",
                "temp=xx.xx;umid=48;ts=1732000180;batt_low=false",
                "temp=21.8;umid=150;ts=1732000240;batt_low=false",
                "",
                "temp=25.1;batt_low=maybe",
                "garbage",
                "temp=-4.5;umid=90;ts=abc;batt_low=true",
                null);

        List<SensorReading> validReadings = new ArrayList<>();
        int parsingErrors = 0;

        for (String rawPacket : rawPackets) {
            try {
                Optional<SensorReading> reading = SensorReading.parsePacket(rawPacket);
                if (reading.isPresent()) {
                    validReadings.add(reading.get());
                } else {
                    parsingErrors++;
                }
            } catch (InvalidReadingException exception) {
                LOGGER.warning("Lettura scartata: " + exception.getMessage());
                parsingErrors++;
            }
        }

        printReport(validReadings, parsingErrors);
    }

    /**
     * Stampa il report; la media considera solo le temperature non null.
     */
    private static void printReport(List<SensorReading> validReadings, int parsingErrors) {
        OptionalDouble averageTemperature = validReadings.stream()
                .map(SensorReading::getTemperature)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average();

        System.out.println("=== REPORT CENTRALINA ===");
        System.out.println("Letture valide: " + validReadings.size());
        System.out.println("Errori di parsing: " + parsingErrors);
        if (averageTemperature.isPresent()) {
            System.out.printf("Temperatura media: %.2f%n", averageTemperature.getAsDouble());
        } else {
            System.out.println("Temperatura media: non disponibile");
        }
    }
}
import java.util.Objects;

/**
 * Contiene due versioni del confronto tra percentuali di batteria:
 * una errata (operatore ==) e una corretta (Objects.equals).
 */
public final class BatteryComparison {

    private BatteryComparison() {}

    /**
     * confronta i valori con Objects.equals, che gestisce
     * anche il caso in cui uno o entrambi i riferimenti siano null.
     */
    public static boolean compareBattery(Integer firstBattery, Integer secondBattery) {
        return Objects.equals(firstBattery, secondBattery);
    }
}
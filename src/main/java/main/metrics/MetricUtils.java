package main.metrics;

import java.util.List;

/**
 * Utilitar pentru transformarea datelor calitative in indicatori numerici.
 * Permite calcularea mediilor, normalizarea scorurilor si evaluarea riscurilor.
 */
public final class MetricUtils {

    private static final double PROCENT_MAXIM = 100.0;
    private static final double NUMAR_CATEGORII = 3.0;

    private static final double LIMITA_RISC_SCAZUT = 25.0;
    private static final double LIMITA_RISC_MODERAT = 50.0;
    private static final double LIMITA_RISC_RIDICAT = 75.0;

    private static final int NIVEL_MINIM = 1;
    private static final int NIVEL_INTERMEDIAR = 2;
    private static final int NIVEL_RIDICAT = 3;
    private static final int NIVEL_MAXIM = 4;

    private static final int VALOARE_MICA = 1;
    private static final int VALOARE_MEDIE = 3;
    private static final int VALOARE_MARE = 6;
    private static final int VALOARE_FOARTE_MARE = 10;

    private MetricUtils() {
    }

    /**
     * Converteste frecventa unui bug intr-o valoare de la 1 la 4.
     *
     * @param frequency frecventa raportata
     * @return valoarea numerica
     */
    public static int getFrequencyValue(final String frequency) {
        if (frequency == null) {
            return 0;
        }
        return switch (frequency) {
            case "RARE" -> NIVEL_MINIM;
            case "OCCASIONAL" -> NIVEL_INTERMEDIAR;
            case "FREQUENT" -> NIVEL_RIDICAT;
            case "ALWAYS" -> NIVEL_MAXIM;
            default -> 0;
        };
    }

    /**
     * Transforma prioritatea intr-un punctaj de la 1 la 4.
     *
     * @param priority prioritatea stabilita
     * @return punctajul asociat
     */
    public static int getPriorityValue(final String priority) {
        if (priority == null) {
            return 0;
        }
        return switch (priority) {
            case "LOW" -> NIVEL_MINIM;
            case "MEDIUM" -> NIVEL_INTERMEDIAR;
            case "HIGH" -> NIVEL_RIDICAT;
            case "CRITICAL" -> NIVEL_MAXIM;
            default -> 0;
        };
    }

    /**
     * Atribuie un punctaj severitatii bug-ului.
     *
     * @param severity severitatea raportata
     * @return punctajul de la 1 la 3
     */
    public static int getSeverityValue(final String severity) {
        if (severity == null) {
            return 0;
        }
        return switch (severity) {
            case "MINOR" -> NIVEL_MINIM;
            case "MODERATE" -> NIVEL_INTERMEDIAR;
            case "SEVERE" -> NIVEL_RIDICAT;
            default -> 0;
        };
    }

    /**
     * Evalueaza importanta de business pe o scara de la 1 la 10.
     *
     * @param val marimea valorii (S, M, L, XL)
     * @return punctajul corespunzator
     */
    public static int getBusinessValue(final String val) {
        if (val == null) {
            return 0;
        }
        return switch (val) {
            case "S" -> VALOARE_MICA;
            case "M" -> VALOARE_MEDIE;
            case "L" -> VALOARE_MARE;
            case "XL" -> VALOARE_FOARTE_MARE;
            default -> 0;
        };
    }

    /**
     * Evalueaza nivelul cererii din partea clientilor.
     *
     * @param demand intensitatea cererii
     * @return punctajul de la 1 la 10
     */
    public static int getCustomerDemandValue(final String demand) {
        if (demand == null) {
            return 0;
        }
        return switch (demand) {
            case "LOW" -> VALOARE_MICA;
            case "MEDIUM" -> VALOARE_MEDIE;
            case "HIGH" -> VALOARE_MARE;
            case "VERY_HIGH" -> VALOARE_FOARTE_MARE;
            default -> 0;
        };
    }

    /**
     * Incadreaza un scor brut intr-un format procentual.
     *
     * @param baseScore scorul initial
     * @param maxValue valoarea de referinta
     * @return procentul obtinut (max 100)
     */
    public static double normalizeScore(final double baseScore, final double maxValue) {
        if (maxValue == 0) {
            return 0.0;
        }
        double result = (baseScore * PROCENT_MAXIM) / maxValue;
        return Math.min(PROCENT_MAXIM, result);
    }

    /**
     * Calculeaza media pentru o lista de valori de impact.
     *
     * @param scores lista scorurilor
     * @return media rezultatelor
     */
    public static double calculateAverageImpact(final List<Double> scores) {
        return scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    /**
     * Calculeaza distributia medie pe cele trei tipuri de tichete.
     */
    public static double averageResolvedTicketType(final int bug, final int feature, final int ui) {
        return (bug + feature + ui) / NUMAR_CATEGORII;
    }

    /**
     * Calculeaza deviatia standard pentru categoriile de tichete.
     */
    public static double standardDeviation(final int bug, final int feature, final int ui) {
        double mean = averageResolvedTicketType(bug, feature, ui);
        double variance = (Math.pow(bug - mean, 2) + Math.pow(feature - mean, 2)
                + Math.pow(ui - mean, 2)) / NUMAR_CATEGORII;
        return Math.sqrt(variance);
    }

    /**
     * Calculeaza factorul de diversitate al activitatii.
     */
    public static double ticketDiversityFactor(final int bug, final int feature, final int ui) {
        double mean = averageResolvedTicketType(bug, feature, ui);
        if (mean == 0.0) {
            return 0.0;
        }
        double std = standardDeviation(bug, feature, ui);
        return std / mean;
    }

    /**
     * Determina nivelul de risc in functie de scorul final.
     *
     * @param score scorul calculat
     * @return eticheta riscului
     */
    public static String getRiskLabel(final double score) {
        if (score < LIMITA_RISC_SCAZUT) {
            return "NEGLIGIBLE";
        }
        if (score < LIMITA_RISC_MODERAT) {
            return "MODERATE";
        }
        if (score < LIMITA_RISC_RIDICAT) {
            return "SIGNIFICANT";
        }
        return "MAJOR";
    }
}

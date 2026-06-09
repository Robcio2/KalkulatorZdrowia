package pl.edu.psw.zdrowie.logic;

public class CalculatorService {

    public enum ActivityLevel {
        BRAK(1.2, "Brak aktywności"),
        NISKA(1.375, "Niska aktywność"),
        SREDNIA(1.55, "Średnia aktywność"),
        WYSOKA(1.725, "Wysoka aktywność"),
        BARDZO_WYSOKA(1.9, "Bardzo wysoka aktywność sportowa");

        private final double multiplier;
        private final String description;

        ActivityLevel(double multiplier, String description) {
            this.multiplier = multiplier;
            this.description = description;
        }

        public double getMultiplier() { return multiplier; }
        public String getDescription() { return description; }

        @Override
        public String toString() { return description; }
    }

    public static double calculateBMI(double weightKg, double heightCm) {
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }

    public static String getBMICategory(double bmi) {
        if (bmi < 18.5) return "Niedowaga (Ryzyko: Zwiększone)";
        if (bmi < 25.0) return "Wartość prawidłowa (Ryzyko: Minimalne)";
        if (bmi < 30.0) return "Nadwaga (Ryzyko: Umiarkowane)";
        return "Otyłość (Ryzyko: Wysokie)";
    }

    public static double calculateBMR(boolean isMale, double weightKg, double heightCm, int age) {
        // Wzór Mifflina-St Jeora
        double base = (10 * weightKg) + (6.25 * heightCm) - (5 * age);
        return isMale ? base + 5 : base - 161;
    }

    public static double calculateTDEE(double bmr, ActivityLevel activityLevel) {
        return bmr * activityLevel.getMultiplier();
    }

    public static double calculateWaterNeeds(double weightKg){
        return (weightKg * 30.0) / 1000.0;
    }

    public static String getMacroNutrients(double tdee){
        double carbsKcal = tdee * 0.50;
        double proteinKcal = tdee * 0.20;
        double fatKcal = tdee * 0.30;

        double carbsGrams = carbsKcal / 4.0;
        double proteinGrams = proteinKcal / 4.0;
        double fatGrams = fatKcal / 9.0;

        return String.format("Węglowodany: %.0f g | Białoko: %.0f g | Tłuszcze: %.0f g",
                carbsGrams, proteinGrams, fatGrams);
    }
}
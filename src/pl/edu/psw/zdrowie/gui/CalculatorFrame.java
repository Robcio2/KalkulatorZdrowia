package pl.edu.psw.zdrowie.gui;

import pl.edu.psw.zdrowie.logic.CalculatorService;
import pl.edu.psw.zdrowie.logic.CalculatorService.ActivityLevel;
import pl.edu.psw.zdrowie.logic.FileService;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class CalculatorFrame extends JFrame {

    private JRadioButton maleRadio;
    private JRadioButton femaleRadio;
    private JTextField ageField;
    private JTextField heightField;
    private JTextField weightField;
    private JComboBox<ActivityLevel> activityComboBox;
    private JTextArea resultsArea;

    public CalculatorFrame() {
        setTitle("Kalkulator Zdrowia (BMI & BMR)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initComponents();
    }

    private void initComponents() {
        // --- Panel Danych (Siatka / GridLayout) ---
        JPanel dataPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        dataPanel.setBorder(BorderFactory.createTitledBorder("Dane personalne"));

        dataPanel.add(new JLabel("Płeć:"));
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        maleRadio = new JRadioButton("Mężczyzna");
        femaleRadio = new JRadioButton("Kobieta");
        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(maleRadio);
        genderGroup.add(femaleRadio);
        genderPanel.add(maleRadio);
        genderPanel.add(femaleRadio);
        dataPanel.add(genderPanel);

        dataPanel.add(new JLabel("Wiek (1-120 lat):"));
        ageField = new JTextField();
        dataPanel.add(ageField);

        dataPanel.add(new JLabel("Wzrost (cm):"));
        heightField = new JTextField();
        dataPanel.add(heightField);

        dataPanel.add(new JLabel("Waga (kg):"));
        weightField = new JTextField();
        dataPanel.add(weightField);

        dataPanel.add(new JLabel("Aktywność fizyczna (PAL):"));
        activityComboBox = new JComboBox<>(ActivityLevel.values());
        dataPanel.add(activityComboBox);

        add(dataPanel, BorderLayout.NORTH);

        // --- Panel Wyników ---
        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(resultsArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Wyniki"));
        add(scrollPane, BorderLayout.CENTER);

        // --- Panel Akcji ---
        JPanel actionPanel = new JPanel(new FlowLayout());

        JButton calculateBtn = new JButton("Oblicz");
        JButton clearBtn = new JButton("Wyczyść");
        JButton saveBtn = new JButton("Zapisz wynik");

        calculateBtn.addActionListener(e -> calculateResults());
        clearBtn.addActionListener(e -> clearFields(genderGroup));
        saveBtn.addActionListener(e -> saveCurrentResults());

        actionPanel.add(calculateBtn);
        actionPanel.add(clearBtn);
        actionPanel.add(saveBtn);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void calculateResults() {
        try {
            // Walidacja płci
            if (!maleRadio.isSelected() && !femaleRadio.isSelected()) {
                throw new IllegalArgumentException("Proszę wybrać płeć.");
            }
            boolean isMale = maleRadio.isSelected();

            // Walidacja wieku
            int age;
            try {
                age = Integer.parseInt(ageField.getText().trim());
                if (age < 1 || age > 120) {
                    throw new IllegalArgumentException("Wiek musi być liczbą całkowitą od 1 do 120.");
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Błędny format wieku (wymagana liczba całkowita).");
            }

            // Walidacja wzrostu
            double height = parseDoubleInput(heightField.getText().trim(), "wzrostu");
            if (height <= 0) throw new IllegalArgumentException("Wzrost musi być większy od 0.");

            // Walidacja wagi
            double weight = parseDoubleInput(weightField.getText().trim(), "wagi");
            if (weight <= 0) throw new IllegalArgumentException("Waga musi być większa od 0.");

            // Aktywność
            ActivityLevel pal = (ActivityLevel) activityComboBox.getSelectedItem();

            // Obliczenia
            double bmi = CalculatorService.calculateBMI(weight, height);
            String bmiCategory = CalculatorService.getBMICategory(bmi);
            double bmr = CalculatorService.calculateBMR(isMale, weight, height, age);
            double tdee = CalculatorService.calculateTDEE(bmr, pal);
            double waterLiters = CalculatorService.calculateWaterNeeds(weight);
            String macroInfo = CalculatorService.getMacroNutrients(tdee);

            String resultText = String.format("""
                    Zestawienie wyników:
                    -------------------------------------
                    BMI: %.2f
                    Kategoria WHO: %s
                    
                    BMR (Spoczynkowy wydatek): %.2f kcal
                    TDEE (Całkowite zapotrzebowanie): %.2f kcal
                    
                    Rozkład makroskładników:
                    %s
                      
                    Zalecane dzienne spożycie wody: %.2f litra
                    """, bmi, bmiCategory, bmr, tdee, macroInfo, waterLiters);

            resultsArea.setText(resultText);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Błąd walidacji danych", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields(ButtonGroup genderGroup) {
        genderGroup.clearSelection();
        ageField.setText("");
        heightField.setText("");
        weightField.setText("");
        activityComboBox.setSelectedIndex(0);
        resultsArea.setText("");
    }

    private void saveCurrentResults() {
        String results = resultsArea.getText();
        if (results == null || results.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Brak wyników do zapisania! Najpierw dokonaj obliczeń.", "Uwaga", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            FileService.saveResult(results);
            JOptionPane.showMessageDialog(this, "Pomyślnie zapisano do pliku historia_pomiarow.txt", "Sukces", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Błąd podczas zapisu do pliku: " + ex.getMessage(), "Błąd I/O", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double parseDoubleInput(String input, String fieldName) {
        try {
            // Obsługa zarówno kropki jak i przecinka jako separatora dziesiętnego
            input = input.replace(",", ".");
            return Double.parseDouble(input);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Błędny format w polu " + fieldName + ".");
        }
    }
}
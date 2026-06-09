package pl.edu.psw.zdrowie.gui;

import pl.edu.psw.zdrowie.logic.CalculatorService;
import pl.edu.psw.zdrowie.logic.CalculatorService.ActivityLevel;
import pl.edu.psw.zdrowie.logic.FileService;
import pl.edu.psw.zdrowie.logic.CalculatorService.DietGoal;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class CalculatorFrame extends JFrame {

    private JTextField nameField;
    private JRadioButton maleRadio;
    private JRadioButton femaleRadio;
    private JTextField ageField;
    private JTextField heightField;
    private JTextField weightField;
    private JComboBox<ActivityLevel> activityComboBox;
    private JComboBox<DietGoal> goalComboBox;
    private JTextArea resultsArea;

    public CalculatorFrame() {
        setTitle("Kalkulator Zdrowia");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        createMenuBar();
        initComponents();
    }

    private void initComponents() {
        // Panel Danych
        JPanel dataPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        dataPanel.setBorder(BorderFactory.createTitledBorder("Dane personalne"));

        dataPanel.add(new JLabel("Imie:"));
        nameField = new JTextField();
        dataPanel.add(nameField);

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

        dataPanel.add(new JLabel("Wiek (lat):"));
        ageField = new JTextField();
        dataPanel.add(ageField);

        dataPanel.add(new JLabel("Wzrost (cm):"));
        heightField = new JTextField();
        dataPanel.add(heightField);

        dataPanel.add(new JLabel("Waga (kg):"));
        weightField = new JTextField();
        dataPanel.add(weightField);

        dataPanel.add(new JLabel("Aktywność fizyczna:"));
        activityComboBox = new JComboBox<>(ActivityLevel.values());
        dataPanel.add(activityComboBox);

        dataPanel.add(new JLabel("Cel diety:"));
        goalComboBox = new JComboBox<>(DietGoal.values());
        dataPanel.add(goalComboBox);

        add(dataPanel, BorderLayout.NORTH);

        // Panel Wyników
        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(resultsArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Wyniki"));
        add(scrollPane, BorderLayout.CENTER);

        // Panel Akcji
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
        try {  // imie blank = intognito
            String name = nameField.getText();
            if(name.isEmpty()){
                name = "Nieznajomy";
            }

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
            if (height <= 20 || height >= 300) throw new IllegalArgumentException("Wzrost musi być w przedziale 50-300.");

            // Walidacja wagi
            double weight = parseDoubleInput(weightField.getText().trim(), "wagi");
            if (weight <= 2 || weight >= 500) throw new IllegalArgumentException("Waga musi być w przedziale 2-500.");

            // Aktywność
            ActivityLevel pal = (ActivityLevel) activityComboBox.getSelectedItem();

            // Obliczenia
            double bmi = CalculatorService.calculateBMI(weight, height);
            String bmiCategory = CalculatorService.getBMICategory(bmi);
            double bmr = CalculatorService.calculateBMR(isMale, weight, height, age);
            double tdee = CalculatorService.calculateTDEE(bmr, pal);
            double waterLiters = CalculatorService.calculateWaterNeeds(weight);
            DietGoal goal = (DietGoal) goalComboBox.getSelectedItem();
            double targetCalories = CalculatorService.calculateTargetCalories(tdee, goal);
            String macroInfo = CalculatorService.getMacroNutrients(targetCalories);

            String resultText = String.format("""
                    Zestawienie wyników dla: %s
                    -------------------------------------
                    BMI: %.2f
                    Kategoria WHO: %s
                    
                    BMR (Spoczynkowy wydatek): %.2f kcal
                    TDEE (Całkowite zapotrzebowanie): %.2f kcal
                    
                    Twój cel: %s
                    Zalecane kalorie względem celu: %.2f kcal
                    
                    Rozkład makroskładników:
                    %s
                      
                    Zalecane dzienne spożycie wody: %.2f litra
                    """, name, bmi, bmiCategory, bmr, tdee, goal.toString(), targetCalories, macroInfo, waterLiters);

            resultsArea.setText(resultText);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Błąd walidacji danych", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields(ButtonGroup genderGroup) {
        nameField.setText("");
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
    private void showAboutDialog() {
        String aboutText = """
                Kalkulator Zdrowia (BMI & BMR) v1.0
                
                Autor: Robert Tymoszuk
                Projekt zaliczeniowy
                
                Funkcje programu:
                - Obliczanie BMI oraz BMR
                - Wyliczanie TDEE (całkowitego zapotrzebowania)
                - Rozkład makroskładników wg wybranego celu diety
                - Obliczanie zapotrzebowania na wodę
                - Zapis wyników do pliku
                - Cel deity
                """;

        JOptionPane.showMessageDialog(this, aboutText, "O programie", JOptionPane.INFORMATION_MESSAGE);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Plik");

        JMenuItem exitItem = new JMenuItem("Zakończ");
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu("Pomoc");

        JMenuItem aboutItem = new JMenuItem("O programie");
        aboutItem.addActionListener(e -> showAboutDialog());

        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

}
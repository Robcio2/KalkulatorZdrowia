package pl.edu.psw.zdrowie.logic;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileService {
    private static final String FILE_NAME = "historia_pomiarow.txt";

    public static void saveResult(String resultData) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timestamp = LocalDateTime.now().format(dtf);

            writer.write("--- Pomiar z dnia: " + timestamp + " ---\n");
            writer.write(resultData);
            writer.write("\n\n");
        }
    }
}
package com.golexer;

import com.golexer.driver.Driver;
import com.golexer.error.Error;
import com.golexer.scanner.Scanner;
import java.util.*;

public class Main {
    private static int totalTokens = 0;
    private static Map<Scanner.Lex, Integer> tokenCount = new HashMap<>();

    public static void main(String[] args) {
        System.out.println(" Лексический анализатор языка Go \n");

        String[] filesToAnalyze;
        if (args.length == 0) {
            System.out.println("Используем тестовые файлы:");
            filesToAnalyze = new String[]{"test1.go", "test2.go", "test3.go"};
            for (String f : filesToAnalyze) {
                System.out.println("  - " + f);
            }
            System.out.println();
        } else {
            filesToAnalyze = args;
        }

        Error error = new Error();

        for (String filename : filesToAnalyze) {
            System.out.println("Анализ файла: " + filename);

            java.io.File file = new java.io.File(filename);
            if (!file.exists()) {
                System.err.println("  ОШИБКА: Файл не найден - " + filename);
                System.err.println("  Убедитесь, что файл существует в папке: " + new java.io.File(".").getAbsolutePath());
                System.out.println();
                continue;
            }

            try {
                Driver driver = new Driver();
                driver.resetText(filename);
                Scanner scanner = new Scanner(driver, error);
                analyzeFile(scanner);
                System.out.println("  Найдено лексем: " + totalTokens);
            } catch (Exception e) {
                System.err.println("  Ошибка при анализе: " + e.getMessage());
            }
            System.out.println();
        }

        if (totalTokens > 0) {
            printResults();
        } else {
            System.err.println("\nНет данных для анализа. Проверьте, что файлы существуют.");
            System.err.println("Текущая директория: " + new java.io.File(".").getAbsolutePath());
        }
    }

    private static void analyzeFile(Scanner scanner) {
        Scanner.Lex lex;
        do {
            lex = scanner.lex;

            if (lex != Scanner.Lex.COMMENT && lex != Scanner.Lex.EOT) {
                totalTokens++;
                tokenCount.put(lex, tokenCount.getOrDefault(lex, 0) + 1);
            }

            scanner.nextLex();
        } while (lex != Scanner.Lex.EOT);
    }

    private static void printResults() {
        System.out.println("РЕЗУЛЬТАТЫ ЛЕКСИЧЕСКОГО АНАЛИЗА");

        System.out.println("1. Общее количество лексем: " + totalTokens + "\n");

        System.out.println("2. Частота лексем:");

        List<Map.Entry<Scanner.Lex, Integer>> list = new ArrayList<>(tokenCount.entrySet());
        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        for (Map.Entry<Scanner.Lex, Integer> entry : list) {
            double percent = (entry.getValue() * 100.0) / totalTokens;
            String name = entry.getKey().toString();
            // Делаем названия более читаемыми
            if (name.equals("NAME")) name = "ИДЕНТИФИКАТОР";
            if (name.equals("NUM")) name = "ЧИСЛО";
            if (name.equals("STRING")) name = "СТРОКА";
            if (name.equals("EOT")) continue;
            System.out.printf("   %-20s: %5d (%5.2f%%)\n", name, entry.getValue(), percent);
        }

    }
}
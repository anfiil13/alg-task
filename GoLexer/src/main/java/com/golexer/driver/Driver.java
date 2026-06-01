package com.golexer.driver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Driver {
    private char[] inputChain;
    private int position;
    private int line;
    private char currentChar;
    
    public static final char chSpace = ' ';
    public static final char chTab = '\t';
    public static final char chEOL = '\n';
    public static final char chEOF = '\0';
    
    public int getPosition() { return position; }
    public int getLine() { return line; }
    public char getCurrentChar() { return currentChar; }
    
    public Driver() {}
    
    public void resetText(String path) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(path)));
            inputChain = content.toCharArray();
            position = 0;
            line = 1;
            nextChar();
        } catch (IOException e) {
            System.err.println("Не удалось открыть файл: " + path);
            System.exit(1);
        }
    }
    
    public void nextChar() {
        if (position < inputChain.length) {
            currentChar = inputChain[position];
            position++;
            
            if (currentChar == '\n') {
                line++;
                currentChar = chEOL;
            }
        } else {
            currentChar = chEOF;
        }
    }
    
    public char peek() {
        if (position < inputChain.length) {
            return inputChain[position];
        }
        return chEOF;
    }
}
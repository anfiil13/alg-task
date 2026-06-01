package com.golexer.error;

import com.golexer.driver.Driver;
import com.golexer.scanner.Scanner;

public class Error {
    
    public void error(String msg) {
        System.err.println("ERROR !!! " + msg);
        System.exit(1);
    }
    
    private void skip(int position, Driver driver) {
        while (driver.getCurrentChar() != Driver.chEOF && driver.getCurrentChar() != Driver.chEOL) {
            driver.nextChar();
        }
        if (position > 0) {
            System.out.print(String.format("%" + position + "s", " "));
        }
        System.out.println("^");
    }
    
    public void lexError(String msg, Driver driver) {
        skip(driver.getPosition(), driver);
        System.err.println(msg);
        System.exit(1);
    }
    
    public void syntaxError(String msg, Scanner scanner) {
        skip(scanner.getLexPosition(), scanner.getDriver());
        System.err.println("Ожидается: " + msg);
        System.exit(1);
    }
}
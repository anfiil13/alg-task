package com.golexer.scanner;

import com.golexer.driver.Driver;
import com.golexer.error.Error;
import java.util.HashMap;
import java.util.Map;

public class Scanner {
    public enum Lex {
        NONE, NAME, NUM, STRING, COMMENT, EOT,
        PACKAGE, IMPORT, FUNC, VAR, CONST, TYPE,
        IF, ELSE, SWITCH, CASE, DEFAULT, FOR, RANGE,
        BREAK, CONTINUE, GOTO, RETURN, DEFER, GO,
        SELECT, CHAN, STRUCT, INTERFACE, MAP,
        ADD, SUB, MUL, QUO, REM,
        AND, OR, XOR, SHL, SHR, AND_NOT,
        LAND, LOR,
        EQL, NEQ, LSS, LEQ, GTR, GEQ,
        ASSIGN, DEFINE, SEND,
        LPAREN, RPAREN, LBRACK, RBRACK, LBRACE, RBRACE,
        COMMA, SEMICOLON, COLON, DOT, ELLIPSIS
    }
    
    public Lex lex;
    public static String nameValue;
    public static int numValue;
    private int lexPosition;
    
    private Driver driver;
    private Error error;
    
    private static final Map<String, Lex> keywords = new HashMap<>();
    
    static {
        keywords.put("package", Lex.PACKAGE);
        keywords.put("import", Lex.IMPORT);
        keywords.put("func", Lex.FUNC);
        keywords.put("var", Lex.VAR);
        keywords.put("const", Lex.CONST);
        keywords.put("type", Lex.TYPE);
        keywords.put("if", Lex.IF);
        keywords.put("else", Lex.ELSE);
        keywords.put("switch", Lex.SWITCH);
        keywords.put("case", Lex.CASE);
        keywords.put("default", Lex.DEFAULT);
        keywords.put("for", Lex.FOR);
        keywords.put("range", Lex.RANGE);
        keywords.put("break", Lex.BREAK);
        keywords.put("continue", Lex.CONTINUE);
        keywords.put("goto", Lex.GOTO);
        keywords.put("return", Lex.RETURN);
        keywords.put("defer", Lex.DEFER);
        keywords.put("go", Lex.GO);
        keywords.put("select", Lex.SELECT);
        keywords.put("chan", Lex.CHAN);
        keywords.put("struct", Lex.STRUCT);
        keywords.put("interface", Lex.INTERFACE);
        keywords.put("map", Lex.MAP);
    }
    
    public Scanner(Driver driver, Error error) {
        this.driver = driver;
        this.error = error;
        this.lex = Lex.NONE;
        nameValue = "";
        numValue = 0;
        nextLex();
    }
    
    public Driver getDriver() { return driver; }
    public int getLexPosition() { return lexPosition; }
    
    public void nextLex() {
        while (driver.getCurrentChar() == Driver.chSpace ||
               driver.getCurrentChar() == Driver.chTab ||
               driver.getCurrentChar() == Driver.chEOL) {
            driver.nextChar();
        }
        
        lexPosition = driver.getPosition();
        
        char ch = driver.getCurrentChar();
        
        if (ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z' || ch == '_') {
            scanName();
        }
        else if (ch >= '0' && ch <= '9') {
            scanNum();
        }
        else if (ch == '"') {
            scanString();
        }
        else if (ch == '`') {
            scanRawString();
        }
        else if (ch == '/') {
            driver.nextChar();
            if (driver.getCurrentChar() == '/') {
                skipLineComment();
                nextLex();
            } else if (driver.getCurrentChar() == '*') {
                skipBlockComment();
                nextLex();
            } else {
                lex = Lex.QUO;
            }
        }
        else if (ch == Driver.chEOF) {
            lex = Lex.EOT;
        }
        else {
            scanOperator();
        }
    }
    
    private void scanName() {
        nameValue = "";
        char ch = driver.getCurrentChar();
        while ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || 
               (ch >= '0' && ch <= '9') || ch == '_') {
            nameValue += ch;
            driver.nextChar();
            ch = driver.getCurrentChar();
        }
        lex = keywords.getOrDefault(nameValue, Lex.NAME);
    }
    
    private void scanNum() {
        numValue = 0;
        char ch = driver.getCurrentChar();
        while (ch >= '0' && ch <= '9') {
            numValue = numValue * 10 + (ch - '0');
            driver.nextChar();
            ch = driver.getCurrentChar();
        }
        lex = Lex.NUM;
    }
    
    private void scanString() {
        nameValue = "\"";
        driver.nextChar();
        char ch = driver.getCurrentChar();
        while (ch != '"' && ch != Driver.chEOF && ch != Driver.chEOL) {
            if (ch == '\\') {
                nameValue += ch;
                driver.nextChar();
                ch = driver.getCurrentChar();
            }
            nameValue += ch;
            driver.nextChar();
            ch = driver.getCurrentChar();
        }
        if (ch == '"') {
            nameValue += ch;
            driver.nextChar();
        } else {
            error.lexError("Незакрытая строка", driver);
        }
        lex = Lex.STRING;
    }
    
    private void scanRawString() {
        nameValue = "`";
        driver.nextChar();
        char ch = driver.getCurrentChar();
        while (ch != '`' && ch != Driver.chEOF) {
            nameValue += ch;
            driver.nextChar();
            ch = driver.getCurrentChar();
        }
        if (ch == '`') {
            nameValue += ch;
            driver.nextChar();
        } else {
            error.lexError("Незакрытая сырая строка", driver);
        }
        lex = Lex.STRING;
    }
    
    private void skipLineComment() {
        while (driver.getCurrentChar() != Driver.chEOL && driver.getCurrentChar() != Driver.chEOF) {
            driver.nextChar();
        }
    }
    
    private void skipBlockComment() {
        driver.nextChar();
        while (true) {
            if (driver.getCurrentChar() == Driver.chEOF) {
                error.lexError("Незакрытый комментарий", driver);
            }
            if (driver.getCurrentChar() == '*' && driver.peek() == '/') {
                driver.nextChar();
                driver.nextChar();
                break;
            }
            driver.nextChar();
        }
    }
    
    private void scanOperator() {
        char ch = driver.getCurrentChar();
        driver.nextChar();
        
        switch (ch) {
            case '+': lex = Lex.ADD; break;
            case '-': lex = Lex.SUB; break;
            case '*': lex = Lex.MUL; break;
            case '%': lex = Lex.REM; break;
            case '&': 
                if (driver.getCurrentChar() == '&') { driver.nextChar(); lex = Lex.LAND; }
                else lex = Lex.AND;
                break;
            case '|':
                if (driver.getCurrentChar() == '|') { driver.nextChar(); lex = Lex.LOR; }
                else lex = Lex.OR;
                break;
            case '^': lex = Lex.XOR; break;
            case '<':
                if (driver.getCurrentChar() == '<') { driver.nextChar(); lex = Lex.SHL; }
                else if (driver.getCurrentChar() == '=') { driver.nextChar(); lex = Lex.LEQ; }
                else if (driver.getCurrentChar() == '-') { driver.nextChar(); lex = Lex.SEND; }
                else lex = Lex.LSS;
                break;
            case '>':
                if (driver.getCurrentChar() == '>') { driver.nextChar(); lex = Lex.SHR; }
                else if (driver.getCurrentChar() == '=') { driver.nextChar(); lex = Lex.GEQ; }
                else lex = Lex.GTR;
                break;
            case '=':
                if (driver.getCurrentChar() == '=') { driver.nextChar(); lex = Lex.EQL; }
                else lex = Lex.ASSIGN;
                break;
            case '!':
                if (driver.getCurrentChar() == '=') { driver.nextChar(); lex = Lex.NEQ; }
                else error.lexError("Неизвестный оператор '!'", driver);
                break;
            case ':':
                if (driver.getCurrentChar() == '=') { driver.nextChar(); lex = Lex.DEFINE; }
                else lex = Lex.COLON;
                break;
            case '.': 
                if (driver.getCurrentChar() == '.' && driver.peek() == '.') {
                    driver.nextChar(); driver.nextChar(); lex = Lex.ELLIPSIS;
                } else lex = Lex.DOT;
                break;
            case ',': lex = Lex.COMMA; break;
            case ';': lex = Lex.SEMICOLON; break;
            case '(': lex = Lex.LPAREN; break;
            case ')': lex = Lex.RPAREN; break;
            case '[': lex = Lex.LBRACK; break;
            case ']': lex = Lex.RBRACK; break;
            case '{': lex = Lex.LBRACE; break;
            case '}': lex = Lex.RBRACE; break;
            default: error.lexError("Недопустимый символ: " + ch, driver);
        }
    }
}
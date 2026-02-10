package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    TokenType.AND);
        keywords.put("class",  TokenType.CLASS);
        keywords.put("else",   TokenType.ELSE);
        keywords.put("false",  TokenType.FALSE);
        keywords.put("for",    TokenType.FOR);
        keywords.put("fun",    TokenType.FUN);
        keywords.put("if",     TokenType.IF);
        keywords.put("nil",    TokenType.NIL);
        keywords.put("or",     TokenType.OR);
        keywords.put("print",  TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super",  TokenType.SUPER);
        keywords.put("this",   TokenType.THIS);
        keywords.put("true",   TokenType.TRUE);
        keywords.put("var",    TokenType.VAR);
        keywords.put("while",  TokenType.WHILE);
    }

    public Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(':
                this.addToken(TokenType.LEFT_PAREN);
                break;
            case ')':
                this.addToken(TokenType.RIGHT_PAREN);
                break;
            case '{':
                this.addToken(TokenType.LEFT_BRACE);
                break;
            case '}':
                this.addToken(TokenType.RIGHT_BRACE);
                break;
            case ',':
                this.addToken(TokenType.COMMA);
                break;
            case '.':
                this.addToken(TokenType.DOT);
                break;
            case '-':
                this.addToken(TokenType.MINUS);
                break;
            case '+':
                this.addToken(TokenType.PLUS);
                break;
            case ';':
                this.addToken(TokenType.SEMICOLON);
                break;
            case '*':
                this.addToken(TokenType.STAR);
                break;
            case '!':
                this.addToken(this.macth('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                this.addToken(this.macth('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '>':
                this.addToken(this.macth('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '<':
                this.addToken(this.macth('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '/':
                if (this.macth('/')) {
                    while (this.peek() != '\n' && !isAtEnd() ) this.advance();
                } else {
                    this.addToken(TokenType.SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespaces
                break;
            case '\n':
                this.line++;
                break;
            case '"': this.string(); break;
            default:
                if (this.isDigit(c)) {
                    this.number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(this.line, "Unexpected character.");
                }
                break;
        }
    }

    private char advance() {
        return this.source.charAt(this.current++);
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return this.source.charAt(this.current);
    }

    private char peekNext() {
        if (this.current + 1 >= this.source.length()) return '\0';
        return this.source.charAt(this.current + 1);
    }

    private void addToken(TokenType type) {
        this.addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(this.start, this.current);
        tokens.add(new Token(type, text, literal, this.line));
    }

    private boolean macth(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(this.current) != expected) return false;

        this.current++;
        return true;
    }

    private void string() {
        while (this.peek() != '"' && !this.isAtEnd()) {
            if (this.peek() == '\n') this.line++;
            this.advance();
        }

        if (this.isAtEnd()) {
            Lox.error(this.line, "Unterminated string.");
            return;
        }

        // The closing ".
        this.advance();

        // Trim the surrounding quotes.
        String value = this.source.substring(this.start + 1, this.current - 1);
        this.addToken(TokenType.STRING, value);
    }

    private void number() {
        while (this.isDigit(this.peek())) this.advance();

        if (this.peek() == '.' && this.isDigit(this.peekNext())) {
            // consume the '.'
            this.advance();
        }

        while (this.isDigit(this.peek())) this.advance();

        double numberValue = Double.parseDouble(source.substring(this.start, this.current));

        addToken(TokenType.NUMBER, numberValue );
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
        
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return this.isDigit(c) || this.isAlpha(c);
    }

    private void identifier() {
        while (this.isAlphaNumeric(this.peek())) advance();

        String text = this.source.substring(this.start, this.current);
        TokenType type = Scanner.keywords.get(text);

        if (type == null) this.addToken(TokenType.IDENTIFIER);
        else this.addToken(type);
        
    }
}

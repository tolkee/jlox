package lox;

import java.util.List;

public class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    private Expr expression() {
        return this.equality();
    }

    private Expr equality() {
        Expr expr = this.comparison();

        while (this.match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = this.previous();
            Expr right = this.comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = this.term();

        while (this.match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = this.previous();
            Expr right = this.term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = this.factor();

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = this.previous();
            Expr right = this.factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = this.unary();

        while (match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = this.previous();
            Expr right = this.unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = this.previous();
            Expr right = this.unary();
            return new Expr.Unary(operator, right);
        }

        return this.primary();
    }

    private Expr primary() {
        Token currentToken = this.peek();
        this.advance();

        switch (currentToken.type) {
            case FALSE:
                return new Expr.Literal(false);
            case TRUE:
                return new Expr.Literal(true);
            case NIL:
                return new Expr.Literal(null);
            case NUMBER:
            case STRING:
                return new Expr.Literal(previous().literal);
            case LEFT_PAREN:
                Expr expr = this.expression();
                this.consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
                return new Expr.Grouping(expr);
            default:
                throw error(currentToken, "Expect expression.");
        }
    }

    private boolean match(
            TokenType... types) {
        for (TokenType type : types) {
            if (this.check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        return this.tokens.get(current).type == type;
    }

    private Token advance() {
        if (!isAtEnd())
            current++;

        return previous();
    }

    private boolean isAtEnd() {
        return this.peek().type == TokenType.EOF;
    }

    private Token peek() {
        return this.tokens.get(this.current);
    }

    private Token previous() {
        return this.tokens.get(this.current - 1);
    }

    private Token consume(TokenType type, String errorMessage) {
        if (this.check(type))
            return advance();

        throw error(peek(), errorMessage);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);

        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (isAtEnd()) {
            switch (this.peek().type) {
                case FOR:
                case WHILE:
                case FUN:
                case VAR:
                case CLASS:
                case IF:
                case PRINT:
                case RETURN:
                    return;
                default:
                    // do nothing for non starting token statements
            }

            advance();
        }
    }
}

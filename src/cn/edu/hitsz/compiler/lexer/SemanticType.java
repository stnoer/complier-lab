package cn.edu.hitsz.compiler.lexer;

import cn.edu.hitsz.compiler.parser.table.Symbol;
import cn.edu.hitsz.compiler.symtab.SourceCodeType;

public class SemanticType {
    public SourceCodeType type = null;
    private Symbol symbol = null;

    public SemanticType(SourceCodeType type, Token token) {
        this.type = type;
        this.symbol = new Symbol(token);
    }

    public SourceCodeType getType() {
        if (this.type == null) {
            throw new RuntimeException("Not a type!");
        }
        return type;
    }

    public Symbol getSymbol() {
        if (this.symbol == null) {
            throw new RuntimeException("Not a type!");
        }
        return symbol;
    }
}

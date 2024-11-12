package cn.edu.hitsz.compiler.parser.table;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.lexer.Token;
//以简单定义一个 Symbol 来实现 Union<Token, NonTerminal> 的功能
public class Symbol {
        private Token token;
        private NonTerminal nonTerminal;
        public IRValue value = null;

        private Symbol(Token token, NonTerminal nonTerminal){
            this.token = token;
            this.nonTerminal = nonTerminal;
        }

        public Symbol(Token token){
            this(token, null);
        }

        public Symbol(NonTerminal nonTerminal){
            this(null, nonTerminal);
        }

        public boolean isToken(){
            return this.token != null;
        }

        public boolean isNonterminal(){
            return this.nonTerminal != null;
        }
        public NonTerminal getNonTerminal(){
            if(isNonterminal())return  this.nonTerminal;
            else throw new NotImplementedException();
        }

        public Token getToken() {
            if (isToken()) return  this.token;
            else throw new NotImplementedException();
        }

}


package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.lexer.SemanticType;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.lexer.TokenKind;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.symtab.SourceCodeType;
import cn.edu.hitsz.compiler.symtab.SymbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

// TODO: 实验三: 实现语义分析
public class SemanticAnalyzer implements ActionObserver {

//定义语义栈，存type属性//还要扩充token
    private SymbolTable symbolTable;

    private Stack<SemanticType> sybmolstack = new Stack<>() ;

    @Override
    public void whenAccept(Status currentStatus) {
        // TODO: 该过程在遇到 Accept 时要采取的代码动作
        //System.out.println("语法分析完成，符号表内容：" + symbolTable);
        //throw new NotImplementedException();
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO: 该过程在遇到 reduce production 时要采取的代码动作
        //prodution对应产生式
        //定义语义检查相关的动作
        switch (production.index()){
            case 4://S -> D id;
                var token = sybmolstack.pop().getSymbol().getToken();
                symbolTable.get(token.getText()).setType(sybmolstack.pop().getType());
                sybmolstack.push(new SemanticType(null,null));
                break;
            case 5://D -> int;
                if(Objects.equals(sybmolstack.pop().getSymbol().getToken().getKindId(), "int"))
                {
                    sybmolstack.push(new SemanticType(SourceCodeType.Int,null));
                }
                else throw new UnsupportedOperationException("Semantic analyzer case 5 error.");
                break;
            default:
                // 其它产生式直接弹栈即可
                int num = production.body().size();
                while (num > 0) {
                    sybmolstack.pop();
                    num--;
                }
                // 压入空记录占位
                sybmolstack.push(new SemanticType(null,null));

        }
        //throw new NotImplementedException();
    }

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO: 该过程在遇到 shift 时要采取的代码动作
            sybmolstack.push(new SemanticType(null,currentToken));
        //进来要先保存token
        //System.out.println("Shift: " + currentToken.getText());
        //throw new NotImplementedException();
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO: 设计你可能需要的符号表存储结构
        // 如果需要使用符号表的话, 可以将它或者它的一部分信息存起来, 比如使用一个成员变量存储
        this.symbolTable =table;
        //throw new NotImplementedException();
    }
}


package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.IRImmediate;
import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.lexer.SemanticType;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.parser.table.Symbol;
import cn.edu.hitsz.compiler.symtab.SourceCodeType;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

// TODO: 实验三: 实现 IR 生成

/**
 *
 */

//定义栈，存贮value属性值，用IRValue类
public class IRGenerator implements ActionObserver {

    private SymbolTable symbolTable;
    private final Stack<Symbol> IRStack = new Stack<>();
    private List<Instruction> instructionList = new ArrayList<>();
    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO
        Symbol currentSymbol = new Symbol(currentToken);
        if(Objects.equals(currentToken.getKind().getTermName(), "IntConst"))
            currentSymbol.value = IRImmediate.of(Integer.parseInt(currentToken.getText()));
        else
            currentSymbol.value = IRVariable.named(currentToken.getText());
       IRStack.push(currentSymbol);
        // throw new NotImplementedException();
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO
        Symbol NonTerminal = new Symbol(production.head());
        IRVariable tempVariable;
        Symbol leftPart , rigthPart;
        switch (production.index()){
            case 6://S -> id = E;
                rigthPart = IRStack.pop();//E
                IRStack.pop();//=
                leftPart = IRStack.pop();//id
                tempVariable = (IRVariable) leftPart.value;
                NonTerminal.value = null;
                instructionList.add(Instruction.createMov(tempVariable,rigthPart.value));
                IRStack.push(NonTerminal);
                break;
            case 10:case 12: case 14://E -> A;//A -> B;//B -> id;
                NonTerminal.value = IRStack.pop().value;//A/B/id
                IRStack.push(NonTerminal);
                break;
            case 15://B -> IntConst;
                rigthPart = IRStack.pop();//int数
                NonTerminal.value = rigthPart.value;
                IRStack.push(NonTerminal);
                break;
            case 7://S -> return E;
                rigthPart = IRStack.pop();//E
                IRStack.pop();//return
                NonTerminal.value = null;
                instructionList.add(Instruction.createRet(rigthPart.value));
                IRStack.push(NonTerminal);
                break;
            case 8://E -> E + A;
                rigthPart = IRStack.pop();//A
                IRStack.pop();//+
                leftPart = IRStack.pop();//E
                tempVariable = IRVariable.temp();  //生成临时变量
                instructionList.add(Instruction.createAdd(tempVariable, leftPart.value, rigthPart.value));
                NonTerminal.value = tempVariable;
                IRStack.push(NonTerminal);
                break;
            case 9://E -> E - A;
                rigthPart = IRStack.pop();//A
                IRStack.pop();//-
                leftPart = IRStack.pop();//E
                tempVariable = IRVariable.temp();  //生成临时变量
                instructionList.add(Instruction.createSub(tempVariable, leftPart.value, rigthPart.value));
                NonTerminal.value = tempVariable;
                IRStack.push(NonTerminal);
                break;
            case 11://A -> A * B;
                rigthPart = IRStack.pop();//B
                IRStack.pop();//*
                leftPart = IRStack.pop();//A
                tempVariable = IRVariable.temp();  //生成临时变量
                instructionList.add(Instruction.createMul(tempVariable, leftPart.value, rigthPart.value));
                NonTerminal.value = tempVariable;
                IRStack.push(NonTerminal);
                break;
            case 13:    //B -> ( E );
                IRStack.pop();
                leftPart = IRStack.pop();
                IRStack.pop();
                NonTerminal.value = leftPart.value;
                IRStack.push(NonTerminal);
                break;
            default:
                for(int i=0; i<production.body().size(); i++){
                    IRStack.pop();
                }
                IRStack.push(new Symbol(production.head()));
                break;
        }
//        throw new NotImplementedException();
    }


    @Override
    public void whenAccept(Status currentStatus) {
        // TODO
        System.out.println("IR完成，符号表内容：" + symbolTable);
        //throw new NotImplementedException();
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO
        this.symbolTable =table;
        //throw new NotImplementedException();
    }

    public List<Instruction> getIR() {
        // TODO
        return instructionList;
        //throw new NotImplementedException();
    }

    public void dumpIR(String path) {
        FileUtils.writeLines(path, getIR().stream().map(Instruction::toString).toList());
    }

}


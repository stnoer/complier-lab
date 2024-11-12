package cn.edu.hitsz.compiler.lexer;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

/**
 * TODO: 实验一: 实现词法分析
 * <br>
 * 你可能需要参考的框架代码如下:
 *
 * @see Token 词法单元的实现
 * @see TokenKind 词法单元类型的实现
 */
public class LexicalAnalyzer {
    private final SymbolTable symbolTable;
    private String fileString;
    public LexicalAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public List<Token> tokenList = new ArrayList<>();


    /**
     * 从给予的路径中读取并加载文件内容
     *
     * @param path 路径
     */
    public void loadFile(String path) {
        // TODO: 词法分析前的缓冲区实现
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            StringBuilder fileContent = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                fileContent.append(line);
            }
            this.fileString = fileContent.toString();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // 可自由实现各类缓冲区
        // 或直接采用完整读入方法
        //throw new NotImplementedException();
    }

    /**
     * 执行词法分析, 准备好用于返回的 token 列表 <br>
     * 需要维护实验一所需的符号表条目, 而得在语法分析中才能确定的符号表条目的成员可以先设置为 null
     */
    public boolean isKeyword(String s){
        return Objects.equals(s, "int") || Objects.equals(s, "return");
    }
    public void run() {
        // TODO: 自动机实现的词法分析过程


        char [] charArray = fileString.toCharArray();
        for(int i = 0; i < charArray.length; ){
            char ch = charArray[i++];
            StringBuilder string = new StringBuilder();
            if(ch==' '||ch=='\n') continue;
            if(Character.isAlphabetic(ch)){
                string.append(ch);
                ch = charArray[i++];
                while (i<charArray.length&&(Character.isAlphabetic(ch)||Character.isDigit(ch))){
                    string.append(ch);
                    ch = charArray[i++];
                }
                i--;
                String resultString = string.toString();
                if(isKeyword(resultString)){
                    Token token = Token.simple(resultString);
                    tokenList.add(token);
                }
                else {
                    if(!symbolTable.has(resultString))
                        symbolTable.add(resultString);
                    Token token=  Token.normal("id",resultString);
                    tokenList.add(token);
                }
            }
            else if(Character.isDigit(ch)){
                string.append(ch);
                ch = charArray[i++];
                while (i<charArray.length&&Character.isDigit(ch)) {
                    string.append(ch);
                    ch = charArray[i++];
                }
                i--;
                String resultString = string.toString();
                Token token = Token.normal("IntConst",resultString);
                tokenList.add(token);
            }
            else{
                switch (ch) {
                    case '*':
                        Token token = Token.simple("*");
                        tokenList.add(token);
                        break;
                    case '=':
                        Token token2 = Token.simple("=");
                        tokenList.add(token2);
                        break;
                    case ',':
                        Token token3 = Token.simple(",");
                        tokenList.add(token3);
                        break;
                    case ';':
                        Token token4 = Token.simple("Semicolon");
                        tokenList.add(token4);
                        break;
                    case '+':
                        Token token5 = Token.simple("+");
                        tokenList.add(token5);
                        break;
                    case '-':
                        Token token6 = Token.simple("-");
                        tokenList.add(token6);
                        break;
                    case '/':
                        Token token7 = Token.simple("/");
                        tokenList.add(token7);
                        break;
                    case '(':
                        Token token8 = Token.simple("(");
                        tokenList.add(token8);
                        break;
                    case ')':
                        Token token9 = Token.simple(")");
                        tokenList.add(token9);
                        break;
                }
            }
        }
        Token token = Token.eof();
        tokenList.add(token);
    }

    /**
     * 获得词法分析的结果, 保证在调用了 run 方法之后调用
     *
     * @return Token 列表
     */
    public Iterable<Token> getTokens() {
        // TODO: 从词法分析过程中获取 Token 列表
        if(tokenList!=null)return tokenList;
        throw new NotImplementedException();
    }

    public void dumpTokens(String path) {
        FileUtils.writeLines(
            path,
            StreamSupport.stream(getTokens().spliterator(), false).map(Token::toString).toList()
        );
    }


}

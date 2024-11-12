package cn.edu.hitsz.compiler.symtab;

import java.util.Objects;

/**
 * 符号表条目
 */
public class SymbolTableEntry {
    /**
     * @param text 符号的文本表示. 对于标识符符号, 该参数应该为标识符文本.
     */
    public SymbolTableEntry(String text) {
        this.text = text;
        this.type = null;
    }

    /**
     * @return 符号的文本表示
     */
    public String getText() {
        return text;
    }

    /**
     * @return 该标识符符号可以绑定到的源语言对象的类型
     */
    public SourceCodeType getType() {
        return type;
    }

    /**
     * 由于这个类型严格来说只能在语法分析后才能获得, 所以为了在词法分析时就构造出符号表,
     * 我们只能暴露出该接口用以修改该成员. 该成员应该且只应该被修改一次.
     *
     * @param type 该标识符符号可以绑定到的源语言对象的类型
     */
    public void setType(SourceCodeType type) {
        if (this.type != null) {
            throw new RuntimeException("Can NOT set type for an entry twice");
        }

        this.type = type;
    }

    private final String text;
    private SourceCodeType type;

    public boolean equals(Object obj) {
        // 1. 如果两个对象的引用相同，直接返回 true
        if (this == obj) return true;

        // 2. 检查 obj 是否为 null 或是否是同一类型的对象
        if (obj == null || getClass() != obj.getClass()) return false;

        // 3. 将传入的对象转换为 Person 类型
        SymbolTableEntry symbolTableEntry = (SymbolTableEntry) obj;

        // 4. 比较两个对象的属性，决定是否相等
        return Objects.equals(text, symbolTableEntry.text);
    }

    // 重写 hashCode() 方法
    @Override
    public int hashCode() {
        return Objects.hash(text, type);
    }

}

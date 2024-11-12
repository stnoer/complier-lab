package cn.edu.hitsz.compiler.asm;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.*;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * TODO: 实验四: 实现汇编生成
 * <br>
 * 在编译器的整体框架中, 代码生成可以称作后端, 而前面的所有工作都可称为前端.
 * <br>
 * 在前端完成的所有工作中, 都是与目标平台无关的, 而后端的工作为将前端生成的目标平台无关信息
 * 根据目标平台生成汇编代码. 前后端的分离有利于实现编译器面向不同平台生成汇编代码. 由于前后
 * 端分离的原因, 有可能前端生成的中间代码并不符合目标平台的汇编代码特点. 具体到本项目你可以
 * 尝试加入一个方法将中间代码调整为更接近 risc-v 汇编的形式, 这样会有利于汇编代码的生成.
 * <br>
 * 为保证实现上的自由, 框架中并未对后端提供基建, 在具体实现时可自行设计相关数据结构.
 *
 * @see AssemblyGenerator#run() 代码生成与寄存器分配
 */
public class AssemblyGenerator {

     //存放中间指令

    private final List<Instruction> instructions = new ArrayList<>();


    //变量寄存器双向map

    private BijectiveMap<IRValue, Reg> regMap = new BijectiveMap<>();

    //生成汇编指令

    private final List<String> asmInstructions = new ArrayList<>(List.of(".text"));

    //寄存器

    enum Reg {
        t0,t1,t2,t3,t4,t5,t6
    }

    /**
     * 加载前端提供的中间代码
     * <br>
     * 视具体实现而定, 在加载中或加载后会生成一些在代码生成中会用到的信息. 如变量的引用
     * 信息. 这些信息可以通过简单的映射维护, 或者自行增加记录信息的数据结构.
     *
     * @param originInstructions 前端提供的中间代码
     */
    public void loadIR(List<Instruction> originInstructions) {
        // TODO: 读入前端提供的中间代码并生成所需要的信息
        for(Instruction instruction : originInstructions){
            InstructionKind instructionKind = instruction.getKind();
            if(instructionKind.isBinary()){//是两个操作数指令
                IRValue leftPart = instruction.getLHS();
                IRValue rightPart = instruction.getRHS();
                IRVariable result = instruction.getResult();
                //两个操作数都是立即数
                if(leftPart.isImmediate() && rightPart.isImmediate()){
                    int opResult = 0;
                    switch (instructionKind){
                        case MUL-> opResult = ((IRImmediate)leftPart).getValue() * ((IRImmediate)rightPart).getValue();
                        case SUB -> opResult = ((IRImmediate)leftPart).getValue() - ((IRImmediate)rightPart).getValue();
                        case ADD -> opResult = ((IRImmediate)leftPart).getValue() + ((IRImmediate)rightPart).getValue();
                        default -> throw new NotImplementedException();
                    }
                    instructions.add(Instruction.createMov(result,IRImmediate.of(opResult)));
                }
                //修改左立即数
                else if (leftPart.isImmediate() && rightPart.isIRVariable()){
                    switch (instructionKind){
                        case SUB -> {
                            IRVariable temp = IRVariable.temp();
                            instructions.add(Instruction.createMov(temp, leftPart));
                            instructions.add(Instruction.createSub(result, temp, rightPart));
                        }
                        case MUL -> {
                            IRVariable temp = IRVariable.temp();
                            instructions.add(Instruction.createMov(temp, leftPart));
                            instructions.add(Instruction.createMul(result, temp, rightPart));
                        }
                        case ADD -> instructions.add(Instruction.createAdd(result,rightPart,leftPart));
                    }
                }
                //修改右立即数
                else if (leftPart.isIRVariable() && rightPart.isImmediate()) {
                    switch (instructionKind){
                        case ADD -> instructions.add(instruction);
                        case SUB -> instructions.add(instruction);
                        case MUL ->{
                            IRVariable temp = IRVariable.temp();
                            instructions.add(Instruction.createMov(temp, rightPart));
                            instructions.add(Instruction.createMul(result, leftPart, rightPart));
                        }
                        default -> throw new NotImplementedException();
                    }
                }
                else{
                    instructions.add(instruction);
                }
            }
            else if(instructionKind.isUnary()){//是一个操作数指令指令
                instructions.add(instruction);
            }
            else if (instructionKind.isReturn()){
                instructions.add(instruction);
                break;
            }
        }

        //throw new NotImplementedException();
    }

    /**
     * 分配寄存器
     */
    public void VariableToRegister(IRValue operands, int index){
        // 立即数无需分配寄存器
        if(operands.isImmediate()){
            return;
        }
        // 变量已经存在寄存器中
        if(regMap.containsKey(operands)){
            return;
        }
        // 不存在,寻找空闲寄存器
        for(Reg register: Reg.values()){
            if(!regMap.containsValue(register)){
                regMap.replace(operands, register);
                return;
            }
        }
        // 若无空闲寄存器，则夺取不再使用的变量所占的寄存器
        Set<Reg> notUseRegs = Arrays.stream(Reg.values()).collect(Collectors.toSet());
        for(int i = index; i<instructions.size(); i++){
            Instruction instruction = instructions.get(i);
            // 遍历搜寻不再使用的变量
            for(IRValue irValue: instruction.getOperands()){
                notUseRegs.remove(regMap.getByKey(irValue));
            }
        }
        if(!notUseRegs.isEmpty()){
            regMap.replace(operands, notUseRegs.iterator().next());
            return;
        }
        // 否则将无法分配寄存器并报错
        throw new NotImplementedException();
    }

    /**
     * 执行代码生成.
     * <br>
     * 根据理论课的做法, 在代码生成时同时完成寄存器分配的工作. 若你觉得这样的做法不好,
     * 也可以将寄存器分配和代码生成分开进行.
     * <br>
     * 提示: 寄存器分配中需要的信息较多, 关于全局的与代码生成过程无关的信息建议在代码生
     * 成前完成建立, 与代码生成的过程相关的信息可自行设计数据结构进行记录并动态维护.
     */
    public void run() {
        // TODO: 执行寄存器分配与代码生成
        int i = 0;
        String asmCode = null;
        for(Instruction instruction:instructions){
            InstructionKind instructionKind = instruction.getKind();
            switch (instructionKind) {
                case MOV -> {
                    IRValue form = instruction.getFrom();
                    IRVariable result = instruction.getResult();
                    VariableToRegister(form, i);
                    VariableToRegister(result, i);
                    Reg formReg = regMap.getByKey(form);
                    Reg resultReg = regMap.getByKey(result);
                    if (form.isImmediate()) {
                        asmCode = String.format("\tli %s, %s", resultReg.toString(), form.toString());
                    } else {
                        asmCode = String.format("\tmv %s, %s", resultReg.toString(), formReg.toString());
                    }
                }
                case MUL -> {
                    IRValue leftPart = instruction.getLHS();
                    IRValue rightPart = instruction.getRHS();
                    IRVariable result = instruction.getResult();
                    VariableToRegister(leftPart, i);
                    VariableToRegister(rightPart, i);
                    VariableToRegister(result, i);
                    Reg lReg = regMap.getByKey(leftPart);
                    Reg rReg = regMap.getByKey(rightPart);
                    Reg resultReg = regMap.getByKey(result);
                    asmCode = String.format("\tmul %s, %s, %s", resultReg.toString(), lReg.toString(), rReg.toString());

                }
                case ADD -> {
                    IRValue leftPart = instruction.getLHS();
                    IRValue rightPart = instruction.getRHS();
                    IRVariable result = instruction.getResult();
                    VariableToRegister(leftPart, i);
                    VariableToRegister(rightPart, i);
                    VariableToRegister(result, i);
                    Reg lReg = regMap.getByKey(leftPart);
                    Reg rReg = regMap.getByKey(rightPart);
                    Reg resultReg = regMap.getByKey(result);
                    if (rightPart.isImmediate()) {
                        asmCode = String.format("\taddi %s, %s, %s", resultReg.toString(), lReg.toString(), rightPart.toString());
                    } else {
                        asmCode = String.format("\tadd %s, %s, %s", resultReg.toString(), lReg.toString(), rReg.toString());
                    }
                }
                case SUB -> {
                    IRValue leftPart = instruction.getLHS();
                    IRValue rightPart = instruction.getRHS();
                    IRVariable result = instruction.getResult();
                    VariableToRegister(leftPart, i);
                    VariableToRegister(rightPart, i);
                    VariableToRegister(result, i);
                    Reg lReg = regMap.getByKey(leftPart);
                    Reg rReg = regMap.getByKey(rightPart);
                    Reg resultReg = regMap.getByKey(result);
                    if (rightPart.isImmediate()) {
                        asmCode = String.format("\tsubi %s, %s, %s", resultReg.toString(), lReg.toString(), rightPart.toString());
                    } else {
                        asmCode = String.format("\tsub %s, %s, %s", resultReg.toString(), lReg.toString(), rReg.toString());
                    }
                }
                case RET -> {
                    IRValue returnValue = instruction.getReturnValue();
                    Reg returnValueReg = regMap.getByKey(returnValue);
                    asmCode = String.format("\tmv a0, %s", returnValueReg.toString());
                }
                default -> throw new RuntimeException("error asm!");
                }
            asmCode += "\t\t#  %s".formatted(instruction.toString());
            asmInstructions.add(asmCode);
            i++;
            if(instructionKind == InstructionKind.RET){
                break;
            }
        }
    }


    /**
     * 输出汇编代码到文件
     *
     * @param path 输出文件路径
     */
    public void dump(String path) {
        // TODO: 输出汇编代码到文件
        FileUtils.writeLines(path, asmInstructions);
    }
}


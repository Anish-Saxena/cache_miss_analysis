import java.util.*;
import java.lang.Math;
import static java.util.stream.Collectors.*;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.xpath.XPath;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode.*;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.ParserRuleContext;

// FIXME: You should limit your implementation to this class. You are free to add new auxilliary classes. You do not need to touch the LoopNext.g4 file.
class Analysis extends LoopNestBaseListener {

    // Possible types
    enum Types {
        Byte, Short, Int, Long, Char, Float, Double, Boolean, String
    }

    // Type of variable declaration
    enum VariableType {
        Primitive, Array, Literal
    }

    // Types of caches supported
    enum CacheTypes {
        DirectMapped, SetAssociative, FullyAssociative,
    }

    // auxilliary data-structure for converting strings
    // to types, ignoring strings because string is not a
    // valid type for loop bounds
    final Map<String, Types> stringToType = Collections.unmodifiableMap(new HashMap<String, Types>() {
        private static final long serialVersionUID = 1L;

        {
            put("byte", Types.Byte);
            put("short", Types.Short);
            put("int", Types.Int);
            put("long", Types.Long);
            put("char", Types.Char);
            put("float", Types.Float);
            put("double", Types.Double);
            put("boolean", Types.Boolean);
        }
    });

    // Map of cache type string to value of CacheTypes
    final Map<String, CacheTypes> stringToCacheType = Collections.unmodifiableMap(new HashMap<String, CacheTypes>() {
        private static final long serialVersionUID = 1L;

        {
            put("FullyAssociative", CacheTypes.FullyAssociative);
            put("SetAssociative", CacheTypes.SetAssociative);
            put("DirectMapped", CacheTypes.DirectMapped);
        }
    });

    ArrayList<HashMap<String, Long>> TestCases;
    TestCase currentTestCase;
    boolean debugEnable;

    public Analysis(boolean IsDebugEnable) {
        debugEnable = IsDebugEnable;
    }

    /******************************** LOCAL VARIABLE CREATION BEGIN ********************************/
    @Override
    public void enterLocalVariableDeclaration(LoopNestParser.LocalVariableDeclarationContext ctx) {
        currentTestCase.currVarDeclaration = true;
        currentTestCase.currVarName = ctx.getChild(1).getChild(0).getText();
    }

    @Override
    public void exitUnannArrayType(LoopNestParser.UnannArrayTypeContext ctx) {
        if (currentTestCase.currVarDeclaration){
            currentTestCase.currVarIsArr = true;
            currentTestCase.currVarDims = (ctx.getChild(1).getChildCount())/2;
        }
    }

    @Override
    public void exitUnannStringType(LoopNestParser.UnannStringTypeContext ctx) {
        if (currentTestCase.currVarDeclaration){
            currentTestCase.currVarType = Types.String;
        }
    }

    @Override
    public void exitUnannPrimitiveType(LoopNestParser.UnannPrimitiveTypeContext ctx) {
        if (currentTestCase.currVarDeclaration){
            currentTestCase.currVarType = stringToType.get(ctx.getText());
            if (currentTestCase.currVarType == Types.Short || currentTestCase.currVarType == Types.Int ||
                currentTestCase.currVarType == Types.Long){
                currentTestCase.isImpVar = true;
            }
        }
    }

    @Override
    public void exitVariableDeclarator(LoopNestParser.VariableDeclaratorContext ctx) {
        if (currentTestCase.currVarDeclaration && ! currentTestCase.currVarIsArr){
            String VarVal = ctx.getChild(2).getText();
            if (currentTestCase.currVarType == Types.String){
                VarVal = VarVal.substring(1, VarVal.length()-1);;
            }
            if (currentTestCase.currVarType == Types.String){
                currentTestCase.CacheType = stringToCacheType.get(VarVal);
                if (debugEnable){
                    System.out.println("CacheType is: " + currentTestCase.CacheType);
                }
            }
            else if (currentTestCase.isImpVar){
                currentTestCase.currVarVal = Long.parseLong(VarVal);
            }
        }
    }

    @Override
    public void exitLocalVariableDeclaration(LoopNestParser.LocalVariableDeclarationContext ctx) {
        if (currentTestCase.forInit){
            currentTestCase.Loops.get(currentTestCase.forLoopDepth).GoverningVar = currentTestCase.currVarName;
            currentTestCase.Loops.get(currentTestCase.forLoopDepth).LowerBound = currentTestCase.currVarVal;
        }
        if (currentTestCase.currVarIsArr){
            ArrayInfo currArrInfo = new ArrayInfo(currentTestCase.currVarDims, currentTestCase.currVarType,
                                                    currentTestCase.currVarDimSize);
            currentTestCase.Arrays.put(currentTestCase.currVarName, currArrInfo);
            if (debugEnable){
                System.out.println("Imp Var: " + currentTestCase.currVarName + ", Params: " + currArrInfo);
            }
        }
        else if (currentTestCase.isImpVar){
            currentTestCase.SymbolTable.put(currentTestCase.currVarName, currentTestCase.currVarVal);
            if (debugEnable){
                System.out.println("Imp Var: " + currentTestCase.currVarName + ", Val: " + currentTestCase.currVarVal);
            }
        }
        else if (currentTestCase.currVarType != Types.String){
            currentTestCase.NonImpVars.put(currentTestCase.currVarName, currentTestCase.currVarType);
            if (debugEnable){
                System.out.println("Non Imp Var: " + currentTestCase.currVarName + ", Val: " + currentTestCase.currVarType);
            }
        }
        currentTestCase.currVarReset();
    }

    /******************************** LOCAL VARIABLE CREATION END ********************************/

	@Override public void enterBlockStatements(LoopNestParser.BlockStatementsContext ctx) { 
        currentTestCase.beginCompute = true;
        for (int i = 0; i < ctx.getChildCount(); i++){
            RuleContext rc = (RuleContext)ctx.getChild(i).getChild(0).getChild(0).getPayload();
            if (rc.getRuleIndex() == LoopNestParser.RULE_forStatement){
                currentTestCase.beginCompute = false;
                break;
            }
        }
        if (currentTestCase.beginCompute){
            currentTestCase.cacheSize = (long)Math.pow(2, currentTestCase.SymbolTable.get("cachePower"));
            currentTestCase.blockSize = (long)Math.pow(2, currentTestCase.SymbolTable.get("blockPower"));
            currentTestCase.numBlocks = currentTestCase.cacheSize/currentTestCase.blockSize;
            if (currentTestCase.CacheType == CacheTypes.SetAssociative){
                currentTestCase.numWays = currentTestCase.SymbolTable.get("setSize");
                currentTestCase.numSets = currentTestCase.cacheSize/(currentTestCase.blockSize*currentTestCase.numWays);
            }
            else if (currentTestCase.CacheType == CacheTypes.DirectMapped){
                currentTestCase.numWays = 1;
                currentTestCase.numSets = currentTestCase.numBlocks;
            }
            else {
                currentTestCase.numWays = currentTestCase.numBlocks;
                currentTestCase.numSets = 1;
            }
            if (debugEnable){
                System.out.println("Compute enabled, cacheSize: " + currentTestCase.cacheSize + ", numBlocks: " + 
                currentTestCase.numBlocks + ", numSets: " + currentTestCase.numSets + ", numWays: " + currentTestCase.numWays);
            }
        }
    }

    /******************************** FOR STATEMENT BLOCK BEGIN ********************************/

	@Override public void enterForStatement(LoopNestParser.ForStatementContext ctx) { 
        currentTestCase.Loops.add(new ForLoop());
        currentTestCase.forLoopDepth++;
    }
    @Override
    public void enterForInit(LoopNestParser.ForInitContext ctx) {
        currentTestCase.forInit = true;
    }

    @Override
    public void exitForInit(LoopNestParser.ForInitContext ctx) {
        currentTestCase.forInit = false;
    }
    
    @Override
    public void enterForCondition(LoopNestParser.ForConditionContext ctx) {
        currentTestCase.forCond = true;
    }

    @Override public void exitRelationalExpression(LoopNestParser.RelationalExpressionContext ctx) { 
        if (currentTestCase.forCond){
            String UpperVal = ctx.getChild(2).getText();
            if (UpperVal.charAt(0) >= '0' && UpperVal.charAt(0) <= '9'){
                currentTestCase.Loops.get(currentTestCase.forLoopDepth).UpperBound = Long.parseLong(UpperVal);
            }
            else {
                currentTestCase.Loops.get(currentTestCase.forLoopDepth).UpperBound = currentTestCase.ResolveSymbolicAssignment(UpperVal);
            }
        }
    }

    @Override
    public void exitForCondition(LoopNestParser.ForConditionContext ctx) {
        currentTestCase.forCond = false;
    }

    @Override
    public void exitSimplifiedAssignment(LoopNestParser.SimplifiedAssignmentContext ctx) {
        String stride = ctx.getChild(2).getText();
        if (stride.charAt(0) >= '0' && stride.charAt(0) <= '9'){
            currentTestCase.Loops.get(currentTestCase.forLoopDepth).Stride = Long.parseLong(stride);
        }
        else {
            currentTestCase.Loops.get(currentTestCase.forLoopDepth).Stride = currentTestCase.ResolveSymbolicAssignment(stride);
        }
        if (debugEnable){
            System.out.println("For Loop: " + currentTestCase.Loops.get(currentTestCase.forLoopDepth));
        }
    }

    @Override public void exitForStatement(LoopNestParser.ForStatementContext ctx) { 
        if (currentTestCase.beginCompute){
            ForLoop currLoop = currentTestCase.Loops.get(currentTestCase.forLoopDepth);
            long stride = currLoop.Stride;
            long numElAccessed = (currLoop.UpperBound)/stride;
            ///////////////// AUGMENT MISSES FOR ARRAYS THAT RELY ON THIS LOOP ////////////////
            for (int i = 0; i < currLoop.ArrTouched.size(); i++){
                ArrayInfo arr = currentTestCase.Arrays.get(currLoop.ArrTouched.get(i));
                long byteJump = stride * arr.ElSize;
                int accDim = -1;
                for (int j = arr.ArrayDims - 1; j >= 0; j--){
                    if (arr.forLoopVar[j].equals(currLoop.GoverningVar)){
                        accDim = j;
                        break;
                    }
                    else {
                        byteJump *= arr.ArrayDim[j];
                    }
                }
                if (byteJump < currentTestCase.blockSize && arr.cacheFitsBlocks){
                    arr.Misses *= ((byteJump * numElAccessed)/currentTestCase.blockSize < 1) ? 1 
                                                : (byteJump * numElAccessed)/currentTestCase.blockSize;
                }
                else {
                    arr.Misses *= numElAccessed;
                }
                if (debugEnable){
                    System.out.println("Computing step for array: " + currLoop.ArrTouched.get(i) + " byteJump: " + byteJump + " elAcc: " + 
                                        numElAccessed + " cacheFitsBlocks: " + arr.cacheFitsBlocks + " Misses: " + arr.Misses);
                }
                if (arr.cacheFitsBlocks){
                    if (arr.Misses > currentTestCase.numBlocks){
                        arr.cacheFitsBlocks = false;
                    }
                    else if (byteJump/currentTestCase.blockSize >= currentTestCase.numSets){
                        if (numElAccessed > currentTestCase.numWays){
                            arr.cacheFitsBlocks = false;
                        }
                    }
                    else if ((numElAccessed * byteJump)/currentTestCase.blockSize > currentTestCase.numBlocks){
                        arr.cacheFitsBlocks = false;
                    }
                    else {
                        arr.cacheFitsBlocks = true;
                    }
                }
            }
            ///////////////// AUGMENT MISSES FOR ARRAYS THAT DO NOT RELY ON THIS LOOP ////////////////
            if (currentTestCase.forLoopDepth + 1 < currentTestCase.Loops.size()){
                ForLoop nextLoop = currentTestCase.Loops.get(currentTestCase.forLoopDepth + 1);
                for (int k  = 0; k < nextLoop.ArrTouched.size(); k++){
                    if (!currLoop.ArrTouched.contains(nextLoop.ArrTouched.get(k))){
                        currLoop.ArrTouched.add(nextLoop.ArrTouched.get(k));
                        if (!currentTestCase.Arrays.get(nextLoop.ArrTouched.get(k)).cacheFitsBlocks){
                            if (debugEnable){
                                System.out.println("Extra computing step for array: " + nextLoop.ArrTouched.get(k) + 
                                                    " elements accessed: " + numElAccessed);
                            }
                            currentTestCase.Arrays.get(nextLoop.ArrTouched.get(k)).Misses *= numElAccessed;
                        }
                    }
                }
            }
        }
        currentTestCase.forLoopDepth--;
    }

    /******************************** FOR STATEMENT BLOCK END ********************************/

    /******************************** ASSIGNMENT STATEMENT BEGIN ********************************/

    @Override
    public void exitArrayAccess(LoopNestParser.ArrayAccessContext ctx) {
        if (currentTestCase.beginCompute){
            String ArrName = ctx.getChild(0).getText();
            ArrayInfo arrI = currentTestCase.Arrays.get(ArrName);
            for (int i = 2; i < 3*arrI.ArrayDims + 2; i += 3){
                arrI.forLoopVar[(i-2)/3] = ctx.getChild(i).getText();
                for (int j  = 0; j < currentTestCase.Loops.size(); j++){
                    if (currentTestCase.Loops.get(j).GoverningVar.equals(arrI.forLoopVar[(i-2)/3])){
                        if (!currentTestCase.Loops.get(j).ArrTouched.contains(ArrName)){
                            currentTestCase.Loops.get(j).ArrTouched.add(ArrName);
                        }
                        if (debugEnable){
                        System.out.println(ArrName + " is indexed by loop var " 
                                            + currentTestCase.Loops.get(j).GoverningVar);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void exitArrayAccess_lfno_primary(LoopNestParser.ArrayAccess_lfno_primaryContext ctx) {
        if (currentTestCase.beginCompute){
            String ArrName = ctx.getChild(0).getText();
            ArrayInfo arrI = currentTestCase.Arrays.get(ArrName);
            for (int i = 2; i < 3*arrI.ArrayDims + 2; i += 3){
                arrI.forLoopVar[(i-2)/3] = ctx.getChild(i).getText();
                for (int j  = 0; j < currentTestCase.Loops.size(); j++){
                    if (currentTestCase.Loops.get(j).GoverningVar.equals(arrI.forLoopVar[(i-2)/3])){
                        if (!currentTestCase.Loops.get(j).ArrTouched.contains(ArrName)){
                            currentTestCase.Loops.get(j).ArrTouched.add(ArrName);
                        }
                        if (debugEnable){
                            System.out.println(ArrName + " is indexed by loop var " + 
                                                currentTestCase.Loops.get(j).GoverningVar);
                        }
                    }
                }
            }
        }
    }

    /******************************** ASSIGNMENT STATEMENT END ********************************/


    @Override
    public void exitDimExpr(LoopNestParser.DimExprContext ctx) {
        if (currentTestCase.currVarDeclaration && currentTestCase.currVarIsArr && 
            currentTestCase.currVarArrDim < currentTestCase.currVarDims){
            String DimSize = ctx.getChild(1).getText();
            if (DimSize.charAt(0) >= '0' && DimSize.charAt(0) <= '9'){
                currentTestCase.currVarDimSize[currentTestCase.currVarArrDim] = Long.parseLong(DimSize);
            }
            else{
                currentTestCase.currVarDimSize[currentTestCase.currVarArrDim] = currentTestCase.ResolveSymbolicAssignment(DimSize);
            }
            currentTestCase.currVarArrDim++;
        }
    }

    /******************************** TEST CASE ACCOUNTKEEPING BEGIN ********************************/

    @Override 
    public void enterTests(LoopNestParser.TestsContext ctx) { 
        System.out.println("Tests begin");
        if (debugEnable)
        {
            System.out.println("DEBUGGING IS ENABLED");
            System.out.println("*************************************************");
        }
        else{
            System.out.println("DEBUGGING IS DISABLED");
        }
        TestCases = new ArrayList<HashMap<String, Long>>(ctx.getChildCount());
        currentTestCase = new TestCase();
    }

    @Override
    public void enterMethodDeclarator(LoopNestParser.MethodDeclaratorContext ctx) {
        currentTestCase.TestCaseName = ctx.getChild(0).getText();
    }

    // End of testcase
    @Override
    public void exitMethodDeclaration(LoopNestParser.MethodDeclarationContext ctx) {
        Iterator it = currentTestCase.Arrays.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            ArrayInfo arr = (ArrayInfo)pair.getValue();
            currentTestCase.ComputedMisses.put((String)pair.getKey(), arr.Misses);
        }
        TestCases.add(currentTestCase.ComputedMisses);
        if (debugEnable){
            System.out.println("*************************************************");
        }
        currentTestCase.CleanUp();
    }

    @Override
    public void exitTests(LoopNestParser.TestsContext ctx) {
        System.out.println("Tests end");
        try {
            FileOutputStream fos = new FileOutputStream("Results.obj");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            // FIXME: Serialize your data to a file
            oos.writeObject(TestCases);
            oos.close();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        if (debugEnable){
            try {
                FileInputStream fis = new FileInputStream("Results.obj");
                ObjectInputStream ois = new ObjectInputStream(fis);
                System.out.println("Results.obj: ");
                System.out.println((List)ois.readObject());
                ois.close();
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    /******************************** TEST CASE ACCOUNTKEEPING END ********************************/
}

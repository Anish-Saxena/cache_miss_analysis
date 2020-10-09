import java.util.*;
import static java.util.stream.Collectors.*;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

public class TestCase {
    long cacheSize;
    long blockSize;
    long numWays;
    long numBlocks;
    long numSets;
    Analysis.CacheTypes CacheType;
    String TestCaseName;

    HashMap<String, ArrayInfo> Arrays;
    HashMap<String, Long> SymbolTable;
    HashMap<String, Analysis.Types> NonImpVars;
    HashMap<String, Long> ComputedMisses;
    ArrayList<ForLoop> Loops;

    boolean currVarDeclaration;
    boolean currVarIsArr;
    int currVarArrDim;
    Analysis.Types currVarType;
    boolean isImpVar;
    String currVarName;
    int currVarDims;
    long currVarDimSize[] = new long[3];
    long currVarVal;

    boolean forInit;
    boolean forCond;
    int forLoopDepth;
    boolean beginCompute;

    public TestCase(){
        currVarDeclaration = false;
        forLoopDepth = -1;
        numSets = 0;
        beginCompute = false;
        cacheSize = 0;
        blockSize = 0;
        numWays = 0;
        numBlocks = 0;
        TestCaseName = new String("");
        CacheType = Analysis.CacheTypes.SetAssociative;
        Arrays = new HashMap<String, ArrayInfo>();
        SymbolTable = new HashMap<String, Long>();
        ComputedMisses = new HashMap<String, Long>();
        Loops = new ArrayList<ForLoop>();
        NonImpVars = new HashMap<String, Analysis.Types>();
    }

    public void currVarReset(){
        currVarDeclaration = false;
        currVarIsArr = false;
        currVarArrDim = 0;
        isImpVar = false;
        currVarDimSize[0] = currVarDimSize[1] = currVarDimSize[2] = 0;
        currVarType = Analysis.Types.Byte;
        currVarIsArr = false;
        currVarName = "";
        currVarDims = 0;
        currVarVal = 0;
    }

    public void CleanUp(){
        currVarDeclaration = false;
        forLoopDepth = -1;
        numSets = 0;
        beginCompute = false;
        cacheSize = 0;
        blockSize = 0;
        numWays = 0;
        numBlocks = 0;
        CacheType = Analysis.CacheTypes.SetAssociative;
        TestCaseName = "";
        Arrays.clear();
        SymbolTable.clear();
        Loops.clear();
        NonImpVars.clear();
        ComputedMisses = new HashMap<String, Long>();
    }

    public long ResolveSymbolicAssignment(String alias){
        return SymbolTable.get(alias);
    }
}

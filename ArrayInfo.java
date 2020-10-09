import java.util.*;
import static java.util.stream.Collectors.*;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;


class ArrayInfo{    
    // auxilliary data-structure for mapping types to their byte-size
    // size x means the actual size is 2^x bytes, again ignoring strings
    final Map<Analysis.Types, Integer> typeToSize = Collections.unmodifiableMap(new HashMap<Analysis.Types, Integer>() {
        private static final long serialVersionUID = 1L;

        {
            put(Analysis.Types.Byte, 0);
            put(Analysis.Types.Short, 1);
            put(Analysis.Types.Int, 2);
            put(Analysis.Types.Long, 3);
            put(Analysis.Types.Char, 1);
            put(Analysis.Types.Float, 2);
            put(Analysis.Types.Double, 3);
            put(Analysis.Types.Boolean, 0);
        }
    });
    int ArrayDims;
    Analysis.Types ArrayType;
    long ArrayDim[] = new long[3];
    String forLoopVar[] = new String[3];
    int ElSize;
    boolean isFlushed;
    boolean cacheFitsBlocks;

    public long Misses;

    public ArrayInfo(int ADs, Analysis.Types AT, long AD[]) {
        Misses = 1;
        isFlushed = true;
        cacheFitsBlocks = true;
        ArrayDims = ADs;
        ArrayType = AT;
        for (int i = 0; i < ArrayDims; i++){
            ArrayDim[i] = AD[i];
            forLoopVar[i] = "";
        }
        ElSize = (int)Math.pow(2, typeToSize.get(ArrayType));
    }
    /************* USEFUL FOR DEBUGGING *************/
    @Override
    public String toString() {
      String ret = "{ Type: " + ArrayType + " | Dims: ";
      for (int i = 0; i < ArrayDims; i++){
        ret += "[" + Long.toString(ArrayDim[i]) + "] ";
        }
        ret += " | Element Size: " + ElSize + " }";
        return ret;
    }
}
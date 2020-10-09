import java.util.*;
import static java.util.stream.Collectors.*;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

class ForLoop{
    long LowerBound;
    long UpperBound;
    String GoverningVar;
    long Stride;
    List<String> ArrTouched;

    public ForLoop() {
      ArrTouched = new ArrayList<String>();
    }
    /************* USEFUL FOR DEBUGGING *************/
    @Override
    public String toString() {
      return "{ LowerBound: " + LowerBound + " | UpperBound: " + UpperBound + 
            " | GoverningVar: " + GoverningVar + " | Stride: " + Stride + " }";
    }
}
## Estimate cache misses in pefect loop nests

As part of assignment in CS610A course in IIT Kanpur, this utility estimates cache misses for a program with perfect `for` loop nests and simple indexing expressions. A number of simplifying assumptions are made, including existence of private cache for each multi-dimensional array and a single cache level.

### Pre-compilation

It is assumed that ANTLR 4.8's JAR is located in `$ANTLR_JAR` directory.

1. `export CLASSPATH=".:$ANTLR_JAR:$CLASSPATH"`
2. `alias antlr4='java -Xmx500M -cp "$ANTLR_JAR" org.antlr.v4.Tool'`
3. `alias grun='java org.antlr.v4.gui.TestRig'`

### Compile

1. `antlr4 LoopNest.g4`
2. `javac LoopNest*.java`
3. `javac Driver.java`

### Execute

`java Driver <TestCase>`

The output of testcase(s) is written to a serialized object, `Results.obj`. To view the intermediate steps and the object (in human-readable form), pass a second argument with value `true`, enabling debug mode. 

Example:
`java Driver testcases/TestCase1.t true`

If ANTLR 4.8's JAR is located in base directory, `run.sh` can be used to perform all steps from pre-compilation to execution.



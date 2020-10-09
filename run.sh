#!/bin/bash

rm ./*.class ./*.interp
shopt -s expand_aliases
export CLASSPATH=".:./antlr-4.8-complete.jar:$CLASSPATH"
alias antlr4='java -Xmx500M -cp "./antlr-4.8-complete.jar" org.antlr.v4.Tool'
alias grun='java org.antlr.v4.gui.TestRig'

antlr4 LoopNest.g4

javac LoopNest*.java

javac Driver.java
if [ $# -eq 0 ]
then
	echo "No testcases supplied, files compiled"
else
	java Driver "$1" "$2"
fi
#grun LoopNest tests -gui < $1

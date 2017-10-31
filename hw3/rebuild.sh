#!/bin/bash
rm wc.jar
rm build/*
javac -classpath `hadoop classpath` -d build src/*.java
jar -cvf wc.jar -C build/ .

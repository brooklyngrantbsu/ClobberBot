#!/bin/bash

# File to store the output
output_file="testOutput.txt"

# Clear the file before running the loop
> "$output_file"

javac brooklyngrant.java

for i in {1..10}
do
   echo "Running iteration $i" >> "$output_file"
   java Clobber -loadplayers plist -loadplayer brooklyngrant.class >> "$output_file" 2>&1
done

set -e

path_to_src=../src
compilation_dir=./compiled

# compile java source code
javac -cp "$path_to_src/main/java" \
      -d  "$compilation_dir"       \
          "$path_to_src/main/java/eliseev/pythoninterop/Main.java"

# create jar with Main class as entry point
jar cfe Main.jar eliseev.pythoninterop.Main -C "$compilation_dir" .

# delete compiled files if clean flag is provided
if [[ "$#" -gt 0 ]] && [[ "$1" = clean ]]; then
  rm -r "$compilation_dir"
fi
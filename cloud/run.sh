#!/bin/bash
# Використання: ./run.sh seq N | run N W | bench N
P=$HOME/labs/parcs/Bluck/out/parcs.jar
HOSTS=10.164.0.3
build() {
  mkdir -p build out
  cp $P out/parcs.jar
  echo $HOSTS > out/server
  javac -cp out/parcs.jar -d build src/*.java || exit 1
  jar cf out/HashWorker.jar -C build .
  jar cf out/HashMain.jar -C build .
}
case $1 in
  seq)   build; java -cp build Sequential ${2:-500000000} -1 | grep -E 'hash.A.|time' ;;
  run)   build; echo "${2:-500000000} ${3:-2} -1" > out/input
         (cd out && java -cp parcs.jar:HashMain.jar HashMain) ;;
  bench) build
         for w in 1 2 4 8; do
           echo "${2:-500000000} $w -1" > out/input
           for i in 1 2 3; do
             (cd out && java -cp parcs.jar:HashMain.jar HashMain | grep -E 'workers|time' | tr '\n' ' '); echo
           done
         done ;;
  *) echo "Використання: ./run.sh seq N | run N W | bench N" ;;
esac

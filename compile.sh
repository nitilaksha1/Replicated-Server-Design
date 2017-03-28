#!/bin/sh


thrift -r --gen java ReplicatedBankService.thrift

cd gen-java

javac -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" *.java

if [ $? -eq 0 ]
then
  echo "Done Compilation!"
fi

cd ..


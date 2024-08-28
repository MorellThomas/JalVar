currentdir=$(pwd)
command="java -jar ${currentdir}/build/libs/jalview-all-2.11.3.0-j11.jar"
echo $command > JalVar.command
mv JalVar.command ~/Desktop

# flink-pluralsight-course
Project for the 'Understanding Apache Flink' course on pluralsight

**Create Project Outline with Maven:** \
`mvn archetype:generate -DarchetypeGroupId=org.apache.flink -DarchetypeArtifactId=flink-quickstart-java -DarchetypeVersion=1.2.0 -DgroupId=flink-pluralsight-course -DartifactId=flink-pluralsight-course -Dversion=1.0 -Dpackage=com.pluralsight.flink
`

**Download Movielens Dataset**\
https://grouplens.org/datasets/movielens/


**Control Stream Input**\
If you want to open a socket for control messages on windows you can use [powercat](https://github.com/besimorhino/powercat):\
`PS C:\WINDOWS\system32> powercat -l 9876`
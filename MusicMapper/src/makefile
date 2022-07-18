run: Mapper.class Graph.class GraphADT.class 
	java Mapper

Graph.class: Graph.java
	javac Graph.java

GraphADT.class: GraphADT.java
	javac GraphADT.java

Mapper.class: Mapper.java
	javac Mapper.java

clean: 
	rm *.class
test: TestGraphCreation.class
	java -jar junit5.jar -cp . --scan-classpath

TestGraphCreation.class: TestGraphCreation.java
	javac -cp .:junit5.jar TestGraphCreation.java

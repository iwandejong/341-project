compile:
	javac *.java
run:
	java Main
clean:
	rm **/*.class
compile_executable:
	jar cvfm compiler.jar MANIFEST.MF -C . .
run_executable:
	java -jar compiler.jar
all:
	make clean
	make compile
	make compile_executable
	make run_executable
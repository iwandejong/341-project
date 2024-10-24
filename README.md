# ⌘ 341-project

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)

Develop a compiler for the RecSPL (Recursive Students' Programming Language). The language eliminates traditional looping constructs (like WHILE ), requiring that all iterations be achieved through recursion. The project will culminate in a fully functional compiler that can parse, analyze, and run RecSPL programs.

A compiler by Jacobus Smit (u21489476) and Iwan de Jong (u22498037)

## Contact Information:
- Github Repository: https://github.com/iwandejong/341-project
- Iwan de Jong: u22498037@tuks.co.za
- Jacobus Smit: u21489476@tuks.co.za

### How to run the program:
- Run the compiled program by running the following command in the terminal: ``` java -jar compiler.jar ```
- The program will ask you for the input file name. Enter the name of the file you want to compile. 
  - Note: The file must be in the same directory as the compiler.jar file to use the filename only (filename.txt). Otherwise, you must enter the full path of the file ( C:\files\testFile.txt).
- If the lexer and parser doesnt throw errors, the xml file will be output to "lexer_output_n.xml" and "parser_output_n.xml" where n is the number of the file.
- If there are errors in the file it will be shown in the terminal. If there are no errors, the program will run the code and output the intermediate code in "intermediate_code_n.txt" where n is the number of the file.
- Target code will be output to file "target_code_n.bas" where n is the number of the file.
- Target code will be run using the QB64 compiler and the output will be displayed in the terminal.

<span style="background-color: #FF0000; color: #FFF">Note: The QB64 compiler must be installed on the system to run the target code. It can be obtained from https://www.qb64.com</span>

## Input code rules: 
### Declaring variables:
- When declaring variables or functions seperate every token by a (" ") space.
    - This includes Tab key, Enter key, and extra spaces in between. 
- Variables must start with a capital letter V and followed by and underscore (_).
- if there are no Global variables, the begin token must be on the same line as the main or directly below it.
- Example of variable declaration: num V_a01 , num V_a02 , num      V_a03 , and so on.

### Declaring functions:
- When declaring variables or functions seperate every token by a (" ") space.
    - This includes Tab key, Enter key, and extra spaces in between.
- Functions must start with a capital letter F and followed by and underscore (_).
- Functions must contain 3 parameters.
- Example of function declaration: void F_func ( V_a01 , V_a02 , V_a03 ) ;

### Statements:
- When writing statements, seperate every token by a (" ") space.
- Example of assignment statement: V_a01 = 5 ; or V_a01 = V_a02 ; or V_a01 = "Hello World" ;
- Example of function call statement: F_func ( V_a01 , V_a02 , V_a03 ) ;
- Exmaple of assigning a function call to a variable: V_a01 = F_func ( V_a01 , V_a02 , V_a03 ) ;
    - Note: 
        - The function must be of type num if you are assigning it to a variable of type num. 
        - The function must be of type void if you are not assigning it to a variable.

### Extra whitespace considerations:
- The compiler will ignore extra whitespace in the code such as the Tab key, and extra spaces (" ") between tokens.
- There may not be a empty line between tokens. 

### Strings:
- Strings must be enclosed in double quotes. 
- There may be no spaces between the quotes and the string. 
- String must start with a capital letter.
- String may not be empty.
- Example: "Helowrld" ; - also note length of string is 8.
- Example of incorrect string: " Hello World " ; or "hello World" ; or "" ;


If any of the rules are not followed, the compiler will throw an error and the program will not run.

## Important notes:
### The following rules **doesn't** work:
- COMPOSIT ::= BINOP ( SIMPLE , SIMPLE )
- BINOP ::= or
- BINOP ::= and
- FUNCTIONS ::= SUBFUNCTIONS
### We have implemented the following rules:
- In the original language a function cannot return a value, however, in the type checking it stated that there exists a COMMAND rule such that a function can return a value. Hence we have went with the type checker language and functions such that a value can be returned (Local or Global).

## Wow-features:
- The compiler can handle multiple files at once.
- The compiler can adapt to a new language. We implemented the parser to check for the new language. Phase 3-5 however, will not work with the new language as it is not implemented in the type checker.
- The compiler can handle returning functions and function calls.


Example working code:
```
main
    num V_a01 ,
    num V_a02 ,
    num V_a03 ,
    num V_a04 ,
    begin
        V_a01 < input ;
        V_a02 = 20 ;
        V_a03 = sub ( V_a02 , 4 ) ;
        print V_a03 ;
        if eq ( V_a01 , V_a03 ) then
            begin
                skip ;
                print "Hellyeah" ;
            end
        else
            begin
                print "No" ;
                halt ;
            end ;
        V_a03 = V_a01 ;
        V_a04 = F_add ( V_a01 , V_a02 , V_a03 ) ;
        print V_a04 ;
        halt ;
    end
    num F_add ( V_a01 , V_a02 , V_a03 ) {
        num V_a04 , num V_a05 , num V_a06 ,
        begin
            V_a04 = add ( V_a01 , V_a02 ) ;
            return V_a04 ;
        end
    } end
    void F_foo ( V_a01 , V_a02 , V_a03 ) {
        num V_a04 , num V_a05 , num V_a06 ,
        begin
            V_a04 = V_a01 ;
            V_a05 = V_a02 ;
            V_a06 = mul ( V_a04 , V_a05 ) ;
            return V_a06 ;
        end
    } end
```

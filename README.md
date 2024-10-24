# ⌘ 341-project

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)

Develop a compiler for the RecSPL (Recursive Students' Programming Language). The language eliminates traditional looping constructs (like WHILE ), requiring that all iterations be achieved through recursion. The project will culminate in a fully functional compiler that can parse, analyze, and run RecSPL programs.

How to run the program:
- To input code, create a file with the extension .txt and write the code in the file. Do we prompt for files?
- Where do we output the results? prompt for file name? will it automatically output to output.txt?


Input code rules: 
- Declaring variables:
    - When declaring variables or functions seperate every token by a (" ") space.
        - This includes Tab key, Enter key, and extra spaces in between. 
    - Variables must start with a capital letter V and followed by and underscore (_).
    - Example of variable declaration: num V_a01 , num V_a02 , num      V_a01 , and so on.

- Declaring functions:
    - When declaring variables or functions seperate every token by a (" ") space.
        - This includes Tab key, Enter key, and extra spaces in between.
    - Functions must start with a capital letter F and followed by and underscore (_).
    - Functions must contain 3 parameters.
    - Example of function declaration: void F_func ( V_a01 , V_a02 , V_a03 ) ;

- Statements:
    - When writing statements, seperate every token by a (" ") space.
    - Example of assignment statement: V_a01 = 5 ; or V_a01 = V_a02 ; or V_a01 = "Hello World" ;
    - Example of function call statement: F_func ( V_a01 , V_a02 , V_a03 ) ;
    - Exmaple of assigning a function call to a variable: V_a01 = F_func ( V_a01 , V_a02 , V_a03 ) ;
        - Note: 
            - The function must be of type num if you are assigning it to a variable of type num. 
            - The function must be of type void if you are not assigning it to a variable.

- Extra whitespace considerations:
    - The compiler will ignore extra whitespace in the code such as the Enter key, Tab key, and extra spaces between tokens.

- Strings:
    - Strings must be enclosed in double quotes. 
    - There may be no spaces between the quotes and the string. 
    - String must start with a capital letter.
    - String may not be empty.
    - Example: "Hello World" ;
    - Eample of incorrect string: " Hello World " ; or "hello World" ; or "" ;


If any of the rules are not followed, the compiler will throw an error and the program will not run.

Important notes:
- SUBFUNCTIONS doesnt work.
- In the original language a function cannot return a value, however, in the type checking it stated that there exists a COMMAND rule such that a function can return a value. Hence we have went with the type checker language and functions such that a value can be returned (Local or Global).


Example code:
main
    num V_a01 ,
    num V_a02 ,
    num V_a03 ,
    num V_a04 ,
    begin
        V_a01 = 10 ;
        V_a02 = 20 ;
        if grt ( V_a02 , V_a01 ) then
            begin
                print "Yes" ;
            end
        else
            begin
                print "No" ;
            end ;
        V_a03 = V_a01 ;
        V_a04 = F_add ( V_a01 , V_a02 , V_a03 ) ;
        print V_a04 ;
        halt ;
    end
    num F_add ( V_a01 , V_a02 , V_a03 ) {
        num V_a04 , num V_a05 , num V_a06 ,
        begin
            V_a04 = V_a01 ;
            V_a05 = V_a02 ;
            V_a06 = add ( V_a04 , V_a05 ) ;
            F_foo ( V_a04 , V_a05 , V_a06 ) ;
        end
    } end
    void F_foo ( V_a01 , V_a02 , V_a03 ) {
        num V_a04 , num V_a05 , num V_a06 ,
        begin
            V_a04 = V_a01 ;
            V_a05 = V_a02 ;
            V_a06 = mul ( V_a04 , V_a05 ) ;
        end
    } end
package Analysis;
import java.util.Stack;

// stack to keep track of the scope
public class Scope_Stack {
    // Integer stack to keep track of the scope
    Stack<Integer> scopeStack = new Stack<Integer>();

    public Scope_Stack() {
        
    }

    // push the scope
    public void push(int scope) {
        scopeStack.push(scope);
    }

    // pop the scope
    public void pop() {
        scopeStack.pop();
    }

    public void printStack() {
        if(scopeStack.isEmpty()){
            System.out.println("The stack is empty. No functions.");
        }else{
            System.out.println(scopeStack);
        }
    }
}

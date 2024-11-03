// Starter code for Project 1

// jxc033200
//package dsa;

import java.util.List;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.File;

/** Class to store a node of expression tree
    For each internal node, element contains a binary operator
    List of operators: +|*|-|/|%|^
    Other tokens: (|)
    Each leaf node contains an operand (long integer)
*/

public class Expression {
    public enum TokenType {  // NIL is a special token that can be used to mark bottom of stack
	    PLUS, TIMES, MINUS, DIV, MOD, POWER, OPEN, CLOSE, NIL, NUMBER
    }
    
    public static class Token {
        TokenType token;
        int priority; // for precedence of operator
        Long number;  // used to store number of token = NUMBER
        String string;

        Token(TokenType op, int pri, String tok) {
            token = op;
            priority = pri;
            number = null;
            string = tok;
        }

        // Constructor for number.  To be called when other options have been exhausted.
        Token(String tok) {
            token = TokenType.NUMBER;
            number = Long.parseLong(tok);
            string = tok;
        }
        
        boolean isOperand() { return token == TokenType.NUMBER; }

        public long getValue() { return isOperand() ? number : 0; }

        public String toString() { return string; }
    }

    Token element;
    Expression left, right;

    // Create token corresponding to a string
    // tok is "+" | "*" | "-" | "/" | "%" | "^" | "(" | ")"| NUMBER
    // NUMBER is either "0" or "[-]?[1-9][0-9]*
    static Token getToken(String tok) {  // To do
        Token result;
        switch(tok) {
        case "+":
            result = new Token(TokenType.PLUS, 1, tok);  // modify if priority of "+" is not 1
            break;
            // Complete rest of this method
        case "-":
            result = new Token(TokenType.MINUS, 1, tok); 
            break;
        case "*":
            result = new Token(TokenType.TIMES, 2, tok); //increase priority by 1 if higher priority
            break;
        case "/":
            result = new Token(TokenType.DIV, 2, tok);
            break;
        case "%":
            result = new Token(TokenType.MOD, 2, tok);
            break;
        case "^":
            result = new Token(TokenType.POWER, 3, tok);
            break;
        case "(":
            result = new Token(TokenType.OPEN, 4, tok);
            break;
        case ")":
            result = new Token(TokenType.CLOSE, 4, tok); //4 is highest priority
            break;
        default:
            result = new Token(tok);
            break;
        }
        return result;
    }
    
    private Expression() {
	    element = null;
    }
    
    private Expression(Token oper, Expression left, Expression right) {
        this.element = oper;
        this.left = left;
        this.right = right;
    }

    private Expression(Token num) {
        this.element = num;
        this.left = null;
        this.right = null;
    }

    /**  Converts infix expression to expression tree
     * <p>
     * Given a list of tokens corresponding to an infix expression,
     * return the expression tree corresponding to it.
     * <p>
     * The method will take as parameter a List of tokens read from an input file,
     * in this case, a linked list. It instantiates two stacks (ArrayDeques), 
     * one for the operator, and one for the expression tree. An iterator is used to 
     * traverse through the list of tokens.
     * <p>
     * As long as we have not reached the end of the list, 
     * Operands are pushed to the expression stack with no conditions.
     * Operators are pushed from the list to the operator stack unless the top of 
     * the stack has a higher priority operator, in which we build a subtree with the node being operator
     * and children being popped from the expression stack. When the subtree is established, we push it back
     * into the top of the stack. When open parenthesis are encountered, priority 
     * is increased to the highest - multiplied by 5 to ensure that even the lowest priority (1) 
     * will exceed priority 4. When close parenthesis are encountered, we follow the algorithm
     * as described below, popping from the operator and expression tree to create 
     * a node with the left and right children popped from the expression stack. We then push this subtree 
     * back into the expression stack. 
     * <p>
     * @param exp - the list of tokens in infix form
     * @return expression.pop() - returns the expression tree
     * 
    */
    public static Expression infixToExpression(List<Token> exp) {  // To do
        Deque<Token> operator = new ArrayDeque<>(); //a stack for operators
        Deque<Expression> expression = new ArrayDeque<>(); //a stack for the expression tree
        operator.push(new Token(TokenType.NIL, 0, "")); //NIL as marker for bottom        
        Iterator<Token> iter = exp.iterator(); //iterator to traverse through the list
        /**
         * precedence variable changes from 1 to 5 when in parenthesis. Multiplied by the priority
         * for all tokens pushed into the operator stack to distinguish when parenthesis are involved
         */
        int precedence = 1; //default precedence, mult 5 when parenthesis are involved to get priority to max (above 4)
        while (iter.hasNext()) { //while the list still has a next element
            Token token = iter.next();
            if (token.isOperand()) { //if token is a number, push into expression tree
                expression.push(new Expression(token));                
            } else {
                if (token.token == TokenType.OPEN) { //if it's open parenth 
                    precedence = precedence * 5; //increase precedence for elements until close parenthesis
                    operator.push(token);
                } else if (token.token == TokenType.CLOSE) { //builds tree until matching parenthesis
                    while (operator.peek().token != TokenType.OPEN) { //while top of stack is not open parenthesis
                        Expression right = expression.pop(); //build node with operator and top two in expression stack as children
                        Expression left = expression.pop();
                        Expression tree = new Expression(operator.pop(), left, right);
                        expression.push(tree); //push subtree into expression stack
                    }
                    operator.pop(); //pop the open parenthesis
                    precedence = precedence/5; //reset precedence
                } else if (token.priority*precedence <= operator.peek().priority) { //if token has less priority than top of operator stack
                    while(operator.peek().priority >= token.priority * precedence) { //while top of stack has higher/equal priority as the token
                        Expression right = expression.pop(); //build expression subtree with operator as root
                        Expression left = expression.pop();
                        Expression tree = new Expression(operator.pop(), left, right);
                        expression.push(tree); //push subtree into expression stack
                    }
                    token.priority = token.priority * precedence; //adjust precedence and push current token in operator stack
                    operator.push(token);
                } else {
                    token.priority = token.priority * precedence; 
                    operator.push(token);
                }
            }
        }
        while (operator.peek().token != TokenType.NIL) { //forming the rest of the tree
            Expression right = expression.pop();
            Expression left;
            if (expression.isEmpty()) { //edge case if there are no more operands
                left = new Expression(new Token("0"));
            } else {
                left = expression.pop();
            }
            Expression tree = new Expression(operator.pop(), left, right); //builds another layer with operator as root
            expression.push(tree);
        }
	    return expression.pop();
    }

    /**  Converts infix expression to postfix
     * <p>
     * Given a list of tokens corresponding to an infix expression,
     * return the equivalent postfix expression as a list of tokens.
     * <p>
     * The method will take as parameter a List of tokens read from an input file,
     * in this case, a linked list. It instantiates an ArrayDeque for the operator, 
     * and a linked list for the postfix expression. an Iterator is used to traverse 
     * through the list of tokens.
     * <p>
     * As long as we have not reached the end of the list, operands are pushed to 
     * the postfix list with no conditions. Operators are pushed from the list to 
     * the operator stack unless the top of the stack has a higher priority operator, 
     * in which we follow the algorithm described in class and below. When open 
     * parenthesis are encountered, priority is increased to the highest - multiplied 
     * by 5 to ensure that even the lowest priority (1) will exceed priority 4. When 
     * close parenthesis are encountered, we follow the algorithm as described below.
     * <p>
     * @param exp - the list of tokens in infix form
     * @return postFix - returns the postFix linked list
     * 
    */
    public static List<Token> infixToPostfix(List<Token> exp) {  // To do
        Deque<Token> operator = new ArrayDeque<>(); //operator stack
        List<Token>postFix = new LinkedList<>(); //postfix stack
        operator.push(new Token(TokenType.NIL, 0, "")); //marks bottom of stack        
        Iterator<Token>iter = exp.iterator(); 
        /**
        * precedence variable changes from 1 to 5 when in parenthesis. Multiplied by the priority
        * for all tokens pushed into the operator stack to distinguish when parenthesis are involved
        */
        int precedence = 1;
        while (iter.hasNext()) { //while list has next element
            Token token = iter.next();
            if(token.isOperand()) { //if token is an operand, add it to list
                postFix.add(token);
            }
            else {
                if (token.token == TokenType.OPEN) { //open parenthesis, increase precedence
                    precedence = precedence*5;
                    operator.push(token);   //push it to the stack
                } else if (token.token == TokenType.CLOSE) { //closing parenthesis 
                    while (operator.peek().token != TokenType.OPEN) { //pop from stack and add to postFix until hitting open parenthesis
                        postFix.add(operator.pop());
                    }
                    operator.pop(); //get rid of open parenthesis
                    precedence = precedence/5; //reset precedence
                } else if (token.priority * precedence <= operator.peek().priority) { //if operator has less priority than top of stack
                    while(operator.peek().priority >= token.priority*precedence) { //while top of stack has higher priority
                        postFix.add(operator.pop()); //pop and add to postFix
                    }
                    token.priority = token.priority*precedence; //set precedence and push to stack
                    operator.push(token);
                } else {
                    token.priority = token.priority*precedence; 
                    operator.push(token);
                }
            }
        }
        while (operator.peek().token != TokenType.NIL) { //pop remaining operators
            postFix.add(operator.pop());
        }
	    return postFix;
    }
    /**Evaluates a postfix expression
     * <p>
     * Given a postfix expression, evaluate it and return its value.
     * <p>
     * The method will take as parameter a postfix expression, in this case the 
     * linked list returned from the infixToPostfix method. It instantiates an ArrayDeque 
     * for the operand and an iterator to traverse through the list of tokens.
     * <p>
     * Operands are pushed to the operand stack. When reaching an operator, we pop the 
     * top two operands and perform the operation on them according to tokenType attribute.
     * After this operation is done, we push the result back into the stack and continue
     * the process with the rest of the tokens.
     * <p>
     * @param exp - the list of tokens in postfix form
     * @return operand.pop().number - returns the evaluated value
     * 
    */    
    public static long evaluatePostfix(List<Token> exp) {  // To do
        Deque<Token> operand = new ArrayDeque<>(); //make a stack of operands
        Iterator<Token> iter = exp.iterator();        
        while (iter.hasNext()) {
            Token token = iter.next();
            if (token.isOperand()) { //push operands to stack until reaching an operator
                operand.push(token);
            }
            else {
                long result = 0; //when evaluating an operator, two operands are popped
                long right = operand.pop().number;
                long left = operand.pop().number;
                switch(token.token) { //operations for each operator
                    case PLUS: 
                        result = left+right;
                        break;
                    case MINUS:
                        result = left-right;
                        break;
                    case TIMES:
                        result = left*right;
                        break;                    
                    case DIV:
                        result = left/right;
                        break;
                    case MOD:
                        result = left%right;
                        break;
                    case POWER:
                        result = (long)Math.pow(left, right);
                        break;
                    default:
                }
                operand.push(new Token("" + result)); //the result is pushed back into the stack 
            }
        }
	    return operand.pop().number;
    }

    /**Evaluates an expression tree
     * <p>
     * Given an expression tree, evaluate it and return its value.
     * <p>
     * The method will take as parameter the tree created in the infixtoExpression method.
     * It recursively traverses the tree until reaching left and right leaves, assigning 
     * the values to left and right. The switch statement evaluates the node according to 
     * the assigned tokenType, then recursively repeats until all nodes are visited.
     * @param exp - the expression tree
     * @return result - returns the evaluated value
     * 
    */        
    public static long evaluateExpression(Expression tree) {  // To do
        if (tree.left == null && tree.right == null) //if left and right are null, the node is a leaf (operand)
            return tree.element.number;
        else {
            long result = 0;
            long left = evaluateExpression(tree.left); //recursively traverse tree starting with LEFT
            long right = evaluateExpression(tree.right);
            switch(tree.element.token) { //switch statement for operations on internal node
                case PLUS:
                    result = left+right;
                    break;
                case MINUS:
                    result = left-right;
                    break;
                case TIMES:
                    result = left*right;
                    break;                
                case DIV:
                    result = left/right;
                    break;
                case MOD:
                    result = left%right;
                    break;
                case POWER:
                    result = (long)Math.pow(left, right);
                    break;    
                default:                       
            }
        return result;
        }	    
    }

    // sample main program for testing
    public static void main(String[] args) throws FileNotFoundException {
        Scanner in;
        
        if (args.length > 0) {
            File inputFile = new File("p1 testcases.txt");
            in = new Scanner(inputFile);
        } else {
            in = new Scanner(System.in);
        }
        in = new Scanner(new File("p1 testcases.txt"));
        int count = 0;
        while(in.hasNext()) {
            String s = in.nextLine();
            List<Token> infix = new LinkedList<>();
            Scanner sscan = new Scanner(s);
            int len = 0;
            while(sscan.hasNext()) {
                infix.add(getToken(sscan.next()));
                len++;
            }
            if(len > 0) {
                count++;
                System.out.println("Expression number: " + count);
                System.out.println("Infix expression: " + infix);
                Expression exp = infixToExpression(infix);
                List<Token> post = infixToPostfix(infix);
                System.out.println("Postfix expression: " + post);
                long pval = evaluatePostfix(post);
                long eval = evaluateExpression(exp);
                System.out.println("Postfix eval: " + pval + " Exp eval: " + eval + "\n");
            }
        }
    }
}
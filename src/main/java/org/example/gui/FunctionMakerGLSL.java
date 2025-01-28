package org.example.gui;


import java.util.Objects;
import java.util.Stack;


public class FunctionMakerGLSL {
    public static void main(String[] args) {
        FunctionMakerGLSL test = new FunctionMakerGLSL("q--1*((q^3)-1)/(3*q^2)");
    }

    private final Stack<String> sortedTokenStack = new Stack<>();
    private final Stack<String> tokenStack = new Stack<>();
    private final String function;
    public String code;
    public int highestPolynomial = 2;

    public FunctionMakerGLSL(String _function) {
        function = _function;
        stringToTokenStack();
        operationSorter();
        code = makeCode();
    }

    public String makeCode() {
        Stack<String> calculator = new Stack<>();
        Stack<String> _sortedTokenStack = reverseStack(sortedTokenStack);

        while (!_sortedTokenStack.isEmpty()) {
            String savedA = "", savedB = "";
            if (_sortedTokenStack.peek().charAt(0) != '_') {
                if (_sortedTokenStack.peek().equals("sin") || _sortedTokenStack.peek().equals("cos") || _sortedTokenStack.peek().equals("exp") || _sortedTokenStack.peek().equals("abs")) {
                    savedA = calculator.peek();
                    calculator.pop();
                    switch (_sortedTokenStack.peek()) {
                        case "sin" -> calculator.push("qsin(" + savedA + ")");
                        case "cos" -> calculator.push("qcos(" + savedA + ")");
                        case "exp" -> calculator.push("qexp(" + savedA + ")");
                        case "abs" -> calculator.push("abs(" + savedA + ")");
                    }
                    _sortedTokenStack.pop();
                } else {
                    calculator.push(_sortedTokenStack.peek());
                    _sortedTokenStack.pop();
                }
            } else {
                savedA = calculator.peek();
                calculator.pop();
                savedB = calculator.peek();
                calculator.pop();
                switch (_sortedTokenStack.peek()) {
                    case "_qmul" -> calculator.push("qmul(" + savedB + "," + savedA + ")");
                    case "_qdiv" -> calculator.push("qdiv(" + savedB + "," + savedA + ")");
                    case "_qpow" -> calculator.push("qpow(" + savedB + "," + savedA + ")");
                    case "_minus" -> calculator.push(savedB + "-" + savedA);
                    case "_plus" -> calculator.push(savedB + "+" + savedA);
                }
                _sortedTokenStack.pop();
            }
        }
        String glslFinal = "";
        calculator = reverseStack(calculator);
        while (!calculator.isEmpty()) {
            glslFinal = glslFinal + calculator.peek();
            calculator.pop();
        }
        return glslFinal;
    }


    private void operationSorter() {
        Stack<String> opStack = new Stack<>();
        Stack<String> _inputStack = reverseStack((Stack<String>) tokenStack.clone());

        while (!_inputStack.isEmpty()) {
            if (_inputStack.peek().charAt(0) != '_') {
                if (_inputStack.peek().equals("sin") || _inputStack.peek().equals("cos") || _inputStack.peek().equals("exp") || _inputStack.peek().equals("abs")) {
                    opStack.push(_inputStack.peek());
                } else {
                    sortedTokenStack.push(_inputStack.peek());
                }
                _inputStack.pop();
            } else if (_inputStack.peek().charAt(0) == '_') {
                if (Objects.equals(_inputStack.peek(), "_(")) {
                    opStack.push(_inputStack.peek());
                    _inputStack.pop();
                } else if (Objects.equals(_inputStack.peek(), "_)")) {
                    while (!Objects.equals(opStack.peek(), "_(")) {
                        sortedTokenStack.push(opStack.peek());
                        opStack.pop();
                    }
                    _inputStack.pop();
                    opStack.pop();
                } else {
                    while (!opStack.isEmpty() && ((operatorPrecedence(opStack) > operatorPrecedence(_inputStack)) || (operatorPrecedence(opStack) == operatorPrecedence(_inputStack) && Objects.equals(_inputStack.peek(), "_qpow")))) {
                        sortedTokenStack.push(opStack.peek());
                        opStack.pop();
                    }
                    opStack.push(_inputStack.peek());
                    _inputStack.pop();
                }
            }
        }
        while (!opStack.isEmpty()) {
            sortedTokenStack.push(opStack.peek());
            opStack.pop();
        }
    }


    private void stringToTokenStack() {
        String functionCopy = function + "$";
        boolean isPower = false;
        String longToken = "";
        boolean needCloseParanthesis = false;

        while (!functionCopy.isEmpty()) {
            if (needCloseParanthesis) {
                while (functionCopy.charAt(0) != ')') {
                    longToken = longToken + functionCopy.charAt(0);
                    functionCopy = functionCopy.substring(1);
                }
                tokenStack.push(longToken);
                needCloseParanthesis = false;
            }

            if (functionCopy.charAt(0) >= '0' && functionCopy.charAt(0) <= '9' || functionCopy.charAt(0) == '.' || functionCopy.charAt(0) == 't') {
                if (functionCopy.charAt(0) == 't') {
                    longToken = "timeSin";
                    functionCopy = functionCopy.substring(1);
                }
                while (!functionCopy.isEmpty() && (functionCopy.charAt(0) >= '0' && functionCopy.charAt(0) <= '9' || functionCopy.charAt(0) == '.')) {
                    longToken = longToken + functionCopy.charAt(0);
                    functionCopy = functionCopy.substring(1);
                }
                if (!isPower) {
                    longToken = "vec4(" + longToken + ",0,0,0)";
                } else {
                    highestPolynomial = Math.max(highestPolynomial, Integer.parseInt(longToken));
                }
                tokenStack.push(longToken);
                longToken = "";
            } else if (functionCopy.startsWith("quant(")) {
                functionCopy = functionCopy.substring(6);
                longToken = "vec4(";
                while (functionCopy.charAt(0) != ')') {
                    if (functionCopy.charAt(0) == 't') {
                        longToken = longToken + "timeSin";
                    } else {
                        longToken = longToken + functionCopy.charAt(0);
                    }
                    functionCopy = functionCopy.substring(1);
                }
                longToken = longToken + ")";
                tokenStack.push(longToken);
                if (!functionCopy.isEmpty()) functionCopy = functionCopy.substring(1);
            } else if (functionCopy.startsWith("sin") || functionCopy.startsWith("cos") || functionCopy.startsWith("exp") || functionCopy.startsWith("abs")) {
                if (functionCopy.startsWith("sin")) tokenStack.push("sin");
                if (functionCopy.startsWith("cos")) tokenStack.push("cos");
                if (functionCopy.startsWith("exp")) tokenStack.push("exp");
                if (functionCopy.startsWith("abs")) tokenStack.push("abs");
                functionCopy = functionCopy.substring(3);
            } else {
                if (isPower && functionCopy.charAt(0) == '(') {
                    needCloseParanthesis = true;
                } else {
                    isPower = false;
                }
                longToken = "";
                if (functionCopy.charAt(0) == '-' && (tokenStack.isEmpty() || (tokenStack.peek().charAt(0) == '_' && tokenStack.peek().length() > 2))) {
                    longToken = "-";
                } else if (functionCopy.charAt(0) == 'q') {
                    tokenStack.push("q");
                } else if (functionCopy.charAt(0) == 'c') {
                    tokenStack.push("c");
                } else if (functionCopy.charAt(0) == 'n') {
                    tokenStack.push("n");
                } else switch (functionCopy.charAt(0)) {
                    case '+' -> tokenStack.push("_plus");
                    case '-' -> tokenStack.push("_minus");
                    case '*' -> tokenStack.push("_qmul");
                    case '/' -> tokenStack.push("_qdiv");
                    case '(' -> tokenStack.push("_(");
                    case ')' -> tokenStack.push("_)");
                    case '^' -> {
                        tokenStack.push("_qpow");
                        isPower = true;
                    }
                }
                functionCopy = functionCopy.substring(1);
            }
        }
    }

    private int operatorPrecedence(Stack<String> stack) {
        int operatorPrecedence = 0;
        switch (stack.peek()) {
            case "_plus", "_minus" -> operatorPrecedence = 2;
            case "_qmul", "_qdiv" -> operatorPrecedence = 3;
            case "_qpow" -> operatorPrecedence = 5;
            case "_(" -> operatorPrecedence = 1;
            case "_)" -> operatorPrecedence = 10;
        }
        return operatorPrecedence;
    }

    private static Stack<String> reverseStack(Stack<String> notReversed) {
        Stack<String> reverse = new Stack<>();
        while (!notReversed.isEmpty()) {
            reverse.push(notReversed.peek());
            notReversed.pop();
        }
        return reverse;
    }
}
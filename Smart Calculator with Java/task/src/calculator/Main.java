package calculator;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.exit;

public class Main {

    public static boolean numberPattern(String input) {
        Pattern pattern = Pattern.compile("[+-]*\\s*\\d+\\s*[()+-/*^\\d\\s]*");
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    public static boolean singleVariable(String input) {
        Pattern unknownVarMatcher = Pattern.compile("[a-zA-Z]+");
        Matcher matcher = unknownVarMatcher.matcher(input);
        return matcher.matches();
    }

    public static boolean variablePattern(String input) {
        Pattern operatorPattern = Pattern.compile("[+-]*\\s*[a-zA-Z0-9]+\\s+[()+-/*^a-zA-Z0-9\\s]*");
        Matcher operatorMatcher = operatorPattern.matcher(input);
        return operatorMatcher.matches();
    }

    public static boolean invalidMulDivPattern(String input) {
        Pattern invalidMultDiv = Pattern.compile("[*/^]{2,}");
        Matcher invalidMultDivMatcher = invalidMultDiv.matcher(input.replaceAll("\\s+", ""));
        return invalidMultDivMatcher.find();
    }

    public static boolean initialAssignmentPattern(String input) {
        Pattern assignmentPattern = Pattern.compile("[a-zA-Z]+\\s*=\\s*[\\s+-]*\\d+");
        Matcher assignmentMatcher = assignmentPattern.matcher(input);
        return assignmentMatcher.matches();
    }

    public static boolean variableToVariableAssignment(String input) {
        Pattern varToVarPattern = Pattern.compile("[a-zA-Z]+\\s*=\\s*[a-zA-Z]+");
        Matcher varToVarMatcher = varToVarPattern.matcher(input);
        return varToVarMatcher.matches();
    }

    public static boolean unknownCommand(String input) {
        Pattern unknownPattern = Pattern.compile("/.*?");
        Matcher unknownMatcher = unknownPattern.matcher(input);
        return unknownMatcher.matches();
    }

    public static boolean bracketIsBalanced(String input) {
        Stack<Character> brackets = new Stack<>();
        for (char c : input.toCharArray()) {
            if (c == '(') {
                brackets.push(c);
            } else if (c == ')') {
                if (brackets.isEmpty()) {
                    return false;
                }
                brackets.pop();
            }
        }
        return brackets.isEmpty();
    }

    public static BigInteger numberOperation(String input) {
        String formatedInput = input.trim().replaceAll("--", "+")
                .replaceAll("[+]+", "+").replaceAll("\\+-", "-");
        String[] numbers = formatedInput.split(" ");
        if (numbers.length == 1) {
            return new BigInteger(numbers[0]);
        } else {
            return evaluateExpression(formatedInput);
        }
    }

    public static BigInteger variableOperation(String input, Map<String, BigInteger> map) {
        String formatedInput = input.trim().replaceAll("--", "+")
                .replaceAll("[+]+", "+").replaceAll("\\+-", "-")
                .replaceAll("\\(", " ( ").replaceAll("\\)", " ) ")
                .replaceAll("\\s+", " ");
        String[] formatedArray = formatedInput.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String formated : formatedArray) {
            if (formated.equals("+") || formated.equals("-") || formated.equals("*") || formated.equals("/")
                || formated.equals("(") || formated.equals(")") || formated.equals("^")) {
                sb.append(formated).append(" ");
            } else if (map.containsKey(formated)) {
                sb.append(map.get(formated)).append(" ");
            } else {
                sb.append(formated).append(" ");
            }
        }
        String formated = sb.toString();
        return evaluateExpression(formated);
    }

    public static BigInteger evaluateExpression(String stringExpression) {
        String formatedInput = stringExpression.replaceAll("\\s+", "")
                .replaceAll("--", "+").replaceAll("[+]+", "+")
                .replaceAll("\\+-", "-");
        Stack<BigInteger> valueStack = new Stack<>();
        Stack<Character> operatorStack = new Stack<>();
        char[] stringArray = formatedInput.toCharArray();
        for (int i = 0; i < stringArray.length; i++) {
            if (stringArray[i] == ' ') {
                continue;
            }
            if (Character.isDigit(stringArray[i])) {
                StringBuilder sb = new StringBuilder();
                while (i < stringArray.length && Character.isDigit(stringArray[i])) {
                    sb.append(stringArray[i++]);
                }
                valueStack.push(new BigInteger(sb.toString()));
                i--;
            } else if (stringArray[i] == '(') {
                operatorStack.push(stringArray[i]);
            } else if (stringArray[i] == ')') {
                while (operatorStack.peek() != '(') {
                    valueStack.push(applyOperator(operatorStack.pop(), valueStack.pop(), valueStack.pop()));
                }
                operatorStack.pop();
            } else if (stringArray[i] == '+' || stringArray[i] == '-' || stringArray[i] == '*' || stringArray[i] == '/'
                       || stringArray[i] == '^') {
                while (!operatorStack.isEmpty() && precedence(stringArray[i], operatorStack.peek())) {
                    valueStack.push(applyOperator(operatorStack.pop(), valueStack.pop(), valueStack.pop()));
                }
                operatorStack.push(stringArray[i]);
            }
        }
        while (!operatorStack.isEmpty()) {
            valueStack.push(applyOperator(operatorStack.pop(), valueStack.pop(), valueStack.pop()));
        }
        return valueStack.pop();
    }

    public static boolean precedence(char operator1, char operator2) {
        if (operator2 == '(' || operator2 == ')') {
            return false;
        }
        return (operator1 != '*' && operator1 != '/') || (operator2 != '+' && operator2 != '-');
    }

    public static BigInteger applyOperator(char operator, BigInteger value2, BigInteger value1) {
        return switch (operator) {
            case '+' -> value1.add(value2);
            case '-' -> value1.subtract(value2);
            case '*' -> value1.multiply(value2);
            case '/' -> {
                if (value2.equals(BigInteger.ZERO)) {
                    throw new UnsupportedOperationException("Cannot divide by zero");
                }
                yield value1.divide(value2);
            }
            case '^' -> {
                BigInteger pow = value1;
                for (BigInteger i = BigInteger.ONE; i.compareTo(value2) < 0; i = i.add(BigInteger.ONE)) {
                    pow = pow.multiply(value1);
                }
                yield pow;
            }
            default -> BigInteger.ZERO;
        };
    }

    public static String input(Scanner scanner, Map<String, BigInteger> map) {
        String input = scanner.nextLine().trim().replaceAll("\\s+", " ");

        if (input.equals("/exit")) {
            System.out.println("Bye!");
            exit(0);
        } else if (input.equals("/help")) {
            System.out.println("""
                     The program calculates the addition, subtraction, division, multiplication and power of numbers,\s
                     including BigIntegers. It can also assign and store the numbers in separate variables provided\s
                     through assignment operator such as "a = 17628368", and calculate the complex result using stored\s
                     variables as well. It uses HashMap, so the process of store and retrieving the values of the\s
                     corresponding variables are very efficient.\s
                    \s
                     To get started simply declare your variable with its corresponding values in new line, such as\s
                     input = 76187268".\s
                    \s
                     To get the result, simply input the desired expression such as\s
                     57 + 76172 - (a * 187829), and it will output the result.\s
                    \s
                     You can also use parenthesis () to set the precedence.\s
                    \s
                     Finally, to exit the calculator, simply type /exit, and that should stop the program.\s""");
            input = input(scanner, map);
        } else if (unknownCommand(input)) {
            System.out.println("Unknown command");
            input = input(scanner, map);
        } else if (input.isEmpty()) {
            input = input(scanner, map);
        } else if (!bracketIsBalanced(input) || invalidMulDivPattern(input)) {
            System.out.println("Invalid expression");
            input = input(scanner, map);
        } else if (initialAssignmentPattern(input)) {
            String[] formatedString = input.replaceAll("\\s+", "").split("=");
            map.put(formatedString[0], new BigInteger(formatedString[1]));
            input = input(scanner, map);
        } else if (variableToVariableAssignment(input) && map.containsKey(input.replaceAll("\\s+", "")
                .split("=")[1])) {
            String[] variable = input.replaceAll("\\s+", "").split("=");
            map.put(variable[0], map.get(variable[1]));
            input = input(scanner, map);
        } else if (variableToVariableAssignment(input) && !map.containsKey(input
                .replaceAll("\\s+", "").split("=")[1])) {
            System.out.println("Invalid assignment");
            input = input(scanner, map);
        } else if (singleVariable(input) && !map.containsKey(input)) {
            System.out.println("Unknown variable");
            input = input(scanner, map);
        } else if (numberPattern(input)) {
            System.out.println(numberOperation(input));
            input = input(scanner, map);
        } else if (variablePattern(input)) {
            System.out.println(variableOperation(input, map));
            input = input(scanner, map);
        } else if (singleVariable(input) && map.containsKey(input)) {
            System.out.println(map.get(input));
            input = input(scanner, map);
        }

        if (!numberPattern(input) && !singleVariable(input) && !variablePattern(input)
            && !variableToVariableAssignment(input) && !unknownCommand(input)) {
            System.out.println("Invalid identifier");
            input = input(scanner, map);
        }

        return input;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Map<String, BigInteger> map = new HashMap<>();
        input(scanner, map);
    }
}

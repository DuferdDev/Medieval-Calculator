import java.util.ArrayList;

public class Expression {
	private String expressionString;

	private ArrayList<Expression> expressions;
	private String operation;

	public Expression(String _expressionString) {
		expressionString = _expressionString.replaceAll(" ", "").replaceAll("\t", "");
		if (expressionString == null) {
			expressionString = "";
		}
		while (hasBracketsAround()) {
			expressionString = expressionString.substring(1, expressionString.length() - 1);
		}
		if (expressionString.startsWith("+")) {
			expressionString = expressionString.substring(1);
		}

		expressions = new ArrayList<>();
		operation = "";
	}

	private boolean hasBracketsAround() {
		return hasBracketsAround(expressionString);
	}

	private boolean hasBracketsAround(String target) {
		if (target.length() < 1 || target.charAt(0) != '(') {
			return false;
		}
		int brackets = 0;
		for (int i = 0; i < target.length(); i++) {
			if (target.charAt(i) == '(') {
				brackets++;
			} else if (target.charAt(i) == ')') {
				brackets--;
				if (brackets == 0) {
					return i == target.length() - 1;
				}
			}
		}
		return false;
	}

	public double getValue() {
		double result = Double.NaN;
		System.out.println("Got value: " + expressionString);
		try {
			result = Double.parseDouble(expressionString);
		} catch (NumberFormatException ignored) {
			ArrayList<String> subexpressionStrings = getIsolatedSubexpressionStrings();
			simplifyExpression(subexpressionStrings);
			result = calculateExpression();
		}
		return result;
	}

	public double getValue(int decimalCount) {
		return Math.floor(getValue() * Math.pow(10, decimalCount)) / Math.pow(10, decimalCount);
	}

	private double calculateExpression() {
		if (expressions.isEmpty()) {
			return Double.NaN;
		}

		double[] exps = new double[expressions.size()];
		for (int i = 0; i < exps.length; i++) {
			exps[i] = expressions.get(i).getValue();
		}

		if (operation.isEmpty()) {
			return exps[0];
		}

		return switch (operation) {
		case "+" -> exps[0] + exps[1];
		case "-" -> exps[0] - exps[1];
		case "*" -> exps[0] * exps[1];
		case "/" -> exps[0] / exps[1];
		case "^" -> Math.pow(exps[0], exps[1]);
		case "neg" -> -exps[0];
		case "sqrt" -> Math.sqrt(exps[0]);
		case "abs" -> Math.abs(exps[0]);
		case "sin" -> Math.sin(exps[0]);
		case "cos" -> Math.cos(exps[0]);
		case "tan" -> Math.tan(exps[0]);
		case "cot" -> Math.cos(exps[0]) / Math.sin(exps[0]);
		case "rad" -> Math.toRadians(exps[0]);
		case "deg" -> Math.toDegrees(exps[0]);
		case "round" -> Math.round(exps[0]);
		case "floor" -> Math.floor(exps[0]);
		case "ceil" -> Math.ceil(exps[0]);
		default -> Double.NaN;
		};
	}

	private void simplifyExpression(ArrayList<String> subexpressionStrings) {

		int plusIndex = subexpressionStrings.lastIndexOf("+");
		int minusIndex = subexpressionStrings.lastIndexOf("-");
		int multIndex = subexpressionStrings.lastIndexOf("*");
		int divIndex = subexpressionStrings.lastIndexOf("/");

		int[] operationIndexes = new int[] {
				(plusIndex > -1 && minusIndex > -1) ? Math.min(plusIndex, minusIndex)
						: Math.max(plusIndex, minusIndex),
				(multIndex > -1 && divIndex > -1) ? Math.min(multIndex, divIndex)
						: Math.max(multIndex, divIndex),
				subexpressionStrings.indexOf("^"), };

		for (int i = 0; i < operationIndexes.length; i++) {
			if (operationIndexes[i] != -1) {
				extractExpressionsWithOperation(subexpressionStrings, operationIndexes[i]);
				return;
			}
		}

		if (!subexpressionStrings.isEmpty()) {
			String funStr = subexpressionStrings.get(0);
			if (funStr.contains("(")) {
				int bracketIndex = funStr.indexOf('(');
				String functionString = funStr.substring(0, bracketIndex);
				String functionParameter = funStr.substring(bracketIndex);
				if (hasBracketsAround(functionParameter)) {
					if (functionString.equals("-")) {
						operation = "neg";
					} else {
						operation = functionString;
					}
					expressions.add(new Expression(functionParameter));
				}
			} else {
				expressions.add(new Expression(String.valueOf(resolveConstant(funStr))));
			}
		}
	}

	private double resolveConstant(String constantString) {
		double result = switch (constantString.replace("-", "").replace("+", "")) {
		case "pi" -> Math.PI;
		case "tau" -> Math.TAU;
		case "e" -> Math.E;
		case "phi", "goldenRatio", "gratio", "gRatio" -> (1.0 + Math.sqrt(5.0)) / 2.0;
		default -> Double.NaN;
		};
		if (constantString.startsWith("-")) {
			result = -result;
		}
		return result;
	}

	private void extractExpressionsWithOperation(ArrayList<String> subexpressionStrings,
			int operatorIndex) {
		String expStr0 = "";
		for (int i = 0; i < operatorIndex; i++) {
			expStr0 += subexpressionStrings.get(i);
		}
		String expStr1 = "";
		for (int i = operatorIndex + 1; i < subexpressionStrings.size(); i++) {
			expStr1 += subexpressionStrings.get(i);
		}
		Expression e0 = new Expression(expStr0);
		Expression e1 = new Expression(expStr1);
		expressions.add(e0);
		expressions.add(e1);
		operation = subexpressionStrings.get(operatorIndex);
	}

	public void listAllsubexpressionStrings() {
		ArrayList<String> subexpressionStrings = getIsolatedSubexpressionStrings();
		for (int i = 0; i < subexpressionStrings.size(); i++) {
			System.out.println(subexpressionStrings.get(i));
		}
		System.out.println("-----");
		for (int i = 0; i < expressions.size(); i++) {
			System.out.println(expressions.get(i).expressionString);
		}
		System.out.printf("O: %s\n", operation);
		System.out.println("----------");
	}

	public ArrayList<String> getIsolatedSubexpressionStrings() {
		ArrayList<String> subexpressionStrings = new ArrayList<>();

		String currentStringPart = "";
		int insideBrackets = 0;
		for (int i = 0; i < expressionString.length(); i++) {
			char curc = expressionString.charAt(i);
			switch (curc) {
			case '(':
				insideBrackets++;
				currentStringPart += curc;
				break;
			case ')':
				insideBrackets--;
				currentStringPart += curc;
				break;
			case '+', '*', '/', '^':
				if (insideBrackets > 0) {
					currentStringPart += curc;
				} else {
					subexpressionStrings.add(currentStringPart);
					currentStringPart = "";
					currentStringPart += curc;
					subexpressionStrings.add(currentStringPart);
					currentStringPart = "";
				}
				break;
			case '-':
				// if (insideBrackets > 0) {
				// currentStringPart += curc;
				// } else {
				// if (currentStringPart.isEmpty()) {
				// currentStringPart += curc;
				// } else {
				// subexpressionStrings.add(currentStringPart);
				// currentStringPart = "";
				// currentStringPart += curc;
				// subexpressionStrings.add(currentStringPart);
				// currentStringPart = "";
				// }
				// }
				if (insideBrackets > 0 || currentStringPart.isEmpty()) {
					currentStringPart += curc;
				} else {
					subexpressionStrings.add(currentStringPart);
					currentStringPart = "";
					currentStringPart += curc;
					subexpressionStrings.add(currentStringPart);
					currentStringPart = "";
				}
				break;
			default:
				currentStringPart += curc;
				break;
			}
		}
		if (!currentStringPart.isEmpty()) {
			subexpressionStrings.add(currentStringPart);
		}

		return new ArrayList<>(subexpressionStrings);
	}
}

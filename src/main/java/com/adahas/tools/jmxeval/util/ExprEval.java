package com.adahas.tools.jmxeval.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Evaluates a complex mathematical expression. Supported operations are addition, subtraction,
 * division and multiplication. To enforce precedence of evaluation braces could be used, and
 * multiple levels of braces are supported.
 * 
 * E.g.
 * 4 + 9 will evaluate to 13
 * 5 + 3 * 2 will evaluate to 11 (as multiplication takes precedence over addition)
 * (5 + 3) * 2 will evaluate to 16 (as braces takes precedence over operations)
 * (4 + (2 * 8)) / 2 will evaluate to 10 (as most inner braces will be evaluated first)
 * 
 */
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class ExprEval {

  /**
   * Regular expression to match invalid characters in an expression
   */
  protected static final String REGEX_INVALID_CHAR = "[^\\+\\-\\*\\/.0-9]{1}";
  
  /**
   * Regular expression to match a numeric value
   */
  protected static final String REGEX_NUMERIC_VALUE = "(\\-?[0-9]+\\.[0-9]+|\\-?[0-9]+)";
  
  /**
   * Regular expression to match optional white spaces
   */
  protected static final String REGEX_WHITE_SPACE_OPTNL = "[\\s]*";
  
  /**
   * Regular expression to match mandatory white spaces
   */
  protected static final String REGEX_WHITE_SPACE_MNDTRY = "[\\s]+";
  
  /**
   * Regular expression to match most inner blocks surrounded by braces
   */
  protected static final String REGEX_INNER_BRACES = "\\([^()]*\\)";
  
  /**
   * Regular expression to match multiply operator
   */
  protected static final String REGEX_OP_MULTIPLY = "\\*";
  
  /**
   * Regular expression to match division operator
   */
  protected static final String REGEX_OP_DIVIDE = "\\/";
  
  /**
   * Regular expression to match addition operator
   */
  protected static final String REGEX_OP_ADD = "\\+";
  
  /**
   * Regular expression to match subtract operator
   */
  protected static final String REGEX_OP_SUBTRACT = "\\-";
  
  /**
   * Open brace character
   */
  protected static final String BRACE_OPEN = "(";
  
  /**
   * Close brace character
   */
  protected static final String BRACE_CLOSE = ")";
  
  /**
   * Supported operations defined in order of precedence to process
   */
  static enum Operation {
    DIVIDE,
    MULTIPLY,
    ADD,
    SUBSTRACT
  }
  
  /**
   * Expression to evaluate
   */
  protected final transient String initialExpression;
  
  /**
   * Scale
   */
  protected transient int scale = 2;
  
  /**
   * Constructs the expression for evaluation
   *  
   * @param expression Expression to evaluate
   */
  public ExprEval(final String expression) {
    this.initialExpression = expression;
  }
  
  /**
   * Evaluates the expression and return the result
   * 
   * @param expression Mathematical expression
   * @return Result value
   */
  public Object evaluate() {
    final String resultString = processComplexExpr(initialExpression);
    
    // set the final scale as requested
    BigDecimal result = new BigDecimal(resultString);
    result = result.setScale(scale, RoundingMode.HALF_EVEN);
    
    Object returnValue;
    
    if (scale == 0) {
      returnValue = result.longValue();
    } else {
      returnValue = result.doubleValue();
    }
    
    return returnValue;
  }
  
  /**
   * Processes a complex expression with braces
   * 
   * E.g.
   * 9 + (3 + (5 + 4)) + (2 + 3) will evaluate to 26
   */
  protected String processComplexExpr(final String expression) {
    String currentExpr = expression;
    String processedExpr = expression;
    
    do {
      currentExpr = processedExpr;
      processedExpr = processComplexExprPart(processedExpr);
    } while (!currentExpr.equals(processedExpr)); 
    
    currentExpr = processSimpleExpr(processedExpr);
    
    return currentExpr;
  }
  
  /**
   * Processes most inner blocks enclosed in braces (does not
   * attempt to re-process after one cycle of processing, hence the
   * second level of braces are not processed)
   * 
   * E.g.
   * 9 + (3 + (5 + 4)) + (2 + 3) will evaluate to 9 + (3 + 9) + 5
   */
  protected String processComplexExprPart(final String expression) {
    
    final Pattern pattern = Pattern.compile(REGEX_INNER_BRACES);
    final Matcher matcher = pattern.matcher(expression);
    
    final StringBuffer buffer = new StringBuffer();
    while (matcher.find()) {
      final String result = processSimpleExpr(matcher.group());
      matcher.appendReplacement(buffer, result);
    }
    matcher.appendTail(buffer);
    
    return buffer.toString();
  }

  /**
   * Processes a simple expression which does not include braces
   * 
   * E.g.
   * 4 + 9 will evaluate to 13
   * 5 + 3 * 2 will evaluate to 11 (as multiplication takes precedence over addition)
   */
  protected String processSimpleExpr(final String expression) {
    
    validateSimpleExpr(expression);
    
    String currentExpr = expression;
    String processedExpr = currentExpr;
    
    for (Operation op : Operation.values()) {
      do {
        currentExpr = processedExpr;
        processedExpr = attemptOperation(processedExpr, op);
      } while (!currentExpr.equals(processedExpr));
    } 
    
    return currentExpr;
  }
  
  /**
   * Validates a simple expression for invalid syntax
   * 
   * E.g.
   * 3 + 5 6 + 3 will evaluate as invalid syntax
   * 3 + 5 -6 + 3 will evaluate as invalid syntax
   * 3 + 5 - 6 + 3 will evaluate as valid
   * 
   * 3 % 3 will evaluate as invalid syntax
   * 3& 3 will evaluate as invalid syntax
   */
  protected void validateSimpleExpr(final String expression) {
    final Pattern pattern1 = Pattern.compile(REGEX_NUMERIC_VALUE + REGEX_WHITE_SPACE_MNDTRY + REGEX_NUMERIC_VALUE);
    final Matcher matcher1 = pattern1.matcher(expression);
    
    if (matcher1.find()) {
      throw new IllegalArgumentException("Invalid expression: [" + expression + 
          "] matched invalid block [" + matcher1.group() + "]");
    }
    
    final Pattern pattern2 = Pattern.compile(REGEX_NUMERIC_VALUE + REGEX_WHITE_SPACE_OPTNL + REGEX_INVALID_CHAR + 
        REGEX_WHITE_SPACE_OPTNL + REGEX_NUMERIC_VALUE);
    final Matcher matcher2 = pattern2.matcher(expression);
    if (matcher2.find()) {
      throw new IllegalArgumentException("Invalid expression: [" + expression + 
          "] matched invalid block [" + matcher2.group() + "]");
    }
  }
  
  /**
   * Get regular expression for the given operation
   */
  protected String getRegExForOperation(final Operation operation) {
    String opRegEx;
    
    switch (operation) {
      case ADD:
        opRegEx = REGEX_OP_ADD;
        break;
      case SUBSTRACT:
        opRegEx = REGEX_OP_SUBTRACT;
        break;
      case MULTIPLY:
        opRegEx = REGEX_OP_MULTIPLY;
        break;
      case DIVIDE:
        opRegEx = REGEX_OP_DIVIDE;
        break;
      default:
        throw new IllegalArgumentException("Unsupported operation: " + operation);  
    }
    
    return opRegEx;
  }
  
  /**
   * Attempts to process a given operation on an expression
   * 
   * E.g.
   * If the attempted operation is multiplication
   * 3 + 5 * 6 / 3 will evaluate to 3 + 30 / 3
   */
  protected String attemptOperation(final String expression, final Operation operation) {
    final String opRegEx = getRegExForOperation(operation);
    
    final Pattern pattern = Pattern.compile(REGEX_NUMERIC_VALUE + REGEX_WHITE_SPACE_OPTNL + 
        opRegEx + REGEX_WHITE_SPACE_OPTNL + REGEX_NUMERIC_VALUE);
    
    final StringBuffer buffer = new StringBuffer(); 
    
    final Matcher matcher = pattern.matcher(expression);
    if (matcher.find()) {
      final BigDecimal operand1 = new BigDecimal(matcher.group(1)); 
      final BigDecimal operand2 = new BigDecimal(matcher.group(2)); 

      Object result = null; 
      switch (operation) {
        case ADD:
          result = operand1.add(operand2);
          break;
        case SUBSTRACT:
          result = operand1.subtract(operand2);
          break;
        case MULTIPLY:
          result = operand1.multiply(operand2);
          break;
        case DIVIDE:
          // use a scale of n + 4, where n is the scale requested for the result
          result = operand1.divide(operand2, scale + 4, RoundingMode.HALF_EVEN);
          break;
        default:
          throw new IllegalArgumentException("Unsupported operation: " + operation);  
      }
      
      matcher.appendReplacement(buffer, result.toString());
    }
    matcher.appendTail(buffer);
    
    return trimAndRemoveBraces(buffer.toString());
  }
  
  /**
   * Trims and removes enclosing braces
   */
  protected String trimAndRemoveBraces(final String expression) {
    String result = expression.trim();
    if (result.startsWith(BRACE_OPEN) && result.endsWith(BRACE_CLOSE)) {
      result = result.substring(1, result.length() - 1);
    }
    return result;
  }

  /**
   * Set the required scale for the calculations
   * 
   * @param scale the scale to set
   */
  public void setScale(final int scale) {
    this.scale = scale;
  }
}

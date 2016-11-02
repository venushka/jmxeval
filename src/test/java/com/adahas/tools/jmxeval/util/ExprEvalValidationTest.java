package com.adahas.tools.jmxeval.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.adahas.tools.jmxeval.exception.JMXEvalException;

@RunWith(Parameterized.class)
public class ExprEvalValidationTest {

  private final String expression;
  private final String validationError;

  public ExprEvalValidationTest(final String expression, final String validationError) {
    this.expression = expression;
    this.validationError = validationError;
  }

  @Parameters(name = "{index}: evaluate {0} = {1} (scale: {2})")
  public static Iterable<Object[]> values() {
    return Arrays.asList(new Object[][] {
      { "3 + 5 6 + 3", "Block [5 6] in expression [3 + 5 6 + 3] invalid"},
      { "3 + 5 -6 + 3", "Block [5 -6] in expression [3 + 5 -6 + 3] invalid" },
      { "3& 3", "Block [3& 3] in expression [3& 3] invalid" },
      { "3% + 1 + (5 + 2)", "Result [3% + 8] of the expression [3% + 1 + (5 + 2)] cannot be converted to number" },
      { "(1 + 5", "Result [(6] of the expression [(1 + 5] cannot be converted to number" },
      { "1 + 5)", "Result [6)] of the expression [1 + 5)] cannot be converted to number" }
    });
  }

  @Test
  public void testEvaluate() {
    try {
      new ExprEval(expression).evaluate();
      fail("Should fail as the expression is invalid");
    } catch (JMXEvalException e) {
      assertEquals(validationError, e.getMessage());
    }
  }
}


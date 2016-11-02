package com.adahas.tools.jmxeval.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ExprEvalTest {

  private final String expression;
  private final Integer scale;
  private final Object result;

  public ExprEvalTest(final String expression, final Object result, final Integer scale) {
    this.expression = expression;
    this.result = result;
    this.scale = scale;
  }

  @Parameters(name = "{index}: evaluate {0} = {1} (scale: {2})")
  public static Iterable<Object[]> values() {
    return Arrays.asList(new Object[][] {
      { "10", "10.00", null },
      { "10 + 2", "12.00", null },
      { "12.434 - 3.4", "9.03", null },
      { "2 * 11.1", "22.20", null },
      { "2 * 11.1", "22", 0 },
      { "9 / 3", "3.00", null },
      { "10 / 3", "3.33", null },
      { "388.0 / 1000000.0", "0.00039", 5 },
      { "10 % 3", "1", 0 },
      { "10 + 2 * 4 - 1", "17.00", null },
      { "(10 + 2) * (4 - 1)", "36.00", null },
      { "(2 + 2 * (3 + (4 / 2))) * 2", "24", 0 }
    });
  }

  @Test
  public void testEvaluate() throws Exception {
    final ExprEval exprEval = new ExprEval(expression);
    if (scale != null) {
      exprEval.setScale(scale);
    }
    assertEquals(result, String.valueOf(exprEval.evaluate()));
  }
}


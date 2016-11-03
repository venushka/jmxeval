package com.adahas.tools.jmxeval.model.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.model.Element;

/**
 * Test for {@link Expr}.
 */
@RunWith(Parameterized.class)
public class ExprTest {

  @Mock private Context context;
  @Mock private Node node, attrVar, attrExpression, attrScale;
  @Mock private NamedNodeMap namedNodeMap;
  @Mock private Element child1, child2;

  private final String expression;
  private final String scale;
  private final String result;

  public ExprTest(final String expression, final String scale, final String result) {
    this.expression = expression;
    this.scale = scale;
    this.result = result;
  }

  @Parameters(name = "{index}: expression [{0}] scale [{1}] result [${2}]")
  public static Iterable<Object[]> values() {
    return Arrays.asList(new Object[][] {
      // var1 = 3
      { "10 / ${var1}", null, "3.33" },
      { "10 / ${var1}", "4", "3.3333" },
    });
  }

  @Before
  public void setUp() {
    initMocks(this);
  }

  /**
   * Test evaluating an expression.
   */
  @Test
  public void testProcess() throws Exception {
    // given
    when(node.getAttributes()).thenReturn(namedNodeMap);

    when(namedNodeMap.getNamedItem("var")).thenReturn(attrVar);
    when(attrVar.getNodeValue()).thenReturn("resultVar1");

    when(namedNodeMap.getNamedItem("expression")).thenReturn(attrExpression);
    when(attrExpression.getNodeValue()).thenReturn(expression);

    if (scale != null) {
      when(namedNodeMap.getNamedItem("scale")).thenReturn(attrScale);
      when(attrScale.getNodeValue()).thenReturn(scale);
    }

    when(context.getVar("var1")).thenReturn("3");

    final Expr expr = new Expr(context, node) {{
      addChildElement(child1);
      addChildElement(child2);
    }};

    // when
    expr.process();

    // then
    verify(child1).process();
    verify(child2).process();
    verify(context).setVar("resultVar1", new BigDecimal(result));

    // verify performance data
    assertEquals("Var", "resultVar1", expr.getVar().get());
    assertNull("Critial", expr.getCritical().get());
    assertNull("Warning", expr.getWarning().get());
  }
}


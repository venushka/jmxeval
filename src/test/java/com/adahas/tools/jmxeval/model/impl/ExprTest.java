package com.adahas.tools.jmxeval.model.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.model.Element;

/**
 * Test for {@link Expr}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExprTest {

  @Mock private Context context;
  @Mock private Node node, attrVar, attrExpression, attrScale;
  @Mock private NamedNodeMap namedNodeMap;
  @Mock private Element child1, child2;

  /**
   * Test evaluating an expression.
   */
  @Test
  public void testProcess() throws Exception {
    // given
    when(node.getAttributes()).thenReturn(namedNodeMap);

    when(namedNodeMap.getNamedItem("var")).thenReturn(attrVar);
    when(namedNodeMap.getNamedItem("expression")).thenReturn(attrExpression);
    when(namedNodeMap.getNamedItem("scale")).thenReturn(attrScale);

    when(attrVar.getNodeValue()).thenReturn("resultVar1");
    when(attrExpression.getNodeValue()).thenReturn("10 / ${var1}");
    when(attrScale.getNodeValue()).thenReturn("4");

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
    verify(context).setVar("resultVar1", new BigDecimal("3.3333"));

    // verify performance data
    assertEquals("Var", "resultVar1", expr.getVar().get());
    assertNull("Critial", expr.getCritical().get());
    assertNull("Warning", expr.getWarning().get());
  }
}


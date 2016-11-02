package com.adahas.tools.jmxeval.model.impl;

import static com.adahas.tools.jmxeval.model.Element.literal;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.model.Element;
import com.adahas.tools.jmxeval.response.EvalResult;
import com.adahas.tools.jmxeval.response.Response;

/**
 * Test for {@link Check}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CheckTest {

  @Mock private Context context;
  @Mock private Node node, attrUseVar, attrCritical, attrWarning, attrMessage, attrMode;
  @Mock private NamedNodeMap namedNodeMap;
  @Mock private Element child1, child2;
  @Mock private Eval parent;
  @Mock private Response response;

  @Captor private ArgumentCaptor<EvalResult> evalResultCaptor;

  /**
   * Test check on undefined variable.
   */
  @Test
  public void testProcessUndefinedValueCheck() throws Exception {
    // given
    when(node.getAttributes()).thenReturn(namedNodeMap);

    when(namedNodeMap.getNamedItem("useVar")).thenReturn(attrUseVar);
    when(namedNodeMap.getNamedItem("critical")).thenReturn(attrCritical);
    when(namedNodeMap.getNamedItem("warning")).thenReturn(attrWarning);
    when(namedNodeMap.getNamedItem("message")).thenReturn(attrMessage);

    when(attrUseVar.getNodeValue()).thenReturn("undefinedVar1");
    when(attrCritical.getNodeValue()).thenReturn("cr");
    when(attrWarning.getNodeValue()).thenReturn("wn");
    when(attrMessage.getNodeValue()).thenReturn("test message");

    when(context.getResponse()).thenReturn(response);
    when(parent.getName()).thenReturn(literal("Check1"));

    final Check check = new Check(context, node, parent) {{
      addChildElement(child1);
      addChildElement(child2);
    }};

    // when
    check.process();

    // then
    verify(response).addEvalResult(evalResultCaptor.capture());
    verify(child1).process();
    verify(child2).process();

    final EvalResult result = evalResultCaptor.getValue();
    assertEquals("Result", "Check1 UNKNOWN - test message", result.toString());

    // verify performance data
    assertEquals("Var", "undefinedVar1", check.getVar().get());
    assertEquals("Critial", "cr", check.getCritical().get());
    assertEquals("Warning", "wn", check.getWarning().get());
  }
}

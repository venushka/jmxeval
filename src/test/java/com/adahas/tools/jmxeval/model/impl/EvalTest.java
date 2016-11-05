package com.adahas.tools.jmxeval.model.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
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
import com.adahas.tools.jmxeval.response.Status;

/**
 * Test for {@link Eval}.
 */
@RunWith(MockitoJUnitRunner.class)
public class EvalTest {

  @Mock private Context context;
  @Mock private Node node, attrName, attrHost;
  @Mock private NamedNodeMap namedNodeMap;
  @Mock private Element child1, child2;
  @Mock private Response response;

  @Captor private ArgumentCaptor<EvalResult> evalResultCaptor;

  /**
   * Check processing child elements.
   */
  @Test
  public void testProcess() throws Exception {
    // given
    when(node.getAttributes()).thenReturn(namedNodeMap);
    when(namedNodeMap.getNamedItem("name")).thenReturn(attrName);
    when(attrName.getNodeValue()).thenReturn("MyEval");

    final Eval eval = new Eval(context, node) {{
      addChildElement(child1);
      addChildElement(child2);
    }};

    // when
    eval.process();

    // then
    verify(child1).process();
    verify(child2).process();
    assertEquals("Name", "MyEval", eval.getName().get());
    assertEquals("Host", ".*", eval.getHost().get());
  }

  /**
   * Check if evaluation failure is reported correctly.
   */
  @Test
  public void testProcessFailure() throws Exception {
    // given
    when(node.getAttributes()).thenReturn(namedNodeMap);

    when(namedNodeMap.getNamedItem("name")).thenReturn(attrName);
    when(namedNodeMap.getNamedItem("host")).thenReturn(attrHost);

    when(attrName.getNodeValue()).thenReturn("MyEval");
    when(attrHost.getNodeValue()).thenReturn("*");

    when(context.getResponse()).thenReturn(response);

    final Eval eval = new Eval(context, node) {{
      addChildElement(child1);
      addChildElement(child2);
    }};

    // when
    eval.process();

    // then
    verify(response).addEvalResult(evalResultCaptor.capture());
    verify(child1, never()).process();
    verify(child2, never()).process();

    final EvalResult result = evalResultCaptor.getValue();
    assertEquals("Name", "MyEval", result.getName());
    assertEquals("Status", Status.UNKNOWN, result.getStatus());
    assertEquals("Message", "java.util.regex.PatternSyntaxException: Dangling meta character '*' near index 0 * ^", result.getMessage());
  }

  /**
   * Check evaluation of child elements are restricted based on host names.
   */
  @Test
  public void testProcessHostBasedRestriction() throws Exception {
    // given
    when(node.getAttributes()).thenReturn(namedNodeMap);

    when(namedNodeMap.getNamedItem("name")).thenReturn(attrName);
    when(namedNodeMap.getNamedItem("host")).thenReturn(attrHost);

    when(attrName.getNodeValue()).thenReturn("MyEval");
    when(attrHost.getNodeValue()).thenReturn("nonexistenthostname");

    final Eval eval = new Eval(context, node) {{
      addChildElement(child1);
      addChildElement(child2);
    }};

    // when
    eval.process();

    // then
    verify(child1, never()).process();
    verify(child2, never()).process();
    assertEquals("Name", "MyEval", eval.getName().get());
    assertEquals("Host", "nonexistenthostname", eval.getHost().get());
  }
}


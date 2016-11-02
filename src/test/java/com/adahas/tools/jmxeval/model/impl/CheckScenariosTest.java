package com.adahas.tools.jmxeval.model.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.model.Element;
import com.adahas.tools.jmxeval.response.EvalResult;
import com.adahas.tools.jmxeval.response.Response;

/**
 * Test for {@link Check} ranges.
 */
@RunWith(Parameterized.class)
public class CheckScenariosTest {

  @Mock private Context context;
  @Mock private Node node, attrUseVar, attrCritical, attrWarning, attrMessage, attrMode;
  @Mock private NamedNodeMap namedNodeMap;
  @Mock private Element child1, child2;
  @Mock private Eval parent;
  @Mock private Response response;

  @Captor private ArgumentCaptor<EvalResult> evalResultCaptor;

  private final Object value;
  private final String critical;
  private final String warn;
  private final String mode;
  private final String expectedResult;

  public CheckScenariosTest(final Object value, final String critical, final String warn,  final String mode, final String expectedResult) {
    this.value = value;
    this.critical = critical;
    this.warn = warn;
    this.mode = mode;
    this.expectedResult = expectedResult;
  }

  @Parameters(name = "{index}: value [{0}] response [{1}]")
  public static Iterable<Object[]> values() {
    return Arrays.asList(new Object[][] {
      // numeric, should perform range matches
      { 50, "80", "60", "default", "Check1 OK - test message" },
      { 70, "80", "60", "default", "Check1 WARNING - test message" },
      { 90, "80", "60", "default", "Check1 CRITICAL - test message" },

      // non-numeric, should perform exact matches
      { "green", "red", "amber", "default", "Check1 OK - test message" },
      { "amber", "red", "amber", "default", "Check1 WARNING - test message" },
      { "red", "red", "amber", "default", "Check1 CRITICAL - test message" },

      // non-numeric, without some thresholds set
      { "green", "red", null, "default", "Check1 OK - test message" },
      { "amber", null, "amber", "default", "Check1 WARNING - test message" },
      { "red", "red", null, "default", "Check1 CRITICAL - test message" },

      // regular expression based checks
      { "something went wrong", ".*severe.*", ".*error.*", "regex", "Check1 OK - test message" },
      { "an error occurred", ".*severe.*", ".*error.*", "regex", "Check1 WARNING - test message" },
      { "a severe error occurred", ".*severe.*", ".*error.*", "regex", "Check1 CRITICAL - test message" },

      // regular expressions based checks, without some thresholds set
      { "something went wrong", ".*severe.*", null, "regex", "Check1 OK - test message" },
      { "an error occurred", null, ".*error.*", "regex", "Check1 WARNING - test message" },
      { "a severe error occurred", ".*severe.*", null, "regex", "Check1 CRITICAL - test message" },
    });
  }


  @Before
  public void setUp() {
    initMocks(this);
  }

  /**
   * Test warn range check.
   */
  @Test
  public void testProcessRangeWarnCheck() throws Exception {
    // given
    when(node.getAttributes()).thenReturn(namedNodeMap);

    when(namedNodeMap.getNamedItem("useVar")).thenReturn(attrUseVar);
    when(namedNodeMap.getNamedItem("critical")).thenReturn(attrCritical);
    when(namedNodeMap.getNamedItem("warning")).thenReturn(attrWarning);
    when(namedNodeMap.getNamedItem("message")).thenReturn(attrMessage);
    when(namedNodeMap.getNamedItem("mode")).thenReturn(attrMode);

    when(attrUseVar.getNodeValue()).thenReturn("useVar1");
    when(attrCritical.getNodeValue()).thenReturn(critical);
    when(attrWarning.getNodeValue()).thenReturn(warn);
    when(attrMessage.getNodeValue()).thenReturn("test message");
    when(attrMode.getNodeValue()).thenReturn(mode);

    when(context.getResponse()).thenReturn(response);
    when(context.getVar("useVar1")).thenReturn(value);
    when(parent.getName()).thenReturn(Element.literal("Check1"));

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
    assertEquals("Result", expectedResult, result.toString());
  }
}

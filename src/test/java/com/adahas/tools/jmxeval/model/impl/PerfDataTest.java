package com.adahas.tools.jmxeval.model.impl;

import static com.adahas.tools.jmxeval.model.Element.literal;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

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
import com.adahas.tools.jmxeval.model.PerfDataSupport;
import com.adahas.tools.jmxeval.response.PerfDataResult;
import com.adahas.tools.jmxeval.response.Response;

/**
 * Test for {@link PerfData}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PerfDataTest {

  @Mock private Context context;
  @Mock private Node node, attrLabel, attrCritical, attrWarning, attrMin, attrMax, attrUnit;
  @Mock private NamedNodeMap namedNodeMap;
  @Mock private Element child1, child2;
  @Mock private Response response;

  @Captor private ArgumentCaptor<PerfDataResult> perfResultCaptor;

  /**
   * Test extracting performance data.
   */
  @Test
  public void testProcess() throws Exception {
    // given
    final Element parent = mock(Element.class, withSettings().extraInterfaces(PerfDataSupport.class));
    when(((PerfDataSupport) parent).getVar()).thenReturn(literal("parentVar"));

    when(node.getAttributes()).thenReturn(namedNodeMap);

    when(namedNodeMap.getNamedItem("label")).thenReturn(attrLabel);
    when(namedNodeMap.getNamedItem("critical")).thenReturn(attrCritical);
    when(namedNodeMap.getNamedItem("warning")).thenReturn(attrWarning);
    when(namedNodeMap.getNamedItem("min")).thenReturn(attrMin);
    when(namedNodeMap.getNamedItem("max")).thenReturn(attrMax);
    when(namedNodeMap.getNamedItem("unit")).thenReturn(attrUnit);

    when(attrLabel.getNodeValue()).thenReturn("lb");
    when(attrCritical.getNodeValue()).thenReturn("cr");
    when(attrWarning.getNodeValue()).thenReturn("wn");
    when(attrMin.getNodeValue()).thenReturn("mn");
    when(attrMax.getNodeValue()).thenReturn("mx");
    when(attrUnit.getNodeValue()).thenReturn("UT");

    when(context.getResponse()).thenReturn(response);
    when(context.getVar("parentVar")).thenReturn("parentResult");

    final PerfData perfData = new PerfData(context, node, parent);

    // when
    perfData.process();

    // then
    verify(response).addPerfData(perfResultCaptor.capture());
    verify(child1, never()).process();
    verify(child2, never()).process();

    final PerfDataResult result = perfResultCaptor.getValue();
    assertEquals("Performance data", "lb=parentResultUT;wn;cr;mn;mx", result.toString());
  }

  /**
   * Test extracting performance data.
   */
  @Test
  public void testProcessWithAttrInheritedFromParent() throws Exception {
    // given
    final Element parent = mock(Element.class, withSettings().extraInterfaces(PerfDataSupport.class));
    when(((PerfDataSupport) parent).getVar()).thenReturn(literal("parentVar"));
    when(((PerfDataSupport) parent).getCritical()).thenReturn(literal("parentCr"));
    when(((PerfDataSupport) parent).getWarning()).thenReturn(literal("parentWn"));

    when(node.getAttributes()).thenReturn(namedNodeMap);

    when(namedNodeMap.getNamedItem("min")).thenReturn(attrMin);
    when(namedNodeMap.getNamedItem("max")).thenReturn(attrMax);
    when(namedNodeMap.getNamedItem("unit")).thenReturn(attrUnit);

    when(attrMin.getNodeValue()).thenReturn("mn");
    when(attrMax.getNodeValue()).thenReturn("mx");
    when(attrUnit.getNodeValue()).thenReturn("UT");

    when(context.getResponse()).thenReturn(response);
    when(context.getVar("parentVar")).thenReturn("parentResult");

    final PerfData perfData = new PerfData(context, node, parent);

    // when
    perfData.process();

    // then
    verify(response).addPerfData(perfResultCaptor.capture());
    verify(child1, never()).process();
    verify(child2, never()).process();

    final PerfDataResult result = perfResultCaptor.getValue();
    assertEquals("Performance data", "parentVar=parentResultUT;parentWn;parentCr;mn;mx", result.toString());
  }
}


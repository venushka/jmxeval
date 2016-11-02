package com.adahas.tools.jmxeval.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;

/**
 * Test for {@link Element}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ElementTest {

  @Mock private Element childOne, childTwo;
  @Mock private Context context;
  @Mock private Node elementNode, attributeKey1, attributeKey2, attributeKey4, attributeKey5, attributeKey6, attributeKey7;
  @Mock private NamedNodeMap namedNodeMap;

  /**
   * Test process call delegation.
   */
  @Test
  public void testProcess() throws Exception {
    final Element element = new Element(context);

    // given
    element.addChildElement(childOne);
    element.addChildElement(childTwo);

    // when
    element.process();

    // then
    verify(childOne).process();
    verify(childTwo).process();
  }

  /**
   * Test getting node attribute with a given default value.
   */
  @Test
  public void testGetNodeAttrWithDefaultValue() throws Exception {
    final Element element = new Element(context);

    // given
    when(elementNode.getAttributes()).thenReturn(namedNodeMap);

    when(namedNodeMap.getNamedItem("key1")).thenReturn(attributeKey1);
    when(namedNodeMap.getNamedItem("key2")).thenReturn(attributeKey2);
    when(namedNodeMap.getNamedItem("key4")).thenReturn(attributeKey4);
    when(namedNodeMap.getNamedItem("key5")).thenReturn(attributeKey5);
    when(namedNodeMap.getNamedItem("key6")).thenReturn(attributeKey6);
    when(namedNodeMap.getNamedItem("key7")).thenReturn(attributeKey7);

    when(attributeKey1.getNodeValue()).thenReturn("val1");
    when(attributeKey4.getNodeValue()).thenReturn("Test ${var1} / ${var2}");
    when(attributeKey5.getNodeValue()).thenReturn("Test ${${var3}} / ${var2}");
    when(attributeKey6.getNodeValue()).thenReturn("Test {${var1} / ${var2}}");
    when(attributeKey7.getNodeValue()).thenReturn("Test ${var1} / ${${var2}}");

    when(context.getVar("var1")).thenReturn("111");
    when(context.getVar("var2")).thenReturn("222");
    when(context.getVar("var3")).thenReturn("var1");

    // when, then
    assertEquals("val1", element.getNodeAttr(elementNode, "key1", "def1").get());
    assertEquals("def2", element.getNodeAttr(elementNode, "key2", "def2").get());
    assertEquals("def3", element.getNodeAttr(elementNode, "key3", "def3").get());
    assertEquals("Test 111 / 222", element.getNodeAttr(elementNode, "key4", "def4").get());
    assertEquals("Test 111 / 222", element.getNodeAttr(elementNode, "key5", "def5").get());
    assertEquals("Test {111 / 222}", element.getNodeAttr(elementNode, "key6", "def6").get());
    assertEquals("Test 111 / ${${var2}}", element.getNodeAttr(elementNode, "key7", "def7").get());
  }

  /**
   * Test getting node attribute without default values.
   */
  @Test
  public void testGetNodeAttr() throws Exception {
    final Element element = new Element(context);

    // given
    when(elementNode.getAttributes()).thenReturn(namedNodeMap);

    when(namedNodeMap.getNamedItem("key1")).thenReturn(attributeKey1);
    when(namedNodeMap.getNamedItem("key2")).thenReturn(attributeKey2);
    when(namedNodeMap.getNamedItem("key4")).thenReturn(attributeKey4);
    when(namedNodeMap.getNamedItem("key5")).thenReturn(attributeKey5);
    when(namedNodeMap.getNamedItem("key6")).thenReturn(attributeKey6);
    when(namedNodeMap.getNamedItem("key7")).thenReturn(attributeKey7);

    when(attributeKey1.getNodeValue()).thenReturn("val1");
    when(attributeKey4.getNodeValue()).thenReturn("Test ${var1} / ${var2}");
    when(attributeKey5.getNodeValue()).thenReturn("Test ${${var3}} / ${var2}");
    when(attributeKey6.getNodeValue()).thenReturn("Test {${var1} / ${var2}}");
    when(attributeKey7.getNodeValue()).thenReturn("Test ${var1} / ${${var2}}");

    when(context.getVar("var1")).thenReturn("111");
    when(context.getVar("var2")).thenReturn("222");
    when(context.getVar("var3")).thenReturn("var1");

    // when, then
    assertEquals("val1", element.getNodeAttr(elementNode, "key1").get());
    assertNull(element.getNodeAttr(elementNode, "key2").get());
    assertNull(element.getNodeAttr(elementNode, "key3").get());
    assertEquals("Test 111 / 222", element.getNodeAttr(elementNode, "key4").get());
    assertEquals("Test 111 / 222", element.getNodeAttr(elementNode, "key5").get());
    assertEquals("Test {111 / 222}", element.getNodeAttr(elementNode, "key6").get());
    assertEquals("Test 111 / ${${var2}}", element.getNodeAttr(elementNode, "key7").get());
  }
}

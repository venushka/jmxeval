package com.adahas.tools.jmxeval.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.net.URI;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.exception.JMXEvalException;

/**
 * Test for {@link ElementBuilder}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ElementBuilderTest {

  @Mock private Context context;
  @Mock private Node node;
  @Mock private Element element;

  /**
   * Test building an {@link Element} with a valid configuration file.
   */
  @Test
  public void testBuildWithValidConfig() throws Exception {
    final URI uri = ElementBuilder.class.getResource(this.getClass().getSimpleName() + "-schema-2_0-valid-heap-usage.xml").toURI();
    final String absolutePath = new File(uri).getAbsolutePath();

    doReturn(absolutePath).when(context).getFilename();
    doReturn(true).when(context).isValidate();

    final ElementBuilder elementBuilder = new ElementBuilder();
    final Element rootElement = elementBuilder.build(context);

    assertNotNull(rootElement);
  }


  /**
   * Test building an {@link Element} with a invalid configuration file.
   */
  @Test
  public void testBuildWithInvalidConfig() throws Exception {
    final URI uri = ElementBuilder.class.getResource(this.getClass().getSimpleName() + "-schema-2_0-invalid-no-connection.xml").toURI();
    final String absolutePath = new File(uri).getAbsolutePath();

    doReturn(absolutePath).when(context).getFilename();
    doReturn(true).when(context).isValidate();

    final ElementBuilder elementBuilder = new ElementBuilder();

    try {
      elementBuilder.build(context);
      fail("Should fails as a connection is not defined");
    } catch (JMXEvalException e) {
      assertEquals("Invalid message", "Error reading configuration file: cvc-complex-type.2.4.b: The content of element 'jmxeval:jmxeval' is not complete. One of '{connection, local}' is expected.", e.getMessage());
    }
  }


  /**
   * Test building an {@link Element} with a invalid configuration file.
   */
  @Test
  public void testBuildWithNonExistingConfig() throws Exception {
    final String absolutePath = new File("/tmp/non-exisring-file-path").getAbsolutePath();

    final Context context = mock(Context.class);
    doReturn(absolutePath).when(context).getFilename();

    final ElementBuilder elementBuilder = new ElementBuilder();

    try {
      elementBuilder.build(context);
      fail("Should fails as a connection is not defined");
    } catch (JMXEvalException e) {
      assertEquals("Invalid message", "Can not read configuration file: " + absolutePath, e.getMessage());
    }
  }


  /**
   * Check {@link Element} type construction.
   */
  @Test
  public void testCreateElementInstance() throws Exception {
    final ElementBuilder elementBuilder = new ElementBuilder();

    assertTrue(elementBuilder.createElementInstance(ElementWithNoArgConstructor.class.getName(), context, node, element) instanceof ElementWithNoArgConstructor);
    assertTrue(elementBuilder.createElementInstance(ElementWithConstructorArgElement.class.getName(), context, node, element) instanceof ElementWithConstructorArgElement);
    assertTrue(elementBuilder.createElementInstance(ElementWithConstructorArgNodeAndElement.class.getName(), context, node, element) instanceof ElementWithConstructorArgNodeAndElement);

    try {
      elementBuilder.createElementInstance("com.adahas.tools.jmxeval.model.ElementWithMultipleConstructors", context, node, element);
      fail("Should fail to instanciate a class with multiple constructors");
    } catch (JMXEvalException e) {
      assertEquals("com.adahas.tools.jmxeval.model.ElementWithMultipleConstructors has more than one constructor, it should only "
          + "have one constructor which can optionally accept the following types: com.adahas.tools.jmxeval.Context (execution context), "
          + "org.w3c.dom.Node (XML Node for the element), com.adahas.tools.jmxeval.model.Element (parent Element)", e.getMessage());
    }
  }
}

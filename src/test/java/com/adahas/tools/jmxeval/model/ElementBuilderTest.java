package com.adahas.tools.jmxeval.model;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.net.URI;

import org.junit.Test;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.exception.ConfigurationException;

public class ElementBuilderTest {

  @Test
  public void correctConfigurationsInVersion12AreAccepted() throws Exception {
    final URI uri = ElementBuilder.class.getResource("/schema/1.2/valid/jvm-heap-usage.xml").toURI();
    final String absolutePath = new File(uri).getAbsolutePath();

    final Context context = mock(Context.class);
    doReturn(absolutePath).when(context).getConfigValue(Context.CONFIG_FILENAME);
    doReturn("true").when(context).getConfigValue(Context.CONFIG_VALIDATE);
    doReturn("1.2").when(context).getConfigValue(Context.CONFIG_SCHEMA);

    final ElementBuilder elementBuilder = new ElementBuilder();
    final Element rootElement = elementBuilder.build(context);

    assertNotNull(rootElement);
  }


  @Test(expected = ConfigurationException.class)
  public void incorrectConfigurationsInVersion12AreRejected() throws Exception {
    final URI uri = ElementBuilder.class.getResource("/schema/1.2/invalid/no-connection.xml").toURI();
    final String absolutePath = new File(uri).getAbsolutePath();

    final Context context = mock(Context.class);
    doReturn(absolutePath).when(context).getConfigValue(Context.CONFIG_FILENAME);
    doReturn("true").when(context).getConfigValue(Context.CONFIG_VALIDATE);
    doReturn("1.2").when(context).getConfigValue(Context.CONFIG_SCHEMA);

    final ElementBuilder elementBuilder = new ElementBuilder();

    elementBuilder.build(context);
  }
}

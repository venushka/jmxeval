package com.adahas.tools.jmxeval.model.test;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.exception.ConfigurationException;
import com.adahas.tools.jmxeval.model.Element;
import com.adahas.tools.jmxeval.model.ElementBuilder;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URI;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertNotNull;

public class ElementBuilderTest
{
    @Test(dataProvider = "correctConfigurationsVersion1.2")
    public void correctConfigurationsInVersion12AreAccepted(String configFile) throws Exception {
        URI uri = ElementBuilder.class.getResource(configFile).toURI();
        String absolutePath = new File(uri).getAbsolutePath();

        Context context = mock(Context.class);
        doReturn(absolutePath).when(context).getConfigValue(Context.CONFIG_FILENAME);
        doReturn("true").when(context).getConfigValue(Context.CONFIG_VALIDATE);
        doReturn("1.2").when(context).getConfigValue(Context.CONFIG_SCHEMA);

        ElementBuilder elementBuilder = new ElementBuilder();

        Element rootElement = elementBuilder.build(context);

        assertNotNull(rootElement);
    }

    @Test(dataProvider = "invalidConfigurationsVersion1.2",
          expectedExceptions = ConfigurationException.class)
    public void incorrectConfigurationsInVersion12AreRejected(String configFile) throws Exception {
        URI uri = ElementBuilder.class.getResource(configFile).toURI();
        String absolutePath = new File(uri).getAbsolutePath();

        Context context = mock(Context.class);
        doReturn(absolutePath).when(context).getConfigValue(Context.CONFIG_FILENAME);
        doReturn("true").when(context).getConfigValue(Context.CONFIG_VALIDATE);
        doReturn("1.2").when(context).getConfigValue(Context.CONFIG_SCHEMA);

        ElementBuilder elementBuilder = new ElementBuilder();

        elementBuilder.build(context);
    }


    @DataProvider(name = "correctConfigurationsVersion1.2")
    public static Object[][] correctConfigurationsVersion12() {
        return new Object[][] {
            {"/schema/1.2/valid/jvm-heap-usage.xml"},
        };
    }

    @DataProvider(name = "invalidConfigurationsVersion1.2")
    public static Object[][] invalidConfigurationsVersion12() {
        return new Object[][] {
            {"/schema/1.2/invalid/no-connection.xml"},
        };
    }

}

package com.adahas.tools.jmxeval;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.PrintStream;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.adahas.tools.jmxeval.exception.JMXEvalException;
import com.adahas.tools.jmxeval.model.ElementBuilder;
import com.adahas.tools.jmxeval.model.impl.JMXEval;
import com.adahas.tools.jmxeval.response.EvalResult;
import com.adahas.tools.jmxeval.response.Status;

@RunWith(MockitoJUnitRunner.class)
public class AppTest {

  @Spy private App app;

  @Mock private ElementBuilder elementBuilder;
  @Mock private JMXEval jmxEval;
  @Mock private PrintStream outputWriter, errorWriter;

  @Captor private ArgumentCaptor<Context> contextCaptor;
  @Captor private ArgumentCaptor<String> outputCaptor;
  @Captor private ArgumentCaptor<String> errorCaptor;

  /**
   * Test successful execution.
   */
  @Test
  public void testExecute() throws Exception {
    // given
    doReturn(elementBuilder).when(app).getElementBuilder();
    doReturn(jmxEval).when(elementBuilder).build(any(Context.class));

    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(final InvocationOnMock invocation) throws Throwable {
        verify(elementBuilder).build(contextCaptor.capture());

        final Context context = contextCaptor.getValue();
        context.getResponse().addEvalResult(new EvalResult("Memory", Status.OK, "Memory usage 25%"));
        return null;
      }
    }).when(jmxEval).process();

    // when
    final int returnValue = app.execute(new String[] { "testfile.xml" }, outputWriter, errorWriter);

    // then
    verify(jmxEval).process();
    verify(outputWriter).println(outputCaptor.capture());
    verifyZeroInteractions(errorWriter);

    final String output = outputCaptor.getValue();

    assertTrue("Output", output.trim().matches("JMXEval Memory OK - Memory usage 25% \\| time=[0-9\\.]+s"));
    assertEquals("Result", Status.OK.getValue(), returnValue);
  }

  /**
   * Test run with invalid syntax.
   */
  @Test
  public void testExecuteWithInvalidSyntax() throws Exception {
    // when
    final int returnValue = app.execute(new String[] { "testfile.xml", "--non-existant-arg=foo" }, outputWriter, errorWriter);

    // then
    verify(errorWriter).println(errorCaptor.capture());
    verifyZeroInteractions(outputWriter);

    final List<String> output = errorCaptor.getAllValues();

    assertEquals("Output", "Error: \"--non-existant-arg=foo\" is not a valid option", output.get(0));
    assertEquals("Result", Status.UNKNOWN.getValue(), returnValue);
  }

  /**
   * Test run with invalid syntax.
   */
  @Test
  public void testExecuteExceptionWhileEvaluating() throws Exception {
    // given
    doReturn(elementBuilder).when(app).getElementBuilder();
    doReturn(jmxEval).when(elementBuilder).build(any(Context.class));

    doThrow(new JMXEvalException("Something went wrong")).when(jmxEval).process();

    // when
    final int returnValue = app.execute(new String[] { "testfile.xml" }, outputWriter, errorWriter);

    // then
    verify(elementBuilder).build(contextCaptor.capture());
    verify(errorWriter).println(errorCaptor.capture());
    verifyZeroInteractions(outputWriter);

    final String output = errorCaptor.getValue();

    assertEquals("Output", "Error: Something went wrong (Run with --verbose for option debug information)", output);
    assertEquals("Result", Status.UNKNOWN.getValue(), returnValue);
  }

  /**
   * Test command line argument parsing.
   */
  @Test
  public void testExecuteCommandLineArgParsing() throws Exception {
    // given
    doReturn(elementBuilder).when(app).getElementBuilder();
    doReturn(jmxEval).when(elementBuilder).build(any(Context.class));

    // when
    app.execute(new String[] {
      "testfile.xml",
      "--verbose",
      "--validate",
      "--schema=1.2"
    }, outputWriter, errorWriter);

    // then
    verify(elementBuilder).build(contextCaptor.capture());

    final Context context = contextCaptor.getValue();

    assertEquals("Filename", "testfile.xml", context.getFilename());
    assertEquals("Schema version", "1.2", context.getSchemaVersion());
    assertTrue("Verbose", context.isVerbose());
    assertTrue("Validate", context.isValidate());
  }

  /**
   * Check if the get element builder method can produce a valid {@link ElementBuilder}.
   */
  @Test
  public void testGetElementBuilder() throws Exception {
    final ElementBuilder elementBuilder = app.getElementBuilder();
    assertNotNull(elementBuilder);
  }
}


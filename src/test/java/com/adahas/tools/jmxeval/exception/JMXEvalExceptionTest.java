package com.adahas.tools.jmxeval.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class JMXEvalExceptionTest {

  /**
   * Check if the message with multiple lines of text get converted to a single line.
   */
  @Test
  public void testJMXEvalExceptionMuliLineMessage() {
    final JMXEvalException exception = new JMXEvalException("first\r\nline\tindendeted second line and some white   spaces\n");
    assertEquals("Exception message", "first line indendeted second line and some white spaces", exception.getMessage());
  }

  /**
   * Check that it prevents creating exceptions without a message.
   */
  @Test
  public void testJMXEvalExceptionWithNull() {
    try {
      new JMXEvalException(null);
      fail("Should not allow creating an exception without a message");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }
}


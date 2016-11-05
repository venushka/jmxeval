package com.adahas.tools.jmxeval;

import java.io.PrintStream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.adahas.tools.jmxeval.exception.JMXEvalException;
import com.adahas.tools.jmxeval.model.ElementBuilder;
import com.adahas.tools.jmxeval.model.impl.JMXEval;
import com.adahas.tools.jmxeval.response.PerfDataResult;
import com.adahas.tools.jmxeval.response.Status;

/**
 * Main application class that reads the jmxeval configuration file and process it to
 * check multiple JMX based service checks and report status based on set thresholds. For computing
 * the values for comparisons with the thresholds set, basic mathematical computations can be used.
 */
public class App {

  private static final Logger log = LogManager.getLogger(App.class);

  /**
   * Process executor method as the actual process will need to perform
   * System.exit() which will prevent using test cases to test logic
   * as it exists the current VM
   *
   * @param args Command line args
   * @param outputWriter Writer to direct the output to
   * @param errorWriter Writer to print errors to
   * @return process return value
   * @throws CmdLineException with bad command line options
   */
  protected int execute(final String[] args, final PrintStream outputWriter, final PrintStream errorWriter) {
    // set logging level to OFF to ensure nothing is logged to console unless --verbose option is set
    setLogLevel(Level.OFF);

    final Context context = new Context();
    final CmdLineParser parser = new CmdLineParser(context);

    try {
      // parse command line arguments to populate values in the context
      parser.parseArgument(args);

      // enable verbose logging if --verbose option is set
      if (context.isVerbose()) {
        setLogLevel(Level.DEBUG);
      }

      // capture start time
      final long startTime = System.currentTimeMillis();

      // build the configuration element
      final ElementBuilder elementBuilder = getElementBuilder();
      final JMXEval jmxEval = (JMXEval) elementBuilder.build(context);

      // process the evals
      jmxEval.process();

      // set elapsed time in seconds
      final double elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
      context.getResponse().addPerfData(new PerfDataResult("time", String.valueOf(elapsedTime), "s", null, null, null, null));

      // print response
      outputWriter.println(context.getResponse().toString());

      // return status value to indicate execution status
      return context.getResponse().getStatus().getValue();

    } catch (JMXEvalException | RuntimeException e) {
      log.error("Error while evaluating checks", e);

      // print error
      errorWriter.println("Error: " + e.getMessage() + " (Run with --verbose for option debug information)");

    } catch (CmdLineException e) {
      log.error("Error parsing command line arguments", e);

      // print error and usage information
      errorWriter.println("Error: " + e.getMessage());
      errorWriter.print("Syntax: check_jmxeval");
      parser.printSingleLineUsage(errorWriter);
      errorWriter.println();
      parser.printUsage(errorWriter);
    }

    return Status.UNKNOWN.getValue();
  }

  /**
   * Change the logging level.
   *
   * @param logLevel Log levele
   */
  private void setLogLevel(final Level logLevel) {
    final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    final LoggerConfig loggerConfig = ctx.getConfiguration().getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
    loggerConfig.setLevel(logLevel);
    ctx.updateLoggers();
  }

  /**
   * Main.
   *
   * @param args Argument list, when empty, syntax will be displayed
   */
  public static void main(final String...args)  {
    System.exit(new App().execute(args, System.out, System.err)); // NOSONAR - As this is a console application, System.out/err must be used.
  }

  /**
   * Get an {@link ElementBuilder} instance. Using a getter method to allow testing, due to lack of a DI framework.
   *
   * @return an {@link ElementBuilder} instance
   * @throws JMXEvalException if building the {@link ElementBuilder} fails
   */
  ElementBuilder getElementBuilder() throws JMXEvalException {
    return new ElementBuilder();
  }
}

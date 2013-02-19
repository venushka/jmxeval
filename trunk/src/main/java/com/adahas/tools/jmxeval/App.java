package com.adahas.tools.jmxeval;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.adahas.tools.jmxeval.exception.ConfigurationException;
import com.adahas.tools.jmxeval.exception.EvalException;
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
  
  /**
   * Process executor method as the actual process will need to perform
   * System.exit() which will prevent using test cases to test logic
   * as it exists the current VM
   * 
   * @param args Command line args
   * @param outputWriter Writer to direct the output to
   * @return process return value
   * @throws CmdLineException with bad command line options
   */
  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  protected int execute(final String[] args, final PrintWriter outputWriter) {
    int returnValue;
    
    final Context context = getContextInstance(new HashMap<String,String>());
    CmdLineParser parser = new CmdLineParser(context);
    
    // execute the app
    try {
      parser.parseArgument(args);
      
      // capture start time
      final long startTime = System.currentTimeMillis();
      
      // build the config element
      final ElementBuilder elementBuilder = getElementBuilderInstance();
      final JMXEval jmxEval = (JMXEval) elementBuilder.build(context);
    
      // process the evals
      jmxEval.process(context);

      // set elapsed time in seconds
      final double elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
      context.getResponse().addPerfData(new PerfDataResult(
          "time", String.valueOf(elapsedTime), "s", null, null, null, null));
      
      // print response
      outputWriter.println(context.getResponse());
      
      // return status value to indicate execution status
      returnValue = context.getResponse().getStatus().getValue();
      
    } catch (ConfigurationException e) {
      
      // print exception
      outputWriter.println(e.getMessage());
      if (e.getCause() != null) {
        outputWriter.println("Error details:");
        e.printStackTrace(outputWriter);
      }
      
      // indicate error by returning a non-zero value
      returnValue = Status.UNKNOWN.getValue();
      
    } catch (EvalException e) {
      
      // print exception
      outputWriter.println(e.getMessage());
      if (e.getCause() != null) {
        outputWriter.println("Error details:");
        e.printStackTrace(outputWriter);
      }
      
      // indicate error by returning a non-zero value
      returnValue = e.getStatus().getValue();
    } catch (CmdLineException e) {
      e.printStackTrace();
      // print usage information
      System.err.println(e.getMessage());
      System.err.print("java -jar jmxeval.jar ");
      parser.printSingleLineUsage(System.err);
      System.err.println();
      parser.printUsage(System.err);
      returnValue = Status.UNKNOWN.getValue();
    }
    
    return returnValue;
  }
  
  /**
   * Main
   * 
   * @param args Argument list, when empty, syntax will be displayed
   */
  @SuppressWarnings("PMD.DoNotCallSystemExit")
  public static void main(final String...args) {
    System.exit(
        new App().execute(
            args, new PrintWriter(System.out, true)
        )
    );
  }
  
  /**
   * Get the an element builder
   * 
   * @return Element builder
   * @throws ConfigurationException When a configuration error is encountered
   */
  protected ElementBuilder getElementBuilderInstance() throws ConfigurationException {
    return new ElementBuilder();
  }
  
  /**
   * Get a context instance
   * 
   * @return Context
   */
   protected Context getContextInstance(final Map<String, String> config) {
     return new Context(config);
   }
}

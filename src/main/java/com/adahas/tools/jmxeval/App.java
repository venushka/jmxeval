package com.adahas.tools.jmxeval;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
   * Command line argument prefix
   */
  private static final String ARG_SWITCH_PREFIX = "--";
  
  /**
   * Command line argument key value separator
   */
  private static final String ARG_VALUE_SEPARATOR = "=";
  
  /**
   * Process executor method as the actual process will need to perform
   * System.exit() which will prevent using test cases to test logic
   * as it exists the current VM
   * 
   * @param args Command line args
   * @param outputWriter Writer to direct the output to
   * @return process return value
   */
  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  protected int execute(final String[] args, final PrintWriter outputWriter) {
    int returnValue;
    
    // execute the app
    try {
      final Map<String, String> argsMap = parseArgs(args);
      final Context context = getContextInstance(argsMap);
    
      // capture start time
      final long startTime = System.currentTimeMillis();
      
      // build the config element
      final ElementBuilder elementBuilder = getElementBuilderInstance();
      final JMXEval jmxEval = (JMXEval) elementBuilder.build(context);
    
      // process the evals
      jmxEval.process(context);

      // set elapsed time in seconds
      final double elapsedTime = System.currentTimeMillis() - startTime;
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
   * Parses command line args to a map
   * 
   * @param args Argument list
   * @return Parsed arguments map
   */
  @SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.ConfusingTernary"})
  protected Map<String, String> parseArgs(final String[] args) throws ConfigurationException {
    
    // throw exception if no argument supplied (show syntax)
    if (args.length == 0) {
      throw new ConfigurationException("Syntax: check_jmxeval <filename> [--validate=true [--schema=x.x]]");
    }
  
    final Map<String, String> argsMap = new HashMap<String, String>();
    
    // supported switches
    final List<String> switches = new ArrayList<String>();
    switches.add(Context.CONFIG_VALIDATE);
    switches.add(Context.CONFIG_SCHEMA);
    
    for (String arg : args) {

      // process arguments with -- prefix
      if (arg.startsWith(ARG_SWITCH_PREFIX) && arg.contains(ARG_VALUE_SEPARATOR)) {
        final String argName = arg.substring(ARG_SWITCH_PREFIX.length(), arg.indexOf(ARG_VALUE_SEPARATOR));
        
        if (switches.contains(argName)) {
          if (argsMap.containsKey(argName)) {
            throw new ConfigurationException("Error: Repeated arguments found. [" + ARG_SWITCH_PREFIX + argName + "]");
          } else {
            argsMap.put(argName, arg.substring(arg.indexOf(ARG_VALUE_SEPARATOR) + 1));
          }
        } else {
          throw new ConfigurationException("Error: Invalid argument specified. [" + arg + "]");
        }
        
      } else if (!arg.startsWith(ARG_SWITCH_PREFIX)) {
        // process argument with no -- prefix
        if (argsMap.containsKey(Context.CONFIG_FILENAME)) {
          throw new ConfigurationException("Error: Multiple filenames not supported. [" + arg + "]");
        } else {
          argsMap.put(Context.CONFIG_FILENAME, arg);
        }
      } else {
        throw new ConfigurationException("Error: Invalid argument specified. [" + arg + "]");
      }
    }
    
    return argsMap;
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

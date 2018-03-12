/* -----------------------------------------------------------------------------
 * Parser.java
 * -----------------------------------------------------------------------------
 *
 * Producer : com.parse2.aparse.Parser 2.5
 * Produced : Fri Feb 09 17:16:10 CET 2018
 *
 * -----------------------------------------------------------------------------
 */

package uniresolver.did.parser;

import java.util.Stack;
import java.util.Properties;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;

public class Parser
{
  private Parser() {}

  static public void main(String[] args)
  {
    Properties arguments = new Properties();
    String error = "";
    boolean ok = args.length > 0;

    if (ok)
    {
      arguments.setProperty("Trace", "Off");
      arguments.setProperty("Rule", "did-reference");

      for (int i = 0; i < args.length; i++)
      {
        if (args[i].equals("-trace"))
          arguments.setProperty("Trace", "On");
        else if (args[i].equals("-visitor"))
          arguments.setProperty("Visitor", args[++i]);
        else if (args[i].equals("-file"))
          arguments.setProperty("File", args[++i]);
        else if (args[i].equals("-string"))
          arguments.setProperty("String", args[++i]);
        else if (args[i].equals("-rule"))
          arguments.setProperty("Rule", args[++i]);
        else
        {
          error = "unknown argument: " + args[i];
          ok = false;
        }
      }
    }

    if (ok)
    {
      if (arguments.getProperty("File") == null &&
          arguments.getProperty("String") == null)
      {
        error = "insufficient arguments: -file or -string required";
        ok = false;
      }
    }

    if (!ok)
    {
      System.out.println("error: " + error);
      System.out.println("usage: Parser [-rule rulename] [-trace] <-file file | -string string> [-visitor visitor]");
    }
    else
    {
      try
      {
        Rule rule = null;

        if (arguments.getProperty("File") != null)
        {
          rule = 
            parse(
              arguments.getProperty("Rule"), 
              new File(arguments.getProperty("File")), 
              arguments.getProperty("Trace").equals("On"));
        }
        else if (arguments.getProperty("String") != null)
        {
          rule = 
            parse(
              arguments.getProperty("Rule"), 
              arguments.getProperty("String"), 
              arguments.getProperty("Trace").equals("On"));
        }

        if (arguments.getProperty("Visitor") != null)
        {
          Visitor visitor = 
            (Visitor)Class.forName(arguments.getProperty("Visitor")).newInstance();
          rule.accept(visitor);
        }
      }
      catch (IllegalArgumentException e)
      {
        System.out.println("argument error: " + e.getMessage());
      }
      catch (IOException e)
      {
        System.out.println("io error: " + e.getMessage());
      }
      catch (ParserException e)
      {
        System.out.println("parser error: " + e.getMessage());
      }
      catch (ClassNotFoundException e)
      {
        System.out.println("visitor error: class not found - " + e.getMessage());
      }
      catch (IllegalAccessException e)
      {
        System.out.println("visitor error: illegal access - " + e.getMessage());
      }
      catch (InstantiationException e)
      {
        System.out.println("visitor error: instantiation failure - " + e.getMessage());
      }
    }
  }

  static public Rule parse(String rulename, String string)
  throws IllegalArgumentException,
         ParserException
  {
    return parse(rulename, string, false);
  }

  static public Rule parse(String rulename, InputStream in)
  throws IllegalArgumentException,
         IOException,
         ParserException
  {
    return parse(rulename, in, false);
  }

  static public Rule parse(String rulename, File file)
  throws IllegalArgumentException,
         IOException,
         ParserException
  {
    return parse(rulename, file, false);
  }

  static private Rule parse(String rulename, String string, boolean trace)
  throws IllegalArgumentException,
         ParserException
  {
    if (rulename == null)
      throw new IllegalArgumentException("null rulename");
    if (string == null)
      throw new IllegalArgumentException("null string");

    ParserContext context = new ParserContext(string, trace);

    Rule rule = null;
    if (rulename.equalsIgnoreCase("did-reference")) rule = Rule_did_reference.parse(context);
    else if (rulename.equalsIgnoreCase("did")) rule = Rule_did.parse(context);
    else if (rulename.equalsIgnoreCase("method")) rule = Rule_method.parse(context);
    else if (rulename.equalsIgnoreCase("namechar")) rule = Rule_namechar.parse(context);
    else if (rulename.equalsIgnoreCase("specific-idstring")) rule = Rule_specific_idstring.parse(context);
    else if (rulename.equalsIgnoreCase("idstring")) rule = Rule_idstring.parse(context);
    else if (rulename.equalsIgnoreCase("idchar")) rule = Rule_idchar.parse(context);
    else if (rulename.equalsIgnoreCase("service")) rule = Rule_service.parse(context);
    else if (rulename.equalsIgnoreCase("did-path")) rule = Rule_did_path.parse(context);
    else if (rulename.equalsIgnoreCase("did-query")) rule = Rule_did_query.parse(context);
    else if (rulename.equalsIgnoreCase("did-fragment")) rule = Rule_did_fragment.parse(context);
    else if (rulename.equalsIgnoreCase("path-rootless")) rule = Rule_path_rootless.parse(context);
    else if (rulename.equalsIgnoreCase("segment")) rule = Rule_segment.parse(context);
    else if (rulename.equalsIgnoreCase("segment-nz")) rule = Rule_segment_nz.parse(context);
    else if (rulename.equalsIgnoreCase("segment-nz-nc")) rule = Rule_segment_nz_nc.parse(context);
    else if (rulename.equalsIgnoreCase("pchar")) rule = Rule_pchar.parse(context);
    else if (rulename.equalsIgnoreCase("query")) rule = Rule_query.parse(context);
    else if (rulename.equalsIgnoreCase("fragment")) rule = Rule_fragment.parse(context);
    else if (rulename.equalsIgnoreCase("pct-encoded")) rule = Rule_pct_encoded.parse(context);
    else if (rulename.equalsIgnoreCase("unreserved")) rule = Rule_unreserved.parse(context);
    else if (rulename.equalsIgnoreCase("reserved")) rule = Rule_reserved.parse(context);
    else if (rulename.equalsIgnoreCase("gen-delims")) rule = Rule_gen_delims.parse(context);
    else if (rulename.equalsIgnoreCase("sub-delims")) rule = Rule_sub_delims.parse(context);
    else if (rulename.equalsIgnoreCase("ALPHA")) rule = Rule_ALPHA.parse(context);
    else if (rulename.equalsIgnoreCase("DIGIT")) rule = Rule_DIGIT.parse(context);
    else if (rulename.equalsIgnoreCase("HEXDIG")) rule = Rule_HEXDIG.parse(context);
    else throw new IllegalArgumentException("unknown rule");

    if (rule == null)
    {
      throw new ParserException(
        "rule \"" + (String)context.getErrorStack().peek() + "\" failed",
        context.text,
        context.getErrorIndex(),
        context.getErrorStack());
    }

    if (context.text.length() > context.index)
    {
      ParserException primaryError = 
        new ParserException(
          "extra data found",
          context.text,
          context.index,
          new Stack<String>());

      if (context.getErrorIndex() > context.index)
      {
        ParserException secondaryError = 
          new ParserException(
            "rule \"" + (String)context.getErrorStack().peek() + "\" failed",
            context.text,
            context.getErrorIndex(),
            context.getErrorStack());

        primaryError.initCause(secondaryError);
      }

      throw primaryError;
    }

    return rule;
  }

  static private Rule parse(String rulename, InputStream in, boolean trace)
  throws IllegalArgumentException,
         IOException,
         ParserException
  {
    if (rulename == null)
      throw new IllegalArgumentException("null rulename");
    if (in == null)
      throw new IllegalArgumentException("null input stream");

    int ch = 0;
    StringBuffer out = new StringBuffer();
    while ((ch = in.read()) != -1)
      out.append((char)ch);

    return parse(rulename, out.toString(), trace);
  }

  static private Rule parse(String rulename, File file, boolean trace)
  throws IllegalArgumentException,
         IOException,
         ParserException
  {
    if (rulename == null)
      throw new IllegalArgumentException("null rulename");
    if (file == null)
      throw new IllegalArgumentException("null file");

    try (BufferedReader in = new BufferedReader(new FileReader(file))) {
      int ch;
      StringBuilder out = new StringBuilder();
      while ((ch = in.read()) != -1) {
        out.append((char) ch);
      }
      return parse(rulename, out.toString(), trace);
    }
  }
}

/* -----------------------------------------------------------------------------
 * eof
 * -----------------------------------------------------------------------------
 */

/* -----------------------------------------------------------------------------
 * ParserException.java
 * -----------------------------------------------------------------------------
 *
 * Producer : com.parse2.aparse.Parser 2.5
 * Produced : Fri Feb 09 17:16:10 CET 2018
 *
 * -----------------------------------------------------------------------------
 */

package uniresolver.did.parser;

import java.util.Stack;

/**
 * <p>Signals that a parse failure has occurred.</p>
 * 
 * <p>Producer : com.parse2.aparse.Parser 2.5<br/>
 * Produced : Fri Feb 09 17:16:10 CET 2018</p>
 */

public class ParserException extends Exception
{
  private String reason;
  private String text60;
  private int index60;
  private Stack<String> ruleStack;

  static final private String newline = System.getProperty("line.separator", "\n");

  /**
   * Creates a parser exception from the specified parse failure information.
   *
   * @param reason A description of the parse failure.
   * @param text The string of characters being parsed.
   * @param index The index to the character at which the parse failure occurred.
   * @param ruleStack The ABNF rule stack at the point the parse failure occurred.
   */

  public ParserException(
    String reason,
    String text,
    int index,
    Stack<String> ruleStack)
  {
    this.reason = reason;
    this.ruleStack = ruleStack;

    int start = (index < 30) ? 0: index - 30;
    int end = (text.length() < index + 30) ? text.length(): index + 30;
    text60 = text.substring(start, end).replaceAll("[\\x00-\\x1F]", " ");
    index60 = (index < 30) ? index : 30;
  }

  /**
   * Returns the description of the parse failure.
   *
   * @return The description of the parse failure.
   */

  public String getReason()
  {
    return reason;
  }

  /**
   * Returns a substring of the parsed string that encompasses the point 
   * at which the parse failure occurred. The substring will be up to 60 
   * characters in length unless the point of failure occurred within 
   * 30 characters of the start or end of the parsed string. 
   * {@link #getSubstringIndex} returns an index to the character within 
   * this substring at which the parse failure occurred. This substring 
   * may contain non-printable characters.
   *
   * @return The substring that encompasses the point of failure.
   */

  public String getSubstring()
  {
    return text60;
  }

  /**
   * Returns an index to the character within the substring returned by 
   * {@link #getSubstring} at which the parse failure occurred. 
   *
   * @return The index to the character within the substring returned 
   * {@link #getSubstring} at which the parse failure occurred. 
   */

  public int getSubstringIndex()
  {
    return index60;
  }

  /**
   * Returns the ABNF rule stack at the point the parse failure occurred.
   *
   * @return The ABNF rule stack.
   */

  public Stack<String> getRuleStack()
  {
    return ruleStack;
  }

  /**
   * Returns a message detailing the parse failure. The message detail
   * the reason for the failure and where the failure occurred.
   *
   * <br><br>For example ...<br><br><code>
   * rule "Minutes" failed<br>
   * 15:75:47<br>
   * &nbsp;&nbsp;&nbsp;^<br>
   * rule stack:<br>
   * &nbsp;&nbsp;Clock<br>
   * &nbsp;&nbsp;Minutes</code><br>
   *
   * @return Details of the parse failure.
   */

  public String getMessage()
  {
    String marker = "                              ";

    StringBuffer buffer = new StringBuffer();
    buffer.append(reason + newline);
    buffer.append(text60 + newline);
    buffer.append(marker.substring(0, index60) + "^" + newline);

    if (!ruleStack.empty())
    {
      buffer.append("rule stack:");

      for (String rule : ruleStack)
        buffer.append(newline + "  " + rule);
    }

    ParserException secondaryError = (ParserException)getCause();
    if (secondaryError != null)
    {
      buffer.append("possible cause: " + secondaryError.reason + newline);
      buffer.append(secondaryError.text60 + newline);
      buffer.append(marker.substring(0, secondaryError.index60) + "^" + newline);

      if (!secondaryError.ruleStack.empty())
      {
        buffer.append("rule stack:");

        for (String rule : secondaryError.ruleStack)
          buffer.append(newline + "  " + rule);
      }
    }

    return buffer.toString();
  }
}

/* -----------------------------------------------------------------------------
 * eof
 * -----------------------------------------------------------------------------
 */

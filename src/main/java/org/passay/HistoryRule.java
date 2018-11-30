/* See LICENSE for licensing and NOTICE for copyright. */
package org.passay;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Rule for determining if a password matches one of any previous password a user has chosen. If no historical password
 * reference has been set, then passwords will meet this rule. See {@link PasswordData#setPasswordReferences(List)}.
 *
 * @author  Middleware Services
 */
public class HistoryRule implements Rule
{

  /** Error code for history violation. */
  public static final String ERROR_CODE = "HISTORY_VIOLATION";

  /** Whether to report all history matches or just the first. */
  protected boolean reportAllFailures;

  /** The size of the password history to be displayed in the error message
   *  instead of the size of provided password reference list
   *  (e.g. message is "password can't match the previous 4 passwords" but
   *  the historical password reference list size is only 1) */
  protected Integer sizeToReport;


  /**
   * Creates a new history rule.
   */
  public HistoryRule()
  {
    this(true);
  }


  /**
   * Creates a new history rule.
   *
   * @param  reportAll  whether to report all matches or just the first
   */
  public HistoryRule(final boolean reportAll)
  {
    reportAllFailures = reportAll;
  }


  @Override
  public RuleResult validate(final PasswordData passwordData)
  {
    final RuleResult result = new RuleResult(true);
    final List<PasswordData.HistoricalReference> references = passwordData.getPasswordReferences(
      PasswordData.HistoricalReference.class);
    final int size = references.size();
    if (size == 0) {
      return result;
    }

    final String cleartext = passwordData.getPassword();
    if (reportAllFailures) {
      references.stream().filter(reference -> matches(cleartext, reference)).forEach(reference -> {
        result.setValid(false);
        result.getDetails().add(new RuleResultDetail(ERROR_CODE, createRuleResultDetailParameters(size)));
      });
    } else {
      references.stream().filter(reference -> matches(cleartext, reference)).findFirst().ifPresent(reference -> {
        result.setValid(false);
        result.getDetails().add(new RuleResultDetail(ERROR_CODE, createRuleResultDetailParameters(size)));
      });
    }
    return result;
  }


  /**
   * Determines whether a password matches an historical password.
   *
   * @param  password  candidate password
   * @param  reference  reference password
   *
   * @return  true if passwords match, false otherwise.
   */
  protected boolean matches(final String password, final PasswordData.Reference reference)
  {
    return password.equals(reference.getPassword());
  }


  /**
   * Creates the parameter data for the rule result detail.
   *
   * @param  size  of the history list
   *
   * @return  map of parameter name to value
   */
  protected Map<String, Object> createRuleResultDetailParameters(final int size)
  {
    final Map<String, Object> m = new LinkedHashMap<>();
    m.put("historySize", sizeToReport == null ? size : sizeToReport);
    return m;
  }


  /**
   * Sets the size to report if the password is not valid
   *
   * @param sizeToReport The size to report
   */
  public void setSizeToReport(int sizeToReport) {
    this.sizeToReport = sizeToReport;
  }
}

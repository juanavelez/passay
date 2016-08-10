---
layout: default
title: Reference manual
---
The Passay API consists of 3 core components:

1. [`Rule`](../javadocs/org/passay/Rule.html) - one or more rules define a password policy rule set
2. [`PasswordValidator`](../javadocs/org/passay/PasswordValidator.html) - validates a password against a rule set
3. [`PasswordGenerator`](../javadocs/org/passay/PasswordGenerator.html) - produces passwords that satisfy a given rule set

# Rule overview
Rules are the building blocks for both password validation and generation, and it is helpful to review the ruleset that
passay provides out of the box. There are two broad categories of rules:

1. Positive match require that passwords satisfy a rule
2. Negative match reject passwords that satisfy a rule

The following sections briefly describe the available rules in both categories.

## Positive matching rules
1. [`AllowedCharacterRule`](../javadocs/org/passay/AllowedCharacterRule.html) -
requires passwords to contain _all_ of a set of characters
2. [`AllowedRegexRule`](../javadocs/org/passay/AllowedRegexRule.html) -
requires passwords to conform to a regular expression
3. [`CharacterCharacteristicsRule`](../javadocs/org/passay/CharacterCharacteristicsRule.html) -
requires passwords to contain M of N classes of characters; for example, 3 of 4 of the following: digit, upper-case
letters, lower-case letters, symbols
4. [`CharacterRule`](../javadocs/org/passay/CharacterRule.html) -
requires passwords to contain at least N characters from a given character set (e.g. digits, upper-case letters,
lowercase-letters, symbols)
5. [`LengthRule`](../javadocs/org/passay/LengthRule.html) - requires passwords to meet a minimum required length

## Negative matching rules
1. Dictionary rules
   1. [`DictionaryRule`](../javadocs/org/passay/DictionaryRule.html) -
   rejects passwords that _match_ an entry in a dictionary (exact match semantics)
   2. [`DictionarySubstringRule`](../javadocs/org/passay/DictionarySubstringRule.html) -
   rejects passwords that _contain_ an entry in a dictionary (substring match semantics)
2. History rules
   1. [`HistoryRule`](../javadocs/org/passay/HistoryRule.html) -
   rejects passwords that match previous passwords (cleartext comparison)
   2. [`DigestHistoryRule`](../javadocs/org/passay/DigestHistoryRule.html) -
   rejects passwords that match previous password digests (hash/digest comparison)
3. [`IllegalCharacterRule`](../javadocs/org/passay/IllegalCharacterRule.html) -
rejects passwords that contain _any_ of a set of characters
4. [`IllegalRegexRule`](../javadocs/org/passay/IllegalRegexRule.html) -
rejects passwords that conform to a regular expression
5. [`IllegalSequenceRule`](../javadocs/org/passay/IllegalSequenceRule.html) -
rejects passwords that contain a sequence of N characters (e.g. _12345_)
6. Source rules
   1. [`SourceRule`](../javadocs/org/passay/SourceRule.html) -
   rejects passwords that match those from another source (cleartext comparison)
   2. [`DigestSourceRule`](../javadocs/org/passay/DigestSourceRule.html) -
   rejects passwords that match the digest of those from another source (hash/digest comparison)
7. [`RepeatCharacterRegexRule`](../javadocs/org/passay/RepeatCharacterRegexRule.html) -
rejects passwords that contain a repeated ASCII character
8. [`UsernameRule`](../javadocs/org/passay/UsernameRule.html) -
rejects passwords that contain the username of the user providing the password
9. [`WhitespaceRule`](../javadocs/org/passay/WhitespaceRule.html) -
rejects passwords that contain whitespace characters

## API changes from 1.0 to 1.1.0
Required character and invalid sequence rules have been consolidated in 1.1.0 to facilitate extension to arbitrary
character sets.

`CharacterRule` replaces the following 1.0 components:

1. `AlphabeticalCharacterRule` -> `CharacterRule(EnglishCharacterData.Alphabetical)`
2. `DigitCharacterRule` -> `CharacterRule(EnglishCharacterData.Digit)`
3. `LowercaseCharacterRule` -> `CharacterRule(EnglishCharacterData.LowerCase)`
4. `SpecialCharacterRule` -> `CharacterRule(EnglishCharacterData.Special)`
5. `UppercaseCharacterRule` -> `CharacterRule(EnglishCharacterData.UpperCase)`

`IllegalSequenceRule` replaces the following 1.0 components:

1. `AlphabeticalSequenceRule` -> `IllegalSequenceRule(EnglishSequenceData.Alphabetical)`
2. `NumericSequenceRule` -> `IllegalSequenceRule(EnglishSequenceData.Numerical)`
3. `QwertySequenceRule` -> `IllegalSequenceRule(EnglishSequenceData.USQwerty)`

# Password validation
Password validation involves creating a `PasswordValidator` from a rule set, which is simply a list of `Rule` objects.
Consider the following simple password policy:

1. Length of 8 to 16 characters
2. Must contain at least one of the following: upper case, lower case, digit, and symbol
3. No whitespace characters

The following code excerpt constructs a validator that enforces the policy.

{% highlight java %}
PasswordValidator validator = new PasswordValidator(Arrays.asList(
  // length between 8 and 16 characters
  new LengthRule(8, 16),

  // at least one upper-case character
  new CharacterRule(EnglishCharacterData.UpperCase, 1),

  // at least one lower-case character
  new CharacterRule(EnglishCharacterData.LowerCase, 1),

  // at least one digit character
  new CharacterRule(EnglishCharacterData.Digit, 1),

  // at least one symbol (special character)
  new CharacterRule(EnglishCharacterData.Special, 1),

  // no whitespace
  new WhitespaceRule()));

final char[] password = System.console().readPassword("Password: ");
RuleResult result = validator.validate(new PasswordData(new String(password)));
if (result.isValid()) {
  System.out.println("Password is valid");
} else {
  System.out.println("Invalid password:");
  for (String msg : validator.getMessages(result)) {
    System.out.println(msg);
  }
}
{% endhighlight %}

## Advanced validation: customizing messages
Passay provides the [`MessageResolver`](../javadocs/org/passay/MessageResolver.html) interface to allow arbitrary
conversion of password validation results to meaningful text intended for display to users. The default mechanism
uses a message bundle to define validation messages whose default values are shown below.

    HISTORY_VIOLATION=Password matches one of %1$s previous passwords.
    ILLEGAL_WORD=Password contains the dictionary word '%1$s'.
    ILLEGAL_WORD_REVERSED=Password contains the reversed dictionary word '%1$s'.
    ILLEGAL_MATCH=Password matches the illegal pattern '%1$s'.
    ALLOWED_MATCH=Password must match pattern '%1$s'.
    ILLEGAL_CHAR=Password contains the illegal character '%1$s'.
    ALLOWED_CHAR=Password contains the illegal character '%1$s'.
    ILLEGAL_SEQUENCE=Password contains the illegal sequence '%1$s'.
    ILLEGAL_USERNAME=Password contains the user id '%1$s'.
    ILLEGAL_USERNAME_REVERSED=Password contains the user id '%1$s' in reverse.
    ILLEGAL_WHITESPACE=Password cannot contain whitespace characters.
    INSUFFICIENT_UPPERCASE=Password must contain at least %1$s uppercase characters.
    INSUFFICIENT_LOWERCASE=Password must contain at least %1$s lowercase characters.
    INSUFFICIENT_ALPHABETICAL=Password must contain at least %1$s alphabetical characters.
    INSUFFICIENT_DIGIT=Password must contain at least %1$s digit characters.
    INSUFFICIENT_SPECIAL=Password must contain at least %1$s special characters.
    INSUFFICIENT_CHARACTERISTICS=Password matches %1$s of %3$s character rules, but %2$s are required.
    SOURCE_VIOLATION=Password cannot be the same as your %1$s password.
    TOO_LONG=Password must be no more than %2$s characters in length.
    TOO_SHORT=Password must be at least %1$s characters in length.

The following example demonstrates how to replace the default message bundle with a custom/localized properties file.

{% highlight java %}
Properties props = new Properties();
props.load(new FileInputStream("/path/to/messages.properties"));
MessageResolver resolver = new PropertiesMessageResolver(props);
PasswordValidator validator = new PasswordValidator(resolver, ruleList);
{% endhighlight %}

## Advanced validation: M of N rules
Many password policies contain a rule of the form _password must contain at least M of the following N categories_.
The [`CharacterCharacteristicsRule`](../javadocs/org/passay/CharacterCharacteristicsRule.html) component supports
this use case. Consider the following policy:

1. Length of 8 to 16 characters
2. Must contain characters from at least 3 of the following: upper, lower, digit, symbol
3. No whitespace characters

This policy is implemented in the following code excerpt.

{% highlight java %}
LengthRule r1 = new LengthRule(8, 16);

CharacterCharacteristicsRule r2 = new CharacterCharacteristicsRule();

// Define M (3 in this case)
r2.setNumberOfCharacteristics(3);

// Define elements of N (upper, lower, digit, symbol)
r2.getRules().add(new CharacterRule(EnglishCharacterData.UpperCase, 1));
r2.getRules().add(new CharacterRule(EnglishCharacterData.LowerCase, 1));
r2.getRules().add(new CharacterRule(EnglishCharacterData.Digit, 1));
r2.getRules().add(new CharacterRule(EnglishCharacterData.Special, 1));

WhitespaceRule r3 = new WhitespaceRule();

PasswordValidator validator = new PasswordValidator(Arrays.asList(r1, r2, r3));
{% endhighlight %}

## Advanced validation: dictionary rules
Many password policies seek to prevent common words (e.g. _password_) from appearing in passwords. Passay ships with
two rules that can be used with arbitrary word lists to enforce dictionary policies:

1. [`DictionaryRule`](../javadocs/org/passay/DictionaryRule.html) - exact matching semantics
2. [`DictionarySubstringRule`](../javadocs/org/passay/DictionarySubstringRule.html) - contains matching semantics

`DictionarySubstringRule` should be used judiciously since a configuration with a sizeable common word list would
prevent strong passwords like [_correcthorsebatterystaple_](http://xkcd.com/936/) and _random.words@31415_.
A reasonable use case might be a relatively short list of reserved words forbidden to appear in passwords.

`DictionaryRule`, on the other hand, has a valuable common use case: preventing known weak passwords.
Configuring this rule with a published list of popular passwords, such as those from the
[Adobe breach](http://stricture-group.com/files/adobe-top100.txt), can dramatically increase password security by
preventing common, and therefore insecure, passwords.

{% highlight java %}
DictionaryRule rule = new DictionaryRule(
	new WordListDictionary(WordLists.createFromReader(
	  // Reader around the word list file
  	new FileReader[] {new FileReader("path/to/top100.txt")},
  	// True for case sensitivity, false otherwise
  	false,
  	// Dictionaries must be sorted
  	new ArraysSort())));
{% endhighlight %}

## Advanced validation: password history
The following rules support enforcement of unique passwords in the context of password history:

1. [`HistoryRule`](../javadocs/org/passay/HistoryRule.html) - for passwords stored as cleartext (insecure, uncommon)
2. [`DigestHistoryRule`](../javadocs/org/passay/HistoryRule.html) - for passwords stored as a hash/digest

Both rules require querying a data source for historical password data, but in practice `DigestHistoryRule` is the
more useful component since passwords are typically stored as a hash/digest. Digest support requires the use of
message digest components provided by the [cryptacular](http://www.cryptacular.org/) crypto library. The
example below demonstrates history-based validation for passwords stored in the following format:

1. SHA-256 digest algorithm
2. The hash is computed by digesting two values in turn:
   1. Password string as UTF-8 characters
   2. Random 16-byte salt value
3. The salt is appended to the hash output to form a 48-byte value (32-byte hash + 16-byte salt)
4. The hash output is encoded as base64 characters

This is a realistic scenario for passwords stored in an LDAP directory using the SSHA pseudo-standard.

{% highlight java %}
// The historical data would be obtained from an authentication store in a
// real-world scenario. Each item consists of a label and the encoded password
// data. A common use case for labels is multiple password encodings where each
// label identifies a particular encoding.
// Salt=86ffd2e3521b5b169ec9a75678c92eed
List<PasswordData.Reference> history = Arrays.asList(
  // Password=P@ssword1
  new PasswordData.HistoricalReference(
    "SHA256",
    "j93vuQDT5ZpZ5L9FxSfeh87zznS3CM8govlLNHU8GRWG/9LjUhtbFp7Jp1Z4yS7t"),

  // Password=P@ssword2
  new PasswordData.HistoricalReference(
    "SHA256",
    "mhR+BHzcQXt2fOUWCy4f903AHA6LzNYKlSOQ7r9np02G/9LjUhtbFp7Jp1Z4yS7t"),

  // Password=P@ssword3
  new PasswordData.HistoricalReference(
    "SHA256",
    "BDr/pEo1eMmJoeP6gRKh6QMmiGAyGcddvfAHH+VJ05iG/9LjUhtbFp7Jp1Z4yS7t")
);

// Cryptacular components:
// org.cryptacular.bean.EncodingHashBean;
// org.cryptacular.spec.CodecSpec;
// org.cryptacular.spec.DigestSpec;
EncodingHashBean hasher = new EncodingHashBean(
  new CodecSpec("Base64"), // Handles base64 encoding
  new DigestSpec("SHA256"), // Digest algorithm
  1, // Number of hash rounds
  false); // Salted hash == false

List<Rule> rules = Arrays.asList(
  // ...
  // Insert other rules as needed
  // ...
  new DigestHistoryRule(hasher));

PasswordValidator validator = new PasswordValidator(rules);
PasswordData data = PasswordData.newInstance("P@ssword1", "username", history);
RuleResult result = validator.validate(data);
{% endhighlight %}

# Password generation
The password generation API uses a specialized ruleset consisting exclusively of `CharacterRule`, a specialization of
`Rule`, to define the requisite character classes in generated passwords. The example below demonstrates
password generation for the following policy:

1. Length of 8 to 16 characters
2. Must contain at least one of the following: upper case, lower case, digit, and symbol
3. No whitespace characters

{% highlight java %}
List<CharacterRule> rules = Arrays.asList(
  // at least one upper-case character
  new CharacterRule(EnglishCharacterData.UpperCase, 1),

  // at least one lower-case character
  new CharacterRule(EnglishCharacterData.LowerCase, 1),

  // at least one digit character
  new CharacterRule(EnglishCharacterData.Digit, 1),

  // at least one symbol (special character)
  new CharacterRule(EnglishCharacterData.Special, 1));

PasswordGenerator generator = new PasswordGenerator();

// Generated password is 12 characters long, which complies with policy
String password = generator.generatePassword(12, rules);
{% endhighlight %}

Note that generated passwords in the above example don't contain spaces since none of the character sets above
includes a space character. It is trivial to add support for generating passwords with spaces by including an
additional `CharacterRule` with a custom character set as follows.

{% highlight java %}
new CharacterRule(new CharacterData() {
  @Override
  public String getErrorCode() {
    return "ERR_SPACE";
  }

  @Override
  public String getCharacters() {
    return " ";
  }
}, 1);
{% endhighlight %}
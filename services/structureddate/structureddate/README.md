# Structured Date Parser

This project contains classes that model CollectionSpace structured dates, and a parser to create structured dates from a display date string.

Maven 3 is required to build. ANTLR 4 is used for parser generation.

The ANTLR 4 grammar is located in src/main/antlr4. During the build, source code is generated into target/generated-sources/antlr4. Be sure to add this as a source folder in your IDE, so that it will be able to find the generated classes.

Parser tests are located in src/test/resources/test-dates.yaml. The comments in that file describe how to add tests.


#### Helpful resources:
- [ANTLR4 Documentation](https://github.com/antlr/antlr4/blob/master/doc/index.md)
    - [Setting up ANTLR4 graphical user interface](https://github.com/antlr/antlr4/blob/master/doc/getting-started.md)


## The Code
#### StructuredDate.g4
This file can be found under `services/services/structureddate/src/main/antlr4/.../antlr/`  

This is the file that defines the antlr4 grammar that the parser will be using. When antlr "reads" it, it puts it in the form of a tree. Depending on the input, the evaluator will follow specific branches that fit the rules associated with them. If the input fits a certain rule, the evaluator will go down that branch. For example, for the input `Jan 1 2015 - April 13 2018`, the tree that is explored would look like this: 

![](https://i.imgur.com/5B9UhTK.png)

At the leaves of the tree, the evaluator can transform and validate the data. For example, the listener for `strMonth` transforms the strings `jan` and `april` to their respective numbers, `1` and `4`. In the case of `numDayOfMonth` the listener makes sure that the number that was entered is `(dayOfMonth == 0 || dayOfMonth > 31)`. 

When we need to add support for a specific format, we need to add it to the tree. For example, if we want to add `unknownDate` (which parses the input "unknown"), we start by adding a lexer rule that will allow the parser to recognize the token "unknown". It would look something like this: 

`UNKNOWN:		'unknown' | 'UNKNOWN' ;`

After this, we would need to add a parser rule that will use this token. Our parser rule will look something like this:

`unknownDate:           UNKNOWN ;`

Which means `unknownDate` will consist of solely the token defined by the lexer rule `UNKNOWN`.
Finally, we need to add this to our list of possble date types:
``` java
oneDisplayDate:        displayDate ( DOT | QUESTION )? EOF ; 

displayDate:           uncertainDate
|                      certainDate
|                      beforeOrAfterDate
|                      unknownDate
;
```
Once the lexer and parser rules are made, we can now move on to the evaluator.





#### ANTLRStructuredDateEvaluator.java
This file can be found under `services/services/structureddate/src/main/java/.../antlr/`
Each node of the tree (parser rules) is a listener. The base class for these listeners does nothing, but they can be overriden so that they perform useful actions for us, such as creating `Date` objects and reordering things within the global stack. Every listener method is prefixed with either `enter` or `exit`, followed by the name of the parser rule, and take in a `ParserRuleContext` object, which is the name of the parser rule, suffixed by`Context`. In the following example, we implement the listener for `numDayOfMonth`:
```java 
@Override
public void exitNumDayOfMonth(NumDayOfMonthContext ctx) {
   if (ctx.exception != null) return;

   // Convert the numeric string to an Integer, 
   // and push on the stack.

   Integer dayOfMonth = new Integer(ctx.NUMBER().getText());

   if (dayOfMonth == 0 || dayOfMonth > 31) {
      throw new StructuredDateFormatException("unexpected day of month '" + ctx.NUMBER().getText() + "'");
   }

   stack.push(dayOfMonth);
}
```
In this method, we extract the number of the day of the month, turn it into an int, and push it into the stack, where a higher level of the tree (ex: `exitStrDate` ) will `pop()` it off to contruct a date. For example:

``` java
@Override
public void exitStrDate(StrDateContext ctx) {
   if (ctx.exception != null) return;   
   // Reorder the stack into a canonical ordering,
   // year-month-day-era.

   Era era = (ctx.era() == null) ? null : (Era) stack.pop();
   Integer year = (Integer) stack.pop();
   Integer dayOfMonth = (Integer) stack.pop();
   Integer numMonth = (Integer) stack.pop();

   stack.push(year);
   stack.push(numMonth);
   stack.push(dayOfMonth);
   stack.push(era);
}
```
If we had the input `april 13 2018`, the evaluator the parser rule that will be followed is 
`strDate:               strMonth ( numDayOfMonth | nth ) COMMA? numYear era? ;
`
The listeners for the indivual tokens `push()` an element into the stack, which is in charge of keeping track of all items. At the beginning of this method, the stack will look like `[month day year era]`, however, the stack must be reordered so that it looks like `[year month day era]`. Most lower-level listeners implement this logic, along with any other logic that may be involved. 


#### test-dates.yaml
This file can be found under `services/services/structureddate/structureddate/src/test/java/.../resources/`
 This file contains the test cases used by `StructuredDateEvaluatorTest.java`. When adding a new format to the structured date parser, be sure to also add tests to this file so that you are sure it works. The syntax is easy to follow: 
 ```yaml
   "10/2005-12/2006":                    # Month/Year - Month/Year date
                                         earliestSingleDate: [2005,  10, 1, CE]
                                         latestDate:         [2006,  12, 31, CE]

 ```
 Both of these don't need to be included. In the case that `latestDate` is missing, it is expected that it will be the same as the `earliestSingleDate`. The `earliestSingleDate` and `latestDate` should be specified as a list: `[year, month, day, era, certainty, qualifierType, qualifierValue, qualifierUnit]`. Values at the end of the list may be omitted, in which case they will be expected to be null. 
 

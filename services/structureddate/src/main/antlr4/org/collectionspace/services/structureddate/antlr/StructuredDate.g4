grammar StructuredDate;

/*
 * This is a grammar for ANTLR 4 (http://www.antlr.org/).
 *
 * TODO: 
 *   Allow YYYY-MM-DD and MM-DD-YYYY
 *   Allow month nth, year
 *
 * To generate the lexer, parser, and listener classes, use the command: 
 *     java -jar /usr/local/lib/antlr-4.1-complete.jar -package org.collectionspace.services.structureddate.antlr StructuredDate.g4
 */

/*
 * Parser rules
 */

oneDisplayDate: displayDate EOF ; 
 
displayDate:    CIRCA year (BCE|AD)?                                   # circaYear
|               year (BCE|AD)?                                         # yearOnly
|               nth QUARTER year                                       # toDo
|               LAST QUARTER year                                      # toDo
|               nth HALF year                                          # toDo
|               LAST HALF year                                         # toDo
|               year HYPHEN year                                       # toDo
|               CIRCA year HYPHEN year                                 # toDo
|               CIRCA year HYPHEN year BCE                             # toDo
|               CIRCA year BCE HYPHEN year BCE                         # toDo
|               CIRCA year BCE HYPHEN year                             # toDo
|               CIRCA year BCE HYPHEN year AD                          # toDo
|               year BCE HYPHEN year BCE                               # toDo
|               year HYPHEN year BCE                                   # toDo
|               year BCE HYPHEN year                                   # toDo
|               year BCE HYPHEN year AD                                # toDo
|               season year                                            # toDo
|               season HYPHEN season year                              # toDo
|               season year HYPHEN season year                         # toDo
|               season year BCE                                        # toDo
|               partOf year                                            # toDo
|               partOf year BCE                                        # toDo
|               month                                                  # monthOnly
|               month HYPHEN month                                     # toDo
|               partOf singleDate BCE                                  # toDo
|               singleDate BCE                                         # toDo
|               partOf singleDate                                      # toDo
|               singleDate                                             # singleDateOnly
|               singleDate HYPHEN singleDate                           # toDo
|               nth CENTURY                                            # toDo
|               CIRCA nth CENTURY                                      # toDo
|               nth CENTURY AD                                         # toDo
|               nth CENTURY BCE                                        # toDo
|               CIRCA nth CENTURY BCE                                  # toDo
|               nth CENTURY HYPHEN nth CENTURY                         # toDo
|               nth HYPHEN nth CENTURY                                 # toDo
|               nth HYPHEN nth CENTURY BCE                             # toDo
|               nth CENTURY BCE HYPHEN nth CENTURY BCE                 # toDo
|               nth CENTURY BCE HYPHEN nth CENTURY                     # toDo
|               nth CENTURY BCE HYPHEN nth CENTURY AD                  # toDo
|               nth QUARTER nth CENTURY                                # toDo
|               nth QUARTER nth CENTURY HYPHEN nth QUARTER nth CENTURY # toDo
|               LAST QUARTER nth CENTURY                               # toDo
|               nth HALF nth CENTURY                                   # toDo
|               LAST HALF nth CENTURY                                  # toDo
|               partOf nth CENTURY                                     # toDo
|               partOf nth CENTURY BCE                                 # toDo
|               partOf nth CENTURY HYPHEN partOf nth CENTURY           # toDo
|               partOf nth HYPHEN partOf nth CENTURY                   # toDo
|               partOf nth HYPHEN nth CENTURY                          # toDo
|               partOf nth CENTURY BCE HYPHEN partOf nth CENTURY BCE   # toDo
|               partOf nth CENTURY BCE HYPHEN partOf nth CENTURY       # toDo
|               century                                                # toDo
|               partOf century                                         # toDo
|               partOf century HYPHEN partOf century                   # toDo
|               partOf century BCE HYPHEN partOf century BCE           # toDo
|               partOf century BCE                                     # toDo
|               CIRCA century                                          # toDo
|               CIRCA century BCE                                      # toDo
|               nth MILLENIUM                                          # toDo
|               nth MILLENIUM BCE                                      # toDo
|               decade                                                 # toDo
|               decade HYPHEN decade                                   # toDo
|               decade HYPHEN partOf decade                            # toDo
|               partOf decade HYPHEN partOf century                    # toDo
|               partOf decade HYPHEN partOf decade                     # toDo
|               partOf decade HYPHEN decade                            # toDo
|               partOf decade                                          # toDo
|               decade BCE                                             # toDo
|               partOf decade BCE                                      # toDo
|               CIRCA decade                                           # toDo
|               CIRCA decade BCE                                       # toDo
|               dateRange                                              # dateRangeOnly
;

dateRange:      monthOnlyRange
|               strDateRange
|               numDateRange
;              

singleDate:     numDate
|               strDate
|               invStrDate
;

month:          monthYear
|               invMonthYear
;

strDate:        strMonth dayOfMonth COMMA? year ;
invStrDate:     year COMMA? strMonth dayOfMonth ;
strDateRange:   strMonth dayOfMonth HYPHEN dayOfMonth COMMA? year ;
monthOnlyRange: strMonth HYPHEN strMonth COMMA? year ;
numDateRange:   numMonth SLASH dayOfMonth HYPHEN dayOfMonth SLASH year ;
numDate:        numMonth SLASH dayOfMonth SLASH year ;
monthYear:      strMonth COMMA? year ;
invMonthYear:   year COMMA? strMonth ;
decade:         TENS ;
century:        HUNDREDS ;
season:         SPRING | SUMMER | WINTER | FALL ;
partOf:         EARLY | MIDDLE | LATE | BEFORE | AFTER ;
nth:            NTHSTR | FIRST | SECOND | THIRD | FOURTH ;
strMonth:       MONTH | SHORTMONTH DOT?;
year:           NUMBER ;
numMonth:       NUMBER ;
dayOfMonth:     NUMBER ;


/*
 * Lexer rules
 */

WS:             [ \t\r\n]+ -> skip;
CIRCA:          ('c' | 'ca') '.'? | 'circa' ;
SPRING:         'spring' ;
SUMMER:         'summer' ;
WINTER:         'winter' ;
FALL:           'fall' | 'autumn' ;
EARLY:          'early' ;
MIDDLE:         'mid' ;
LATE:           'late' ;
BEFORE:         'before' | 'pre' '-'? ;
AFTER:          'after' | 'post' '-'?;
FIRST:          'first' ;
SECOND:         'second' ;
THIRD:          'third' ;
FOURTH:         'fourth' ;
LAST:           'last' ;
QUARTER:        'quarter' ;
HALF:           'half' ;
CENTURY:        'century' ;
MILLENIUM:      'millenium' ;
MONTH:          'january' | 'february' | 'march' | 'april' | 'may' | 'june' | 'july' | 'august' | 'september' | 'october' | 'november' | 'december' ;
SHORTMONTH:     'jan' | 'feb' | 'mar' | 'apr' | 'jun' | 'jul' | 'aug' | 'sep' | 'sept' | 'oct' | 'nov' | 'dec' ;
BCE:            'bc' 'e'? |  'b.c.' | 'b.c.e.' ;
AD:             'ad' | 'a.d.' | 'ce' | 'c.e.';
STRING:         [a-z]+ ;
NTHSTR:         [0-9]*? ([0456789] 'th' | '1st' | '2nd' | '3rd' | '11th' | '12th' | '13th') ;
HUNDREDS:       [0-9]*? '00' S;
TENS:           [0-9]*? '0' S;
NUMBER:         [0-9]+ ;
COMMA:          ',' ;
HYPHEN:         '-' ;
SLASH:          '/' ;
DOT:            '.' ;

fragment 
S:              '\''? 's' ;

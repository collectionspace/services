grammar StructuredDate;

/*
 * This is a grammar for ANTLR 4 (http://www.antlr.org/).
 *
 * TODO: 
 *   Allow YYYY-MM-DD
 *   Allow month nth, year
 */

/*
 * Parser rules
 */

oneDisplayDate: displayDate EOF ; 
 
displayDate:    CIRCA year (BC|AD)?                                    # circaYear
|               year (BC|AD)?                                          # yearOnly
|               nth QUARTER year                                       # toDo
|               LAST QUARTER year                                      # toDo
|               nth HALF year                                          # toDo
|               LAST HALF year                                         # toDo
|               year HYPHEN year                                       # toDo
|               CIRCA year HYPHEN year                                 # toDo
|               CIRCA year HYPHEN year BC                              # toDo
|               CIRCA year BC HYPHEN year BC                           # toDo
|               CIRCA year BC HYPHEN year                              # toDo
|               CIRCA year BC HYPHEN year AD                           # toDo
|               year BC HYPHEN year BC                                 # toDo
|               year HYPHEN year BC                                    # toDo
|               year BC HYPHEN year                                    # toDo
|               year BC HYPHEN year AD                                 # toDo
|               season year                                            # toDo
|               season HYPHEN season year                              # toDo
|               season year HYPHEN season year                         # toDo
|               season year BC                                         # toDo
|               partOf year                                            # toDo
|               partOf year BC                                         # toDo
|               month                                                  # monthOnly
|               month HYPHEN month                                     # toDo
|               partOf exactDate BC                                    # toDo
|               partOf exactDate                                       # toDo
|               exactDate (BC|AD)?                                     # exactDateOnly
|               exactDate HYPHEN exactDate                             # toDo
|               nth CENTURY                                            # toDo
|               CIRCA nth CENTURY                                      # toDo
|               nth CENTURY AD                                         # toDo
|               nth CENTURY BC                                         # toDo
|               CIRCA nth CENTURY BC                                   # toDo
|               nth CENTURY HYPHEN nth CENTURY                         # toDo
|               nth HYPHEN nth CENTURY                                 # toDo
|               nth HYPHEN nth CENTURY BC                              # toDo
|               nth CENTURY BC HYPHEN nth CENTURY BC                   # toDo
|               nth CENTURY BC HYPHEN nth CENTURY                      # toDo
|               nth CENTURY BC HYPHEN nth CENTURY AD                   # toDo
|               nth QUARTER nth CENTURY                                # toDo
|               nth QUARTER nth CENTURY HYPHEN nth QUARTER nth CENTURY # toDo
|               LAST QUARTER nth CENTURY                               # toDo
|               nth HALF nth CENTURY                                   # toDo
|               LAST HALF nth CENTURY                                  # toDo
|               partOf nth CENTURY                                     # toDo
|               partOf nth CENTURY BC                                  # toDo
|               partOf nth CENTURY HYPHEN partOf nth CENTURY           # toDo
|               partOf nth HYPHEN partOf nth CENTURY                   # toDo
|               partOf nth HYPHEN nth CENTURY                          # toDo
|               partOf nth CENTURY BC HYPHEN partOf nth CENTURY BC     # toDo
|               partOf nth CENTURY BC HYPHEN partOf nth CENTURY        # toDo
|               century                                                # toDo
|               partOf century                                         # toDo
|               partOf century HYPHEN partOf century                   # toDo
|               partOf century BC HYPHEN partOf century BC             # toDo
|               partOf century BC                                      # toDo
|               CIRCA century                                          # toDo
|               CIRCA century BC                                       # toDo
|               nth MILLENIUM                                          # toDo
|               nth MILLENIUM BC                                       # toDo
|               decade                                                 # toDo
|               decade HYPHEN decade                                   # toDo
|               decade HYPHEN partOf decade                            # toDo
|               partOf decade HYPHEN partOf century                    # toDo
|               partOf decade HYPHEN partOf decade                     # toDo
|               partOf decade HYPHEN decade                            # toDo
|               partOf decade                                          # toDo
|               decade BC                                              # toDo
|               partOf decade BC                                       # toDo
|               CIRCA decade                                           # toDo
|               CIRCA decade BC                                        # toDo
|               smallDateRange                                         # smallDateRangeOnly
;

smallDateRange: monthInYearRange
|               strDayInMonthRange
|               numDayInMonthRange
;              

exactDate:      numDate
|               strDate
|               invStrDate
;

month:          monthYear
|               invMonthYear
;

strDate:            strMonth dayOfMonth COMMA? year ;
invStrDate:         year COMMA? strMonth dayOfMonth ;
strDayInMonthRange: strMonth dayOfMonth HYPHEN dayOfMonth COMMA? year ;
monthInYearRange:   strMonth HYPHEN strMonth COMMA? year ;
numDayInMonthRange: numMonth SLASH dayOfMonth HYPHEN dayOfMonth SLASH year ;
numDate:            numMonth SLASH dayOfMonth SLASH year 
|                   numMonth HYPHEN dayOfMonth HYPHEN year ;
monthYear:          strMonth COMMA? year ;
invMonthYear:       year COMMA? strMonth ;
/*era:              BC | AD ; */
decade:             TENS ;
century:            HUNDREDS ;
season:             SPRING | SUMMER | WINTER | FALL ;
partOf:             EARLY | MIDDLE | LATE | BEFORE | AFTER ;
nth:                NTHSTR | FIRST | SECOND | THIRD | FOURTH ;
strMonth:           MONTH | SHORTMONTH DOT?;
year:               NUMBER ;
numMonth:           NUMBER ;
dayOfMonth:         NUMBER ;


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
MIDDLE:         'middle' | 'mid' '-'?;
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
BC:             'bc' | 'bce' |  'b.c.' | 'b.c.e.' ;
AD:             'ad' | 'a.d.' | 'ce' | 'c.e.';
NTHSTR:         [0-9]*? ([0456789] 'th' | '1st' | '2nd' | '3rd' | '11th' | '12th' | '13th') ;
HUNDREDS:       [0-9]*? '00' '\''? 's';
TENS:           [0-9]*? '0' '\''? 's';
NUMBER:         [0-9]+ ;
COMMA:          ',' ;
HYPHEN:         '-' ;
SLASH:          '/' ;
DOT:            '.' ;
STRING:         [a-z]+ ;

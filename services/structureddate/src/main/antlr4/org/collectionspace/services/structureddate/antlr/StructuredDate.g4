grammar StructuredDate;

/*
 * This is a grammar for ANTLR 4 (http://www.antlr.org/).
 *
 * TODO: 
 *   Allow YYYY-MM-DD
 */

/*
 * Parser rules
 */

oneDisplayDate:        displayDate (DOT | QUESTION)? EOF ; 

displayDate:           uncertainDate
|                      certainDate
/* TODO: Need to decide what "before" and "after" actually mean
|                      beforeOrAfterDate
*/
;

beforeOrAfterDate:     ( BEFORE | AFTER ) singleInterval ;

uncertainDate:         CIRCA certainDate ;

certainDate:           hyphenatedRange
|                      singleInterval
;

hyphenatedRange:       singleInterval HYPHEN singleInterval
|                      nthCenturyRange
|                      monthInYearRange
|                      quarterInYearRange
|                      strDayInMonthRange
|                      numDayInMonthRange
;

singleInterval:        yearSpanningWinter
|                      partialYear
|                      quarterYear
|                      halfYear
|                      millennium
|                      partialCentury
|                      quarterCentury
|                      halfCentury
|                      century
|                      partialDecade
|                      decade
|                      year
|                      month
|                      date
;

quarterInYearRange:    nthQuarterInYearRange
|                      strSeasonInYearRange
;

date:                  numDate
|                      strDate
|                      invStrDate
;

month:                 monthYear
|                      invMonthYear
;

yearSpanningWinter:    WINTER numYear SLASH numYear era ;

partialYear:           partOf numYear era ;

quarterYear:           seasonYear
|                      invSeasonYear
|                      nthQuarterYear
;

halfYear:              nthHalf numYear era ;

year:                  numYear era ;

partialDecade:         partOf numDecade era ;

decade:                numDecade era ;

partialCentury:        partOf (strCentury | numCentury) era ;

quarterCentury:        nthQuarter (strCentury | numCentury) era ;

halfCentury:           nthHalf (strCentury | numCentury) era ;

century:               (strCentury | numCentury) era ;

millennium:            nth MILLENNIUM era ;

strDate:               strMonth (numDayOfMonth | nth) COMMA? numYear era;
invStrDate:            era numYear COMMA? strMonth numDayOfMonth ;
strDayInMonthRange:    strMonth numDayOfMonth HYPHEN numDayOfMonth COMMA? numYear era ;
monthInYearRange:      strMonth HYPHEN strMonth COMMA? numYear era ;
nthQuarterInYearRange: nthQuarter HYPHEN nthQuarter COMMA? numYear era ;
strSeasonInYearRange:  strSeason HYPHEN strSeason COMMA? numYear era ;
numDayInMonthRange:    numMonth SLASH numDayOfMonth HYPHEN numDayOfMonth SLASH numYear era ;
numDate:               numMonth SLASH numDayOfMonth SLASH numYear era
|                      numMonth HYPHEN numDayOfMonth HYPHEN numYear era ;
monthYear:             strMonth COMMA? numYear era ;
invMonthYear:          era numYear COMMA? strMonth ;
seasonYear:            strSeason COMMA? numYear era ;
invSeasonYear:         era numYear COMMA? strSeason ;
nthQuarterYear:        nthQuarter numYear era ;
nthQuarter:            (nth | LAST) QUARTER ;
nthHalf:               (nth | LAST) HALF ;
numDecade:             TENS ;
strCentury:            nth CENTURY ;
numCentury:            HUNDREDS ;
nthCenturyRange:       allOrPartOf nth HYPHEN allOrPartOf nth CENTURY era ;
strSeason:             SPRING | SUMMER | FALL | WINTER ;
allOrPartOf:           partOf | ;
partOf:                EARLY | MIDDLE | LATE ; 
nth:                   NTHSTR | FIRST | SECOND | THIRD | FOURTH ;
strMonth:              MONTH | SHORTMONTH DOT? ;
era:                   BC | AD | ;
numYear:               NUMBER ;
numMonth:              NUMBER ;
numDayOfMonth:         NUMBER ;


/*
 * Lexer rules
 */

WS:             [ \t\r\n]+ -> skip;
CIRCA:          ('c' | 'ca') '.'? | 'circa' ;
SPRING:         'spring' | 'spr' ;
SUMMER:         'summer' | 'sum' ;
WINTER:         'winter' | 'win' ;
FALL:           'fall' | 'autumn' | 'fal' | 'aut' ;
EARLY:          'early' ;
MIDDLE:         'middle' | 'mid' ('-' | DOT)?;
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
MILLENNIUM:     'millennium' ;
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
QUESTION:       '?' ;
STRING:         [a-z]+ ;
OTHER:          . ;

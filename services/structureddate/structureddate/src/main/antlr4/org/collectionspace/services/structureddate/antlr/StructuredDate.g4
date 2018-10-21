grammar StructuredDate;

/*
 * This is a grammar for ANTLR 4 (http://www.antlr.org/).
 *
 */

/*
 * Parser rules
 */

oneDisplayDate:        displayDate ( DOT | QUESTION )? EOF ; 

displayDate:           uncertainDate
|                      certainDate
|                      beforeOrAfterDate
|                      unknownDate
|                      uncalibratedDate
;

uncertainDate:         CIRCA certainDate ;

certainDate:           hyphenatedRange
|                      singleInterval
;

beforeOrAfterDate:     ( BEFORE | AFTER ) singleInterval ;

uncalibratedDate:      numYear PLUSMINUS NUMBER YEARSSTRING? BP; 

hyphenatedRange:       singleInterval ( HYPHEN | DASH ) singleInterval
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
|                      dayFirstDate
|                      dayOrYearFirstDate
|                      invStrDateEraLastDate
;

month:                 monthYear
|                      invMonthYear
;

yearSpanningWinter:    WINTER COMMA? numYear SLASH numYear era? ;

partialYear:           partOf numYear era? ;

quarterYear:           seasonYear
|                      invSeasonYear
|                      nthQuarterYear
;

halfYear:              nthHalf numYear era? ;

year:                  numYear era? ;

partialDecade:         partOf numDecade era? ;

decade:                numDecade era? ;

partialCentury:        partOf ( strCentury | numCentury ) era? ;

quarterCentury:        nthQuarter ( strCentury | numCentury ) era? ;

halfCentury:           nthHalf ( strCentury | numCentury ) era? ;

century:               ( strCentury | numCentury ) era? ;

millennium:            nth MILLENNIUM era? ;

strDate:               strMonth ( numDayOfMonth | nth ) COMMA? numYear era?;
invStrDate:            era num COMMA? strMonth num
|                      era? num COMMA strMonth num ;
dayFirstDate:          num strMonth COMMA? num era
|                      num strMonth COMMA num era?
|                      nth strMonth COMMA? num era? ;
dayOrYearFirstDate:    num strMonth num ;
invStrDateEraLastDate: num COMMA strMonth num era? ;
strDayInMonthRange:    strMonth numDayOfMonth ( HYPHEN | DASH ) numDayOfMonth COMMA? numYear era? ;
monthInYearRange:      strMonth ( HYPHEN | DASH ) strMonth COMMA? numYear era? ;
nthQuarterInYearRange: nthQuarter ( HYPHEN | DASH ) nthQuarter COMMA? numYear era? ;
strSeasonInYearRange:  strSeason ( HYPHEN | DASH ) strSeason COMMA? numYear era? ;
numDayInMonthRange:    numMonth SLASH num ( HYPHEN | DASH ) num SLASH numYear era? ;
numDate:               num SLASH num SLASH num era?
|                      num HYPHEN num HYPHEN num era? ;
monthYear:             strMonth COMMA? numYear era? ;
invMonthYear:          era? numYear COMMA? strMonth ;
seasonYear:            strSeason COMMA? numYear era? ;
invSeasonYear:         era? numYear COMMA? strSeason ;
nthQuarterYear:        nthQuarter numYear era? ;
nthQuarter:            ( nth | LAST ) QUARTER ;
nthHalf:               ( nth | LAST ) HALF ;
numDecade:             TENS ;
strCentury:            nth CENTURY ;
numCentury:            HUNDREDS ;
nthCenturyRange:       allOrPartOf nth ( HYPHEN | DASH ) allOrPartOf nth CENTURY era? ;
strSeason:             SPRING | SUMMER | FALL | WINTER ;
allOrPartOf:           partOf | ;
partOf:                EARLY | MIDDLE | LATE ; 
nth:                   NTHSTR | FIRST | SECOND | THIRD | FOURTH ;
strMonth:              MONTH | SHORTMONTH DOT? ;
era:                   BC | AD ;
numYear:               NUMBER ;
numMonth:              NUMBER ;
numDayOfMonth:         NUMBER ;
num:                   NUMBER ;
unknownDate:           UNKNOWN ;


/*
 * Lexer rules
 */
PLUSMINUS:      '±' | '+/-' ;
WS:             [ \t\r\n]+ -> skip;
CIRCA:          ('c' | 'ca') DOT? | 'circa' ;
SPRING:         'spring' | 'spr' ;
SUMMER:         'summer' | 'sum' ;
WINTER:         'winter' | 'win' ;
FALL:           'fall' | 'autumn' | 'fal' | 'aut' ;
EARLY:          'early' ;
MIDDLE:         'middle' | 'mid' ( HYPHEN | DOT )?;
LATE:           'late' ;
BEFORE:         'before' | 'pre' HYPHEN? ;
AFTER:          'after' | 'post' HYPHEN? ;
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
BC:             'bc' | 'bce' |  'b.c.' | 'b.c.e.';
AD:             'ad' | 'a.d.' | 'ce' | 'c.e.';
BP:             'bp' | 'b.p.' | 'b.p' ;
NTHSTR:         [0-9]*? ([0456789] 'th' | '1st' | '2nd' | '3rd' | '11th' | '12th' | '13th') ;
HUNDREDS:       [0-9]*? '00' '\''? 's';
TENS:           [0-9]*? '0' '\''? 's';
NUMBER:         [0-9]+ ;
COMMA:          ',' ;
HYPHEN:         '-' ;
DASH:           [—–] ; /* EM DASH, EN DASH */
SLASH:          '/' ;
DOT:            '.' ;
QUESTION:       '?' ;
OTHER:          . ;
UNKNOWN:        'unknown';
YEARSSTRING:    'years' | 'year';
STRING:         [a-z]+ ;

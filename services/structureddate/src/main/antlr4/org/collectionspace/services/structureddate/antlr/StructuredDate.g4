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

oneDisplayDate:        displayDate EOF ; 

displayDate:           uncertainDate
|                      certainDate
;

uncertainDate:         CIRCA certainDate ;

certainDate:           hyphenatedRange
|                      singleInterval
;

hyphenatedRange:       quarterYear HYPHEN quarterYear
|                      halfYear HYPHEN halfYear
|                      year HYPHEN year
|                      month HYPHEN month
|                      date HYPHEN date
|                      monthInYearRange
|                      quarterInYearRange
|                      strDayInMonthRange
|                      numDayInMonthRange                                                            
/* TODO: */
/*
|                      nth CENTURY HYPHEN nth CENTURY                         
|                      nth HYPHEN nth CENTURY                                 
|                      nth HYPHEN nth CENTURY BC                              
|                      nth CENTURY BC HYPHEN nth CENTURY BC                   
|                      nth CENTURY BC HYPHEN nth CENTURY                      
|                      nth CENTURY BC HYPHEN nth CENTURY AD                   
|                      partOf nth CENTURY HYPHEN partOf nth CENTURY           
|                      partOf nth HYPHEN partOf nth CENTURY                   
|                      partOf nth HYPHEN nth CENTURY                          
|                      partOf nth CENTURY BC HYPHEN partOf nth CENTURY BC     
|                      partOf nth CENTURY BC HYPHEN partOf nth CENTURY        
|                      partOf century HYPHEN partOf century                   
|                      partOf century BC HYPHEN partOf century BC             
|                      nth QUARTER nth CENTURY HYPHEN nth QUARTER nth CENTURY 
|                      decade HYPHEN decade                                   
|                      decade HYPHEN partOf decade                            
|                      partOf decade HYPHEN partOf century                    
|                      partOf decade HYPHEN partOf decade                     
|                      partOf decade HYPHEN decade                            
*/
;

singleInterval:        quarterYear
|                      halfYear
|                      year
|                      month                                                  
|                      date                                                   
/* TODO: */
/*
|                      partOf year                                            
|                      partOf year BC                                         
|                      partOf date BC                                         
|                      partOf date                                            
|                      nth CENTURY                                            
|                      nth CENTURY AD                                         
|                      nth CENTURY BC                                         
|                      nth QUARTER nth CENTURY                                
|                      LAST QUARTER nth CENTURY                               
|                      nth HALF nth CENTURY                                   
|                      LAST HALF nth CENTURY                                  
|                      partOf nth CENTURY                                     
|                      partOf nth CENTURY BC                                  
|                      century                                                
|                      partOf century                                         
|                      partOf century BC                                      
|                      nth MILLENIUM                                          
|                      nth MILLENIUM BC                                       
|                      decade                                                 
|                      partOf decade                                          
|                      decade BC                                              
|                      partOf decade BC                                       
*/
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

quarterYear:           seasonYear
|                      invSeasonYear
|                      nthQuarterYear
;

halfYear:              nthHalfYear ;

year:                  numYear era ;

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
nthHalfYear:           nthHalf numYear era ;
nthQuarter:            (nth | LAST) QUARTER ;
nthHalf:               (nth | LAST) HALF ;
decade:                TENS ;
century:               HUNDREDS ;
strSeason:             SPRING | SUMMER | FALL | WINTER ;
partOf:                EARLY | MIDDLE | LATE | BEFORE | AFTER ;
nth:                   NTHSTR | FIRST | SECOND | THIRD | FOURTH ;
strMonth:              MONTH | SHORTMONTH DOT?;
era:                   BC | AD | ;
numYear:               NUMBER ;
numMonth:              NUMBER ;
numDayOfMonth:         NUMBER ;


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

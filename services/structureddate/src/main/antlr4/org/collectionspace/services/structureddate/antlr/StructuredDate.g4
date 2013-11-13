grammar StructuredDate;

/*
 * This is a grammar for ANTLR 4 (http://www.antlr.org/).
 *
 * To generate the lexer, parser, and listener classes, use the command: 
 *     java -jar /usr/local/lib/antlr-4.1-complete.jar -package edu.berkeley.cspace.structureddate.antlr StructuredDate.g4
 */

/*
 * Parser rules
 */
 
displayDate:    CIRCA year (BCE|AD)? # circaYear
|               year (BCE|AD)? # yearOnly
//|               nth QUARTER year
//|               LAST QUARTER year
//|               nth HALF year
//|               LAST HALF year
//|               year HYPHEN year
//|               CIRCA year HYPHEN year
//|               CIRCA year HYPHEN year BCE
//|               CIRCA year BCE HYPHEN year BCE
//|               CIRCA year BCE HYPHEN year
//|               CIRCA year BCE HYPHEN year AD
//|               year BCE HYPHEN year BCE
//|               year HYPHEN year BCE
//|               year BCE HYPHEN year
//|               year BCE HYPHEN year AD
//|               season year
//|               season HYPHEN season year
//|               season year HYPHEN season year
//|               season year BCE
//|               partOf year
//|               partOf year BCE
//|               month
//|               month HYPHEN month
//|               partOf singleDate BCE
//|               singleDate BCE
//|               partOf singleDate
|               singleDate # singleDateOnly
//|               singleDate HYPHEN singleDate
//|               nth CENTURY
//|               CIRCA nth CENTURY
//|               nth CENTURY AD
//|               nth CENTURY BCE
//|               CIRCA nth CENTURY BCE
//|               nth CENTURY HYPHEN nth CENTURY
//|               nth HYPHEN nth CENTURY
//|               nth HYPHEN nth CENTURY BCE
//|               nth CENTURY BCE HYPHEN nth CENTURY BCE
//|               nth CENTURY BCE HYPHEN nth CENTURY
//|               nth CENTURY BCE HYPHEN nth CENTURY AD
//|               nth QUARTER nth CENTURY
//|               nth QUARTER nth CENTURY HYPHEN nth QUARTER nth CENTURY
//|               LAST QUARTER nth CENTURY
//|               nth HALF nth CENTURY
//|               LAST HALF nth CENTURY
//|               partOf nth CENTURY
//|               partOf nth CENTURY BCE
//|               partOf nth CENTURY HYPHEN partOf nth CENTURY
//|               partOf nth HYPHEN partOf nth CENTURY
//|               partOf nth HYPHEN nth CENTURY
//|               partOf nth CENTURY BCE HYPHEN partOf nth CENTURY BCE
//|               partOf nth CENTURY BCE HYPHEN partOf nth CENTURY
//|               century
//|               partOf century
//|               partOf century HYPHEN partOf century
//|               partOf century BCE HYPHEN partOf century BCE
//|               partOf century BCE
//|               CIRCA century
//|               CIRCA century BCE
//|               nth MILLENIUM
//|               nth MILLENIUM BCE
//|               decade
//|               decade HYPHEN decade
//|               decade HYPHEN partOf decade
//|               partOf decade HYPHEN partOf century
//|               partOf decade HYPHEN partOf decade
//|               partOf decade HYPHEN decade
//|               partOf decade
//|               decade BCE
//|               partOf decade BCE
//|               CIRCA decade
//|               CIRCA decade BCE
|               dateRange # dateRangeOnly
;

dateRange:      monthOnlyRange
|               strDateRange
|               numDateRange
;              

singleDate:     numDate
|               strDate
|               invStrDate
;

month:          monthOnly ;

strDate:        strMonth dayOfMonth COMMA? year ;
invStrDate:     year COMMA? strMonth dayOfMonth ;
strDateRange:   strMonth dayOfMonth HYPHEN dayOfMonth COMMA? year ;
monthOnlyRange: strMonth HYPHEN strMonth COMMA? year ;
numDateRange:   numMonth SLASH dayOfMonth HYPHEN dayOfMonth SLASH year ;
numDate:        numMonth SLASH dayOfMonth SLASH year ;
monthOnly:      strMonth COMMA? year ;
decade:         TENS ;
century:        HUNDREDS ;
season:         SPRING | SUMMER | WINTER | FALL ;
partOf:         EARLY | MIDDLE | LATE | BEFORE | AFTER ;
nth:            NTHSTR | FIRST | SECOND | THIRD | FOURTH ;
strMonth:       MONTH ;
year:           NUMBER ;
numMonth:       NUMBER ;
dayOfMonth:     NUMBER ;


/*
 * Lexer rules
 */

WS:             [ \t\r\n]+ -> skip;
CIRCA:          [Cc] ('irca' | '.') ;
SPRING:         [Ss] 'pring' ;
SUMMER:         [Ss] 'ummer' ;
WINTER:         [Ww] 'inter' ;
FALL:           [Ff] 'all' | [Aa] 'utumn' ;
EARLY:          [Ee] 'arly' ;
MIDDLE:         [Mm] 'id' ;
LATE:           [Ll] 'ate' ;
BEFORE:         [Bb] 'efore' | [Pp] 're' ;
AFTER:          [Aa] 'fter' | [Pp] 'ost' ;
FIRST:          [Ff] 'irst' ;
SECOND:         [Ss] 'econd' ;
THIRD:          [Th] 'ird' ;
FOURTH:         [Ff] 'ourth' ;
LAST:           [Ll] 'ast' ;
QUARTER:        [Qq] 'uarter' ;
HALF:           [Hh] 'alf' ;
CENTURY:        [Cc] 'entury' ;
MILLENIUM:      [Mm] 'illenium' ;
MONTH:          [Jj] 'anuary' | [Ff] 'ebruary' | [Mm] 'arch' | [Aa] 'pril' | [Mm] 'ay' | [Jj] 'une' | [Jj] 'uly' | [Aa] 'ugust' | [Ss] 'eptember' | [Oo] 'ctober' | [Nn] 'ovember' | [Dd] 'ecember' |
                [Jj] 'an' | [Ff] 'eb' | [Mm] 'ar' | [Aa] 'pr' | [Jj] 'un' | [Jj] 'ul' | [Aa] 'ug' | [Ss] 'ep' | [Ss] 'ept' | [Oo] 'ct' | [Nn] 'ov' | [Dd] 'ec' ;
BCE:            [Bb][Cc][Ee]? | [Bb] '.' [Cc] '.' | [Bb] '.' [Cc] '.' [Ee] '.' ;
AD:             [Aa][Dd] | [Aa] '.' [Dd] '.' ;
STRING:         [a-zA-Z\.]+ ;
NTHSTR:         [0-9]*? ([0456789] 'th' | '1st' | '2nd' | '3rd' | '11th' | '12th' | '13th') ;
HUNDREDS:       [0-9]*? '00' S;
TENS:           [0-9]*? '0' S;
NUMBER:         [0-9]+ ;
COMMA:          ',' ;
HYPHEN:         '-' ;
SLASH:          '/' ;

fragment 
S:              '\''? 's' ;

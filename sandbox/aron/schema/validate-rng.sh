#!/bin/sh
#
# Simple bash shell script to validate an XML instance document
# against a RELAX NG schema.
#
# Requires two arguments:
# - Filename of the RELAX NG schema document.
#   (Or the full path to this document, if it is not in the current directory.)
# - Filename of the XML instance document.
#   (Or the full path to this document, if it is not in the current directory.)
#
# $Author: aron $
# $Revision: 57 $
# $Date: 2009-03-05 16:06:06 -0800 (Thu, 05 Mar 2009) $

java -jar tools/jing.jar $1 $2
#!/bin/sh
#
# rng-to-xsd.sh
# 
# Simple bash shell script to generate a W3C XML Schema (XSD) document
# from a RELAX NG schema document.
#
# Requires two arguments:
# - Filename of the RELAX NG schema document.
#   (or the full path to this document, if it is not in the current directory)
# - Filename of the new W3C XML Schema document to be created.
#   (or the full path to this document, if it is not in the current directory)
#
# $Author: aron $
# $Revision: 57 $
# $Date: 2009-03-05 16:06:06 -0800 (Thu, 05 Mar 2009) $

java -jar tools/trang.jar -I rng -O xsd $1 $2
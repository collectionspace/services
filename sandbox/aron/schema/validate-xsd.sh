#!/bin/sh
#
# Simple bash shell script to validate an XML instance document
# against a W3C XML Schema.
#
# Uses the XMLStarlet utility, http://xmlstar.sourceforge.net/
#
# Assumes the 'xmlstarlet' executable is located within the shell's executable path.
#
# Note that XMLStarlet's support for XML Schema is "not yet fully supported", due to
# incomplete support in a dependency, the XML C toolkit for Gnome (http://xmlsoft.org/).
#
# Requires two arguments:
# - Filename of the W3C XML Schema document.
#   (Or the full path to this document, if it is not in the current directory.)
# - Filename of the XML instance document.
#   (Or the full path to this document, if it is not in the current directory.)
#
# $Author: aron $
# $Revision: 57 $
# $Date: 2009-03-05 16:06:06 -0800 (Thu, 05 Mar 2009) $

xmlstarlet val --err --xsd $1 $2
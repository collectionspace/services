#!/bin/bash

PACKAGE_NAME=$1
BRANCH_NAME=$2
CHECKOUT_DIR=$3
OUTPUT_DIR=$4
LIBRARY_NAME=$5
SERVICE_LIBRARY_NAME=$6

UNORG_PACKAGE_NAME=`echo $PACKAGE_NAME | sed -e 's/^@.*\///'`
CODE_DIR=$CHECKOUT_DIR/$UNORG_PACKAGE_NAME.js

. "$HOME/.nvm/nvm.sh"

rm -r $CODE_DIR
git clone --branch $BRANCH_NAME --depth 1 https://github.com/collectionspace/$UNORG_PACKAGE_NAME.js.git $CODE_DIR

pushd $CODE_DIR
COMMIT_HASH=`git rev-parse --short HEAD`
npm install
popd

cp $CODE_DIR/dist/$LIBRARY_NAME.min.js "$OUTPUT_DIR/$LIBRARY_NAME@$COMMIT_HASH.min.js"

if [ ! -z "$SERVICE_LIBRARY_NAME" ]
then
  cp $CODE_DIR/dist/$SERVICE_LIBRARY_NAME.min.js "$OUTPUT_DIR/$SERVICE_LIBRARY_NAME@$COMMIT_HASH.min.js"
fi

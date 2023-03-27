#!/bin/bash

PACKAGE_NAME=$1
BRANCH_NAME=$2
CHECKOUT_DIR=$3
OUTPUT_DIR=$4
LIBRARY_NAME=$5

CODE_DIR=$CHECKOUT_DIR/$PACKAGE_NAME.js

. "$HOME/.nvm/nvm.sh"

rm -r $CODE_DIR
git clone --branch $BRANCH_NAME --depth 1 https://github.com/collectionspace/$PACKAGE_NAME.js.git $CODE_DIR

pushd $CODE_DIR
COMMIT_HASH=`git rev-parse --short HEAD`
npm install
popd

cp $CODE_DIR/dist/*.min.js "$OUTPUT_DIR/$LIBRARY_NAME@$COMMIT_HASH.min.js"

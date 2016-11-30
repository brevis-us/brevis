#!/bin/bash

if [[ $TRAVIS_BRANCH == 'master' ]]
then
    lein deploy
    echo "Deployed"
fi

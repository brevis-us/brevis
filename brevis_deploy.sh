#!/bin/bash

if [[ $TRAVIS_BRANCH == 'master' ]]

   lein deploy
   
fi

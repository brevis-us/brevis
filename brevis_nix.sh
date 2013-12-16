#/bin/sh

BASEDIR=$(dirname $0)
cd $BASEDIR

until lein run -m brevis.ui.core
do
  sleep 1
  echo "Restarting Brevis."
done

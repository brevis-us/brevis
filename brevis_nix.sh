#/bin/sh

until lein run -m brevis.ui.core
do
  sleep 1
  echo "Restarting Brevis."
done

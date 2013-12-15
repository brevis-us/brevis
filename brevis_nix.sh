#/bin/sh

until lein run -m brevis.ui.core
do
  echo "Restarting Brevis."
done

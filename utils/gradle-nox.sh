#!/usr/bin/env bash

CMD=$(basename $0)
CMD=${CMD%-nox.sh}

echo "Running '$CMD' headlessly"

xvfb-run -s "-screen 0 1280x800x16" -e /dev/stdout -a $CMD ${@}

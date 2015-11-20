#!/bin/sh

export PATH=`pwd`/../gradle-2.9/bin:$PATH

CMD=assembleDebug
if [ -n "$1" ]; then
	CMD="$1"
	shift
fi


gradle "$CMD" "$@"

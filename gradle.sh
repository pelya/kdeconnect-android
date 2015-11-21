#!/bin/sh

export PATH=`pwd`/../gradle-2.9/bin:$PATH
export GRADLE_OPTS=-Dorg.gradle.daemon=true

CMD=assembleDebug
if [ -n "$1" ]; then
	CMD="$1"
	shift
fi

gradle "$CMD" "$@"

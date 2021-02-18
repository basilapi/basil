#!/bin/bash


[ -z "$1" ] && exit 1

base="$1"
cp -rf $base/target/mvn-repo/* .
for f in $(ls $base); do
	tmp="$base/$f/target/mvn-repo/"
	[ -z "$tmp" ] || echo "Copying $tmp" && cp -rf $tmp* .
done

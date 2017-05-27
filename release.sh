#!/usr/bin/env sh

 git tag -a v$1 -m "Version $1"
 git push origin --tags
 git push github --tags
 lein deploy clojars
 lein install
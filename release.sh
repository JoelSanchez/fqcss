#!/usr/bin/env sh

 git tag -a v$1 -m "Version $1"
 git push origin --follow-tags
 git push github --follow-tags
 lein deploy clojars
 lein install

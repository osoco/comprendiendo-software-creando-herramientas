#!/usr/bin/env bash

export TAG="$(grep version build.gradle | head -n 1 | awk -F"'" '{print $2}')";

(
git checkout develop && \
git pull origin develop && \
git submodule update && \
./gradlew clean cloverGenerateReport && \
git checkout master && \
git pull origin master && \
git merge --no-ff --no-edit develop && \
git submodule update && \
./gradlew clean cloverGenerateReport && \
git push origin master && \
git tag ${TAG} -m "${TAG} release" && \
git push origin ${TAG} && \
git checkout develop && \
git merge --no-edit master && \
git push origin develop
) || echo "⚠️ ⚠️ ⚠️ Something was wrong ⚠️ ⚠️ ⚠️"
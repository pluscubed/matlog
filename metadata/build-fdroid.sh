#!/usr/bin/env bash

# https://github.com/nutritionfactsorg/daily-dozen-android/blob/master/metadata/build-fdroid.sh
# https://gitlab.com/fdroid/fdroiddata/blob/master/README.md
# https://gitlab.com/fdroid/fdroiddata/blob/master/CONTRIBUTING.md

set -ue

appid="com.pluscubed.matloglibre"

if [[ ! -d fdroidserver ]]; then
    echo "Cloning fdroidserver"
    git clone https://github.com/f-droid/fdroidserver.git
fi

if [[ ! -d fdroiddata ]]; then
    echo "Cloning fdroiddata"
    git clone https://github.com/f-droid/fdroiddata.git
fi

pushd fdroiddata

echo "Create an empty config file"
touch config.py

echo "Make sure fdroid works and reads the metadata files properly"
../fdroidserver/fdroid readmeta

echo "Cleaning up metadata file"
../fdroidserver/fdroid rewritemeta "$appid"

echo "Filling automated fields in metadata file (e.g. Auto Name and Current Version)"
../fdroidserver/fdroid checkupdates --auto --allow-dirty "$appid"

echo "Making sure that fdroid lint doesn't report any warnings. If it does, fix them."
../fdroidserver/fdroid lint "$appid"

echo "Testing build recipe"
../fdroidserver/fdroid build -v -l "$appid"

cp metadata/$appid.txt ../

popd
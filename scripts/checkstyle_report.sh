#! /usr/bin/env bash
#
# Copyright (C) 2019-2026 LitterBox contributors
#
# This file is part of LitterBox.
#
# LitterBox is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or (at
# your option) any later version.
#
# LitterBox is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
#


function read_dom() {
    local IFS=\>
    read -d \< ENTITY CONTENT
}

function map_severity() {
    local severity="$1"

    if [ "$severity" == "error" ]; then
        echo "critical"
    elif [ "$severity" == "warning" ]; then
        echo "major"
    elif [ "$severity" == "info" ]; then
        echo "minor"
    else
        echo "info"
    fi
}

function extract_value() {
    local input=$1
    local key=$2

    echo "$input" | sed -nE "s#.*$key=\"([^\"]+)\".*#\1#p"
}

workspace=$(pwd)
first=1
error_count=0

echo "["

while read_dom; do
    entity_type=$(echo "$ENTITY" | awk '{ print $1 }')
    if [[ "$entity_type" = "file" ]]; then
        current_file=$(echo "$ENTITY" | sed -nE "s#.*name=\"$workspace/([^\"]+)\".*#\1#p")
    fi

    if [[ "$entity_type" = "error" || "$entity_type" = "warning" || "$entity_type" = "info" ]]; then
        line=$(extract_value "$ENTITY" "line")
        severityCheckstyle=$(extract_value "$ENTITY" "severity")
        severity=$(map_severity "$severityCheckstyle")
        message=$(extract_value "$ENTITY" "message" | sed "s/&quot;/\\\\\"/g; s/&apos;/\\\\\"/g; s/&amp;/\&/g; s/&lt;/</g; s/&gt;/>/g")
        checksum=$(printf "%s %s %d" "$current_file" "$message" "$line" | sha1sum | awk '{ print $1 }')

        if [[ $first -ne 1 ]]; then
            echo ","
        fi

        printf '{ "description": "%s", "severity": "%s", "fingerprint": "%s", "location": { "path": "%s", "lines": { "begin": %d } } }' "$message" "$severity" "$checksum" "$current_file" "$line"

        first=0
        error_count=$((error_count + 1))
    fi
done

echo
echo "]"

exit 0

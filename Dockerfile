#
# Copyright (C) 2019-2022 LitterBox contributors
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

# Container image for building the project
FROM docker.io/library/maven:3-eclipse-temurin-17 AS build
LABEL maintainer="Sebastian Schweikl"

# Parameter for skipping the tests in the build process
ARG SKIP_TESTS=true

WORKDIR /build

# Copy files and directories needed for building
COPY pom.xml ./
COPY src ./src

# Build the project
# The -e flag is to show errors and -B to run in non-interactive aka “batch” mode
# Lastly, make build-artifact naming version-independent
RUN mvn -e -B package -DskipTests=${SKIP_TESTS} && \
    mkdir -p /build/bin && \
    mv target/Litterbox-*-SNAPSHOT.full.jar bin/Litterbox.jar

# Slim container image for running LitterBox
FROM docker.io/library/eclipse-temurin:17-jre

WORKDIR /litterbox
VOLUME /litterbox

# Copy the LitterBox jar from the builder to this container
COPY --from=build /build/bin /litterbox-bin

# The executable is Litterbox
ENTRYPOINT ["java", "-jar", "/litterbox-bin/Litterbox.jar"]

# The default argument is the help menu
# This can be overidden on the command line
CMD ["--help"]


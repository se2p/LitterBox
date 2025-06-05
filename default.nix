{
  lib,
  jre,
  makeWrapper,
  maven,
}: let
  embeddedKittensVersion = "1.1.0-SNAPSHOT";
in
  maven.buildMavenPackage rec {
    pname = "embedded-kittens";
    version = embeddedKittensVersion;

    src = ./.;

    mvnHash = "sha256-+KmtD7s9AfHN155VT6wYs3vzyWATmeci4lwcXjsHpFo=";

    nativeBuildInputs = [makeWrapper];

    installPhase = ''
      mkdir -p $out/share/java
      install -Dm644 target/embedded-kittens-${version}.full.jar $out/share/java/embedded-kittens-${version}.jar

      makeWrapper ${jre}/bin/java $out/bin/embedded-kittens \
        --add-flags "-jar $out/share/java/embedded-kittens-${version}.jar"
    '';

    meta = with lib; {
      licence = [licenses.gpl3Plus];
    };
  }

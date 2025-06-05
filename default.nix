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

    mvnHash = "sha256-D/wra1jSi4PmS/DE4/pN+XPV7cyijYM7p0H094NLBEU=";

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

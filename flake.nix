{
  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
    systems.url = "github:nix-systems/default";
    devenv.url = "github:cachix/devenv";
    devenv.inputs.nixpkgs.follows = "nixpkgs";
  };

  nixConfig = {
    extra-trusted-public-keys = "devenv.cachix.org-1:w1cLUi8dv3hnoSPGAuibQv+f9TZLr6cv/Hm9XgU50cw=";
    extra-substituters = "https://devenv.cachix.org";
  };

  outputs = {
    self,
    nixpkgs,
    devenv,
    systems,
    ...
  } @ inputs: let
    forEachSystem = nixpkgs.lib.genAttrs (import systems);
  in {
    packages = forEachSystem (
      system: let
        pkgs = nixpkgs.legacyPackages.${system};
      in {
        devenv-up = self.devShells.${system}.default.config.procfileScript;
        default = pkgs.callPackage ./default.nix {};
      }
    );

    devShells =
      forEachSystem
      (system: let
        pkgs = nixpkgs.legacyPackages.${system};
      in {
        default = devenv.lib.mkShell {
          inherit inputs pkgs;
          modules = [
            {
              languages.java = {
                enable = true;
                jdk.package = pkgs.jdk21;
                maven.enable = true;
              };

              pre-commit.hooks = {
                alejandra.enable = true;
                mvn-licence = {
                  enable = true;
                  name = "mvn-licence";
                  description = "Run maven license header checker.";
                  entry = "${pkgs.maven}/bin/mvn license:check";
                  files = "^src/.*";
                  pass_filenames = false;
                };
                spotless-fmt = {
                  enable = true;
                  name = "spotless-fmt";
                  description = "Check for correct Java source code formatting.";
                  entry = "${pkgs.maven}/bin/mvn spotless:check";
                  files = ".*\\.java$";
                  pass_filenames = false;
                };
                mvn-checkstyle = {
                  enable = true;
                  name = "mvn-checkstyle";
                  description = "Run Checkstyle checker.";
                  entry = "${pkgs.maven}/bin/mvn checkstyle:check";
                  files = ".*\\.java$";
                  pass_filenames = false;
                };
              };
            }
          ];
        };
      });
  };
}

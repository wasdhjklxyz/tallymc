{
  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs?ref=nixos-unstable";
    systems.url = "github:nix-systems/default";
  };

  outputs = { self, nixpkgs, systems }:
    let
      eachSystem = nixpkgs.lib.genAttrs (import systems);
    in {
      devShells = eachSystem (system:
        let
          pkgs = nixpkgs.legacyPackages.${system};
          jdk = pkgs.temurin-bin-25;
        in {
          default = pkgs.mkShell {
            packages = [
              jdk
              pkgs.git
              pkgs.just
              pkgs.gradle
            ];

            env = {
              JAVA_HOME = "${jdk}";
              GRADLE_OPTS = "-Dorg.gradle.java.home=${jdk} -Dorg.gradle.java.installations.auto-download=false";
            };

            shellHook = ''
              echo "$(java -version 2>&1 | head -1)"
              echo "$(gradle --version | grep '^Gradle')"
            '';
          };
        });
    };
}

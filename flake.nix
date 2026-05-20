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

          paperVersion = "26.1.2-64";
          paperJar = pkgs.fetchurl {
            url = "https://fill-data.papermc.io/v1/objects/830d4eb5c15cbd802a9ec9f2f54eaaaeb9511958339aec983fd0c88bad21d940/paper-${paperVersion}.jar";
            hash = "sha256-gw1OtcFcvYAqnsny9U6qrrlRGVgzmuyYP9DIi60h2UA=";
          };
        in {
          default = pkgs.mkShell {
            packages = [
              jdk
              pkgs.git
              pkgs.gnumake
              pkgs.gradle_9
              pkgs.jdt-language-server
            ];

            env = {
              JAVA_HOME = "${jdk}";
              GRADLE_OPTS = "-Dorg.gradle.java.home=${jdk} -Dorg.gradle.java.installations.auto-download=false";
              PAPER_JAR = "${paperJar}";
              SERVER_PORT = 12345;
              SERVER_MEM = "2G";
            };

            shellHook = ''
              echo "$(java -version 2>&1)"
              echo "$(gradle --version | grep '^Gradle')"
              echo "Paper ${paperVersion}"
            '';
          };
        });
    };
}

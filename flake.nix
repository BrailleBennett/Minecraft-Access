{
  description = "Development environment for Minecraft Access";

  inputs = {
    # Using unstable for newer/edge packages like jdk-25
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
  };

  outputs = { self, nixpkgs }:
    let
      supportedSystems = [ "x86_64-linux" "aarch64-linux" "x86_64-darwin" "aarch64-darwin" ];

      # Helper function to generate attributes for each system
      forAllSystems = nixpkgs.lib.genAttrs supportedSystems;
    in
    {
      devShells = forAllSystems (system:
        let
          pkgs = import nixpkgs { inherit system; };
        in
        {
          default = pkgs.mkShell rec {
            buildInputs = with pkgs; [
              javaPackages.compiler.temurin-bin.jdk-25
              libpulseaudio
              libGL
              openal
              speechd
              libx11 # try xorg.libX11 if resolution fails
            ];

            LD_LIBRARY_PATH = pkgs.lib.makeLibraryPath buildInputs;
            JAVA_HOME = pkgs.javaPackages.compiler.temurin-bin.jdk-25;

            nativeBuildInputs = with pkgs.buildPackages; [
              jetbrains.jdk
              git
              hugo
              wlc
            ];
          };
        }
      );
    };
}

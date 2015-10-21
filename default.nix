args@{ pkgs ? (import <nixpkgs> {}).pkgs
# Keep in sync with parsets-playground.nix
# is there a better way to do this?
, pidFile ? "/dev/null"
, devMode ? false
, extraConfig ? ""
}:
pkgs.callPackage ./parsets-playground.nix ({
    parsets = pkgs.haskell.packages.ghc784.callPackage ./parsets.nix {};
} // args)

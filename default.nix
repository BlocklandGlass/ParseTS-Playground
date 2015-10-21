args@{ nixpkgs ? import <nixpkgs> {}
# Keep in sync with parsets-playground.nix
# is there a better way to do this?
, pidFile ? "/dev/null"
, devMode ? false
, extraConfig ? ""
}:
nixpkgs.pkgs.callPackage ./parsets-playground.nix ({
    parsets = nixpkgs.pkgs.haskell.packages.ghc784.callPackage ./parsets.nix {};
} // args)

{distribution, system, pkgs}:

let
  customPkgs = rec {
    parsets-playground = import ../default.nix { inherit pkgs; };
    parsets-playground-wrapper = pkgs.callPackage ./parsets-playground-wrapper.nix {
      inherit parsets-playground;
    };
    parsets-playground-db = pkgs.callPackage ./parsets-playground-db.nix {};
  };
in rec {
  parsets-playground-db = {
    name = "parsets-playground-db";
    pkg = customPkgs.parsets-playground-db;
    dependsOn = {};
    type = "postgresql-database";
  };
  parsets-playground = {
    name = "parsets-playground";
    pkg = customPkgs.parsets-playground-wrapper;
    dependsOn = {
      inherit parsets-playground-db;
    };
    type = "process";
  };
}
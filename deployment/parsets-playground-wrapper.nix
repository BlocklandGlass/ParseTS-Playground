{stdenv, parsets-playground}:
{parsets-playground-db}:

let
  db = parsets-playground-db;
in parsets-playground.override {
  extraConfig =
    ''
      play.evolutions.db.default.autoApply = true
      #1
      slick.dbs.default.db.url = "jdbc:postgresql://localhost:${db.target.postgresqlPort}/${db.name}"
    '';
}
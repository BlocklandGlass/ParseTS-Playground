{
  main = {config, pkgs, ...}:
    let
      wrapper = pkgs.callPackage ./parsets-playground-wrapper.nix {
        parsets-playground = import ../default.nix { inherit pkgs; };
        port = 9000;
        postgresql = config.services.postgresql.package;
        postgresqlPort = config.services.postgresql.port;
      };
    in
    {
      systemd.services = {
        inherit (wrapper.systemd.services) parsets-playground-db parsets-playground;
      };
      firewall = {
        inherit (wrapper) firewall;
      };
    };
}

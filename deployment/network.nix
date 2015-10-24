let
  baseline = {pkgs, nixpkgs, config, ...}: {
    services = {
      openssh.enable = true;
      oidentd.enable = true;
      postgresql = {
        enable = true;
        enableTCPIP = true;
        package = pkgs.postgresql94;
        authentication =
          ''
            local all all peer
            host all all 127.0.0.1/32 ident
            host all all ::1/128 ident
          '';
      };
      disnix = {
        enable = true;
        infrastructure = {
          postgresqlPort = toString config.services.postgresql.port;
          postgresqlUsername = "root";
        };
      };

      # Seems to have a habit of running out of memory
      # TODO: replace?
      ntp.enable = false;
    };

    # FIXME: Set up firewall whitelists
    networking.firewall.enable = false;

    nixpkgs.config.packageOverrides = pkgs: {
      dysnomia = pkgs.dysnomia.override {
        enablePostgreSQLDatabase = true;
      };
    };
  };
in {
  main = baseline;
}

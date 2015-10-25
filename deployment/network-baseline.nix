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
      postgresqlBackup.enable = true;

      # Seems to have a habit of running out of memory
      # TODO: replace?
      ntp.enable = false;
    };

    nixpkgs.config.allowUnfree = true;
  };
in {
  main = baseline;
}

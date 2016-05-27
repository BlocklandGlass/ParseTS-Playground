{ stdenv, writeText, writeScript, makeWrapper
, bash
, parsets-playground
, port
, lib
, useRecaptcha ? false
, recaptchaSiteKey ? null
, recaptchaSecretKey ? null
, postgresql
, postgresqlPort
, postgresqlDatabase ? "parsets-playground"
, reverseProxyIp ? null
}:

assert useRecaptcha -> (recaptchaSiteKey != null && recaptchaSecretKey != null);

let
  configFile = writeText "parsets-playground.conf"
    ''
      include "application.conf"
      play.evolutions.db.default.autoApply = true
      slick.dbs.default.db.url = "jdbc:postgresql://localhost:${toString postgresqlPort}/${postgresqlDatabase}"
      ${lib.optionalString (reverseProxyIp != null) "play.http.forwarded.trustedProxies += ['${reverseProxyIp}']"}
      ${lib.optionalString useRecaptcha
        ''
          captcha {
            provider = "recaptcha"
            recaptcha {
              siteKey = ${recaptchaSiteKey}
              secretKey = ${recaptchaSecretKey}
            }
          }
        ''
      }
    '';
  initDbScript = writeScript "parsets-playground-initdb"
    ''
      #!${bash}/bin/bash
      if ! ${postgresql}/bin/psql "${postgresqlDatabase}" -c ""; then
        ${postgresql}/bin/createdb "${postgresqlDatabase}"
      fi
    '';
in {
  systemd.services = {
    parsets-playground-db = {
      requires = [ "postgresql.service" ];
      after = [ "postgresql.service" ];
      partOf = [ "parsets-playground.service" ];
      script = "exec ${initDbScript}";
    };
    parsets-playground = {
      wantedBy = [ "multi-user.target" ];
      requires = [ "postgresql.service" "parsets-playground-db.service" ];
      after = [ "network.target" "postgresql.service" "parsets-playground-db.service" ];
      script = "exec ${parsets-playground}/bin/parsets-playground -Dconfig.file=${configFile} -Dhttp.port=${toString port}";
    };
  };
}

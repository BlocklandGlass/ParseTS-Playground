{ stdenv, pkgs, jre, jdk, sbt, parsets, makeWrapper, writeText
, pidFile ? "/dev/null"
, devMode ? false
, extraConfig ? ""
}:

assert (builtins.isBool devMode);

let
  generatedConfig = writeText "parsets-playground.conf" ''
    ${stdenv.lib.optionalString devMode "include \"local.dev.conf\""}
    parsets.bin = "${parsets}/bin/parsets"
    ${extraConfig}
  '';
in stdenv.mkDerivation rec {
  name = "parsets-playground-${version}";
  version = "0.0.1-alpha1";
  src = ./.;

  buildInputs = [ jre jdk sbt parsets makeWrapper ];

  configurePhase =
    ''
      if [ -e conf/local.conf ]; then
        mv conf/local.conf conf/local.dev.conf
      fi

      ln -s ${generatedConfig} conf/local.conf
      sbt playUpdateSecret
    '';
  buildPhase = "sbt stage";
  installPhase =
    ''
      cp -r target/universal/stage $out
      wrapProgram $out/bin/parsets-playground \
        --set JAVA_HOME ${jre} \
        --add-flags \
          -Dpidfile.path=${pidFile}

      mkdir -p $out/etc
      cat >$out/etc/process_config <<EOF
      container_process=$out/bin/parsets-playground
      EOF
    '';
  dontStrip = true;
  preFixup = "rm -rf $out/share/doc";
}

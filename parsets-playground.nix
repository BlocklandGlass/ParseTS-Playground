{ stdenv, jre, jdk, sbt, parsets, makeWrapper, writeText
, pidFile ? "/dev/null"
, preserveLocalConfig ? false
, extraConfig ? ""
}:

assert (builtins.isBool preserveLocalConfig);
assert preserveLocalConfig;

let
  generatedConfig = writeText "parsets-playground.conf" ''
    ${stdenv.lib.optionalString preserveLocalConfig "include \"local.dev.conf\""}
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
    '';
  dontStrip = true;
  preFixup = "rm -rf $out/share/doc";
}

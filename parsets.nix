{ mkDerivation, aeson, base, blaze-html, bytestring, directory
, fetchgit, filepath, mtl, parsec, stdenv, text
, unordered-containers
}:
mkDerivation {
  pname = "parsets";
  version = "0.1.0.0";
  src = fetchgit {
    url = "git://github.com/blocklandglass/parsets";
    sha256 = "f2d1b52328885a30528ece02ce0872fb16affc5d3846c4e49f43c2e5f4d0229d";
    rev = "aa1dff2448555535b9bab703209aa13d077f0aee";
  };
  isLibrary = false;
  isExecutable = true;
  executableHaskellDepends = [
    aeson base blaze-html bytestring directory filepath mtl parsec text
    unordered-containers
  ];
  license = stdenv.lib.licenses.unfree;
}

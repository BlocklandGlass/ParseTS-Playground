{ mkDerivation, aeson, base, blaze-html, bytestring, directory
, fetchgit, filepath, mtl, parsec, stdenv, text
, unordered-containers
}:
mkDerivation {
  pname = "parsets";
  version = "0.1.0.0";
  src = fetchgit {
    url = "git://github.com/blocklandglass/parsets";
    sha256 = "1xsq2yxvspdrhs1cydxd3zh25rlwqwshnhv3i49f6jix0ns2xn3a";
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

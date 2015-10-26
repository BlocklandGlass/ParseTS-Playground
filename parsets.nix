{ mkDerivation, aeson, base, blaze-html, bytestring, directory
, fetchgit, filepath, mtl, parsec, stdenv, text
, unordered-containers
}:
mkDerivation {
  pname = "parsets";
  version = "0.1.0.0";
  src = fetchgit {
    url = "git://github.com/blocklandglass/parsets";
    sha256 = "cd9495c32292c83a6503413193f16fd1f4ec1555a6bd02413ea51b60e1de7c87";
    rev = "3a3c1eaa6d1aa3c276792006d374e6a0b48957ff";
  };
  isLibrary = false;
  isExecutable = true;
  executableHaskellDepends = [
    aeson base blaze-html bytestring directory filepath mtl parsec text
    unordered-containers
  ];
  license = stdenv.lib.licenses.unfree;
}

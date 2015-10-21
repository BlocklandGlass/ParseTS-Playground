{ mkDerivation, aeson, base, blaze-html, bytestring, directory
, fetchgit, filepath, mtl, parsec, stdenv, text
, unordered-containers
}:
mkDerivation {
  pname = "parsets";
  version = "0.1.0.0";
  src = fetchgit {
    url = "git://github.com/blocklandglass/parsets";
    sha256 = "6261538360618cebc431f3ea5cab16fbe43ab0a0ba166c1811491a6c18532607";
    rev = "83684b7e22f5d1521bbcd7280d95abe18a5fcc0a";
  };
  isLibrary = false;
  isExecutable = true;
  executableHaskellDepends = [
    aeson base blaze-html bytestring directory filepath mtl parsec text
    unordered-containers
  ];
  license = stdenv.lib.licenses.unfree;
}

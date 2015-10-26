{ mkDerivation, aeson, base, blaze-html, bytestring, directory
, fetchgit, filepath, mtl, parsec, stdenv, text
, unordered-containers
}:
mkDerivation {
  pname = "parsets";
  version = "0.1.0.0";
  src = fetchgit {
    url = "git://github.com/blocklandglass/parsets";
    sha256 = "a935c17b07b3127dfae5d0c5c3befa93b8bcb35257a77b344cb7031779369fd4";
    rev = "277a62fd552533dcff2237e15898ba2f80318e65";
  };
  isLibrary = false;
  isExecutable = true;
  executableHaskellDepends = [
    aeson base blaze-html bytestring directory filepath mtl parsec text
    unordered-containers
  ];
  license = stdenv.lib.licenses.unfree;
}

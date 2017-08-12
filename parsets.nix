{ fetchgit, haskell, stack, perl, gmp }:
haskell.lib.buildStackProject rec {
  name = "parsets-${version}";
  version = "0.1.0.0";
  ghc = haskell.compiler.ghc784;
  src = fetchgit {
    url = "git://github.com/blocklandglass/parsets";
    sha256 = "1j4ncz7jhvdv8fg2d9sjln9l7xvf68rg1jzjc7c42cig2jaqbl1b";
    rev = "735de344b7502d573d837b6be6622bbd095f1072";
  };
  buildInputs = [ stack perl gmp ];
  configurePhase =
    ''
      export STACK_ROOT=$NIX_BUILD_TOP/.stack
      stack setup
    '';
}

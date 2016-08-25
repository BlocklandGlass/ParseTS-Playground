{ fetchgit, haskell, stack }:
haskell.lib.buildStackProject rec {
  name = "parsets-${version}";
  version = "0.1.0.0";
  ghc = haskell.compiler.ghc784;
  src = fetchgit {
    url = "git://github.com/blocklandglass/parsets";
    sha256 = "1xsq2yxvspdrhs1cydxd3zh25rlwqwshnhv3i49f6jix0ns2xn3a";
    rev = "aa1dff2448555535b9bab703209aa13d077f0aee";
  };
  buildInputs = [ stack ];
  configurePhase =
    ''
      export STACK_ROOT=$NIX_BUILD_TOP/.stack
      stack setup
    '';
}

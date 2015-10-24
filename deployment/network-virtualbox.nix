let
  baseline = {
    deployment.targetEnv = "virtualbox";
    deployment.virtualbox.memorySize = 1024;
  };
in {
  main = baseline;
}
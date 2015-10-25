ParseTS Playground
==================

Quick start (\*NIX only)
------------------------

The easiest option to try out the playground is to run it in a VM through NixOps.
This only requires that you have VirtualBox installed on a \*NIX system.

    # Install the Nix package manager
    $ curl https://nixos.org/nix/install | sh
    # Install NixOps
    $ nix-env -i nixops
    # Initialize deployment config
    $ nixops create -d parsets-playground deployment/network.nix deployment/network-baseline.nix deployment/network-virtualbox.nix
    # Build and start the VM
    $ nixops deploy -d parsets-playground
    # Open the playground in your browser!
    $ xdg-open http://$(nixops info --plain | tr -s \t | cut -f 5):9000/ || open http://$(nixops info --plain | tr -s \t | cut -f 5):9000/
    

Manual startup (for development and Windows)
--------------------------------------------

In order to run the playground you need three things:

* A working JDK (8 or newer)
* PostgreSQL
* ParseTS on your path

After those things are set up you can compile and run the playground:

    $ ./activator run

You can set the option `parsets.bin` in `conf/local.conf` to the location of your ParseTS binary
if you don't want it on your path. Alternately, use the Nix definition in `default.nix` to have
both built automatically.

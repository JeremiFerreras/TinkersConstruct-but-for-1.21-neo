# NeoForge 1.21.1 port

## Required library

This branch uses **Neo Mantle** as its 1.21.1 compatibility library. The regular Mantle 1.20.1/Forge artifact does not expose the APIs used by this port.

The dependency is pinned as the `neomantle` Git submodule. Initialize it after cloning:

```bash
git submodule update --init --recursive
```

Then build with Java 21:

```bash
./gradlew build
```

The pinned submodule revision is from [vancevoj/neomantle](https://github.com/vancevoj/neomantle), branch `1.21.1-main`. Neo Mantle is an unofficial NeoForge port of Mantle, is not endorsed by SlimeKnights, and retains the MIT license and copyright notices in its own repository.

This port remains a separate work branch. The original `1.20.1` branch is not modified.

[![img](https://img.shields.io/badge/Licence-EPL-orange.svg?style=flat-square)](https://git.sr.ht/~etalab/codegouvfr-consolidate-data/blob/master/LICENSE)

# Consolidate and generate codegouvfr data

`codegouvfr` stands for [code.gouv.fr](https://code.gouv.fr).  This
repository helps creating `json/svg/xml` files used by the
[code.gouv.fr](https://git.sr.ht/~etalab/code.gouv.fr) web
application.

# Installation and configuration

1. Install a Java runtime for Java version 8 or 11 if needed. You can
   check the existing version of your java runtime with `java
   -version`. [OpenJDK](https://openjdk.java.net/install/) 11 can be
   installed on Debian-compatible Linux systems with `apt install
   openjdk-11-jdk`.
   
2. Install `node.js` if needed.

3. Install clojure: [follow installation instructions on
   clojure.org](https://clojure.org/guides/getting_started).
   
4. Install rlwrap. For example on Debian-compatible Linux systems with
   `apt install rlwrap`.
   
5. Install vl2svg with the following command `npm install -g
   vega-lite`.
   
6. Clone this repository: `git clone
   https://git.sr.ht/~etalab/codegouvfr-consolidate-data ; cd
   codegouvfr-consolidate-data`

# Generate consolidated files

1. You may use input files.  These files are in the data folder of
   [codegouvfr-fetch-data](https://git.sr.ht/~etalab/codegouvfr-fetch-data).
   Copy them in the project directory.  For example `cp -r
   ../codegouvfr-fetch-data/* .`  If you skip this step, the input
   files will be fetched from [code.gouv.fr](https://code.gouv.fr).
   
2. Launch the command `clj -M:run`, the following files will be made
   available in the project directory:

- deps-orgas.json
- deps-repos-sim.json
- deps-repos.json
- deps-top.json
- deps-total.json
- deps.json
- orgas.json
- repos-deps.json
- repos.json
- reuses.json
- latest.xml

# Get the data

The data are published under the [Open License
2.0](https://www.etalab.gouv.fr/licence-ouverte-open-licence):

-   Organizations: as [csv](https://code.gouv.fr/data/organizations/csv/all.csv) or [json](https://code.gouv.fr/data/organizations/json/all.json)
-   Repositories: as [csv](https://code.gouv.fr/data/repositories/csv/all.csv) or [json](https://code.gouv.fr/data/repositories/json/all.json)
-   Dependencies: [json](https://code.gouv.fr/data/deps.json)

# Contributing

The development of this repository happens on [the SourceHut
repository](https://git.sr.ht/~etalab/codegouvfr-consolidate-data).  

The code is also published on
[GitHub](https://github.com/etalab/codegouvfr-data) to reach more
developers, but please do not send pull requests there.

You can send **patches** by email using
[git-send-email.io](https://git-send-email.io/).  For your patches to
be processed correctly, configure your local copy with this:

    git config format.subjectPrefix 'PATCH codegouvfr-consolidate-data'

You can also contribute with bug reports, feature requests or general
questions by writing to
[~etalab/codegouvfr-devel@lists.sr.ht](mailto:~etalab/codegouvfr-devel@lists.sr.ht).

# License

2020-2021 DINUM, Bastien Guerry.

This application is published under the [EPL 2.0
license](https://git.sr.ht/~etalab/codegouvfr-consolidate-data/blob/master/LICENSE).


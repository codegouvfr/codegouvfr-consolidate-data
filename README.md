[![img](https://img.shields.io/badge/Licence-EPL-orange.svg?style=flat-square)](https://git.sr.ht/~etalab/codegouvfr-consolidate-data/blob/master/LICENSE)

# Consolidate and generate codegouvfr data

`codegouvfr` stands for [code.gouv.fr](https://code.gouv.fr).

The code in this repository creates `json/svg/xml` files used by the [code.gouv.fr](https://git.sr.ht/~etalab/code.gouv.fr) web application.

# Installation and configuration

1. Install a Java runtime for Java version 8 or 11. You can check the existing version of your java runtime with `java -version`. [OpenJDK](https://openjdk.java.net/install/) 11 can be installed on Debian-compatible GNU/Linux systems with `apt install openjdk-11-jdk`.
   
2. Install `node.js` and `vl2svg` (`npm install -g vega-lite`).

3. Install Clojure: [follow installation instructions on clojure.org](https://clojure.org/guides/getting_started).
   
4. Install `rlwrap`. For example on Debian-compatible GNU/Linux systems with `apt install rlwrap`.
   
5. Clone this repository and enter it: `git clone https://git.sr.ht/~etalab/codegouvfr-consolidate-data ; cd codegouvfr-consolidate-data`

# Generate consolidated files

1. You may use input files.  These files are in the data folder of [codegouvfr-fetch-data](https://git.sr.ht/~etalab/codegouvfr-fetch-data).  Copy them in the project directory.  For example `cp -r ../codegouvfr-fetch-data/* .`  If you skip this step, the input files will be fetched from [code.gouv.fr](https://code.gouv.fr).
   
2. Launch the command `clj -M:run`.

# Get the data

These data are published under the [Open License 2.0](https://www.etalab.gouv.fr/licence-ouverte-open-licence):

-   Organizations: as [csv](https://code.gouv.fr/data/organizations/csv/all.csv) or [json](https://code.gouv.fr/data/organizations/json/all.json)
-   Repositories: as [csv](https://code.gouv.fr/data/repositories/csv/all.csv) or [json](https://code.gouv.fr/data/repositories/json/all.json)
-   Dependencies: [json](https://code.gouv.fr/data/deps.json)
-   SILL: [json](https://code.gouv.fr/data/sill.json)

Data for the [sill.etalab.gouv.fr](https://sill.etalab.gouv.fr), exposed on [this page](https://code.gouv.fr/#/sill), come from Wikidata (CC0), Comptoir du libre ([CC0](https://gitlab.adullact.net/Comptoir/Comptoir-srv/-/issues/968)), https://annuaire.cnll.fr and contributors of the SILL ([Open License 2.0](https://www.etalab.gouv.fr/licence-ouverte-open-licence)).

# [Contributing](CONTRIBUTING.md)

# License

2020-2023 DINUM, Bastien Guerry.

This application is published under the [EPL 2.0
license](https://git.sr.ht/~etalab/codegouvfr-consolidate-data/blob/master/LICENSE).

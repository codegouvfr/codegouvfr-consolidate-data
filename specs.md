
# Table of Contents

1.  [Contexte](#org34a62e1)
2.  [Tableau](#orgdc32192)


<a id="org34a62e1"></a>

# Contexte

On va reconstruire <https://code.gouv.fr/data/repos.json> à partir des
données de <https://data.code.gouv.fr> plutôt qu'à partir des scripts
actuels (codegouvfr-fetch-data et codegouvfr-consolidate-data).

Pour les champs actuels de repos.json, comment récupérer les données de
l'API de data.code.gouv.fr ?

Quels champs supprimer ?

Quels champs ajouter ?


<a id="orgdc32192"></a>

# Tableau

<table border="2" cellspacing="0" cellpadding="6" rules="groups" frame="hsides">


<colgroup>
<col  class="org-left" />

<col  class="org-left" />

<col  class="org-left" />

<col  class="org-left" />
</colgroup>
<thead>
<tr>
<th scope="col" class="org-left">champ long</th>
<th scope="col" class="org-left">champ court</th>
<th scope="col" class="org-left">nouveau champ</th>
<th scope="col" class="org-left">url</th>
</tr>
</thead>
<tbody>
<tr>
<td class="org-left">repos</td>
<td class="org-left">&#xa0;</td>
<td class="org-left">&#xa0;</td>
<td class="org-left">&#xa0;</td>
</tr>
</tbody>
<tbody>
<tr>
<td class="org-left">id</td>
<td class="org-left">?</td>
<td class="org-left">?</td>
<td class="org-left">?</td>
</tr>

<tr>
<td class="org-left">name</td>
<td class="org-left">n</td>
<td class="org-left">full<sub>name</sub></td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">platform</td>
<td class="org-left">p</td>
<td class="org-left">"host" : { "name" : "string" }</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">repository<sub>url</sub></td>
<td class="org-left">r</td>
<td class="org-left">repository<sub>url</sub></td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">description</td>
<td class="org-left">d</td>
<td class="org-left">description</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">default<sub>branch</sub></td>
<td class="org-left">nan</td>
<td class="org-left">default<sub>branch</sub></td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">is<sub>fork</sub></td>
<td class="org-left">f?</td>
<td class="org-left">fork</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">is<sub>archived</sub></td>
<td class="org-left">a?</td>
<td class="org-left">archived</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">creation<sub>date</sub></td>
<td class="org-left">nan</td>
<td class="org-left">created<sub>at</sub></td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">last<sub>update</sub></td>
<td class="org-left">u</td>
<td class="org-left">update<sub>at</sub> ?</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">last modification</td>
<td class="org-left">nan</td>
<td class="org-left">pushed<sub>at</sub> ?</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">homepage</td>
<td class="org-left">nan</td>
<td class="org-left">homepage</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">stars<sub>count</sub></td>
<td class="org-left">s</td>
<td class="org-left">subscribers<sub>count</sub> ?</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">licence</td>
<td class="org-left">li</td>
<td class="org-left">license</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">open<sub>issues</sub><sub>count</sub></td>
<td class="org-left">nan</td>
<td class="org-left">open<sub>issues</sub><sub>count</sub></td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">language</td>
<td class="org-left">l</td>
<td class="org-left">language</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">topics</td>
<td class="org-left">nan</td>
<td class="org-left">topics</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">software<sub>heritage</sub><sub>exists</sub></td>
<td class="org-left">nan</td>
<td class="org-left">nan</td>
<td class="org-left">nan</td>
</tr>

<tr>
<td class="org-left">software<sub>heritage</sub><sub>url</sub></td>
<td class="org-left">nan</td>
<td class="org-left">nan</td>
<td class="org-left">nan</td>
</tr>
</tbody>
<tbody>
<tr>
<td class="org-left">organizations</td>
<td class="org-left">&#xa0;</td>
<td class="org-left">&#xa0;</td>
<td class="org-left">&#xa0;</td>
</tr>
</tbody>
<tbody>
<tr>
<td class="org-left">login</td>
<td class="org-left">l</td>
<td class="org-left">login</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup">https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup</a></td>
</tr>

<tr>
<td class="org-left">description</td>
<td class="org-left">d</td>
<td class="org-left">description</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup">https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup</a></td>
</tr>

<tr>
<td class="org-left">name</td>
<td class="org-left">n</td>
<td class="org-left">name</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup">https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup</a></td>
</tr>

<tr>
<td class="org-left">organization<sub>url</sub></td>
<td class="org-left">o</td>
<td class="org-left">owner<sub>url</sub></td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup">https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup</a></td>
</tr>

<tr>
<td class="org-left">avatar<sub>url</sub></td>
<td class="org-left">au</td>
<td class="org-left">icon<sub>url</sub></td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup">https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup</a></td>
</tr>

<tr>
<td class="org-left">website</td>
<td class="org-left">h</td>
<td class="org-left">website</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup">https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup</a></td>
</tr>

<tr>
<td class="org-left">location</td>
<td class="org-left">a</td>
<td class="org-left">location</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup">https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup</a></td>
</tr>

<tr>
<td class="org-left">email</td>
<td class="org-left">e</td>
<td class="org-left">email</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup">https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup</a></td>
</tr>

<tr>
<td class="org-left">is<sub>verified</sub></td>
<td class="org-left">v?</td>
<td class="org-left">nan</td>
<td class="org-left">nan</td>
</tr>

<tr>
<td class="org-left">repositories<sub>count</sub></td>
<td class="org-left">r</td>
<td class="org-left">repositories<sub>count</sub></td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup">https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup</a></td>
<td class="org-left">&#xa0;</td>
</tr>
</tbody>
</table>

creation<sub>date</sub>                      | c           | created<sub>at</sub>                           | <https://data.code.gouv.fr/api/v1/hosts/<HostName>/owners/lookup>                                                                                                                     |

<table border="2" cellspacing="0" cellpadding="6" rules="groups" frame="hsides">


<colgroup>
<col  class="org-left" />

<col  class="org-left" />

<col  class="org-left" />

<col  class="org-left" />
</colgroup>
<thead>
<tr>
<th scope="col" class="org-left">platform</th>
<th scope="col" class="org-left">p</th>
<th scope="col" class="org-left">nan (but can be found in "html<sub>url</sub>")</th>
<th scope="col" class="org-left"><a href="https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup">https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup</a></th>
</tr>
</thead>
<tbody>
<tr>
<td class="org-left">libraries</td>
<td class="org-left">&#xa0;</td>
<td class="org-left">&#xa0;</td>
<td class="org-left">&#xa0;</td>
</tr>
</tbody>
<tbody>
<tr>
<td class="org-left">deprecation<sub>reason</sub></td>
<td class="org-left">&#xa0;</td>
<td class="org-left">nan</td>
<td class="org-left">nan</td>
</tr>

<tr>
<td class="org-left">description</td>
<td class="org-left">d</td>
<td class="org-left">description</td>
<td class="org-left">(from packages software) <a href="https://data.code.gouv.fr/api/v1/packages/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/packages/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a> (other possible fields include: ecosystem, name, sort, order</td>
</tr>

<tr>
<td class="org-left">name</td>
<td class="org-left">&#xa0;</td>
<td class="org-left">name</td>
<td class="org-left">(from packages software) <a href="https://data.code.gouv.fr/api/v1/packages/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/packages/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">forks</td>
<td class="org-left">&#xa0;</td>
<td class="org-left">fork</td>
<td class="org-left">(from packages software) <a href="https://data.code.gouv.fr/api/v1/repositories/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">homepage</td>
<td class="org-left">&#xa0;</td>
<td class="org-left">homepage</td>
<td class="org-left">(from packages software) <a href="https://data.code.gouv.fr/api/v1/packages/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/packages/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">keywords</td>
<td class="org-left">&#xa0;</td>
<td class="org-left">keywords<sub>array</sub></td>
<td class="org-left">(from packages software) <a href="https://data.code.gouv.fr/api/v1/packages/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/packages/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">language</td>
<td class="org-left">&#xa0;</td>
<td class="org-left">nan</td>
<td class="org-left">nan</td>
</tr>

<tr>
<td class="org-left">stars</td>
<td class="org-left">&#xa0;</td>
<td class="org-left">stargazers<sub>count</sub></td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">latest<sub>download</sub><sub>url</sub></td>
<td class="org-left">&#xa0;</td>
<td class="org-left">download<sub>url</sub></td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">latest<sub>stable</sub><sub>release</sub><sub>number</sub></td>
<td class="org-left">&#xa0;</td>
<td class="org-left">name ? (First object)</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/hosts/%3ChostName%3E/repositories/%3CrepositoryName%3E/releases">https://data.code.gouv.fr/api/v1/hosts/%3ChostName%3E/repositories/%3CrepositoryName%3E/releases</a></td>
</tr>

<tr>
<td class="org-left">latest<sub>stable</sub><sub>release</sub><sub>published</sub><sub>at</sub></td>
<td class="org-left">&#xa0;</td>
<td class="org-left">published<sub>at</sub> (First object)</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/hosts/%3ChostName%3E/repositories/%3CrepositoryName%3E/releases">https://data.code.gouv.fr/api/v1/hosts/%3ChostName%3E/repositories/%3CrepositoryName%3E/releases</a></td>
</tr>

<tr>
<td class="org-left">license<sub>normalized</sub></td>
<td class="org-left">&#xa0;</td>
<td class="org-left">?</td>
<td class="org-left">?</td>
</tr>

<tr>
<td class="org-left">normalized<sub>licenses</sub></td>
<td class="org-left">&#xa0;</td>
<td class="org-left">normalized<sub>licenses</sub></td>
<td class="org-left">(from packages software) <a href="https://data.code.gouv.fr/api/v1/packages/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/packages/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">library<sub>manager</sub><sub>url</sub></td>
<td class="org-left">&#xa0;</td>
<td class="org-left">nan</td>
<td class="org-left">nan</td>
</tr>

<tr>
<td class="org-left">platform</td>
<td class="org-left">&#xa0;</td>
<td class="org-left">?</td>
<td class="org-left">?</td>
</tr>

<tr>
<td class="org-left">rank</td>
<td class="org-left">&#xa0;</td>
<td class="org-left">rankings ?</td>
<td class="org-left">(from packages software) <a href="https://data.code.gouv.fr/api/v1/packages/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/packages/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">status</td>
<td class="org-left">&#xa0;</td>
<td class="org-left">status</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>
</tbody>
</table>


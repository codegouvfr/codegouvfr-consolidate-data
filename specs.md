
# Contexte

On va reconstruire <https://code.gouv.fr/data/repos.json> à partir des
données de <https://data.code.gouv.fr> plutôt qu'à partir des scripts
actuels (codegouvfr-fetch-data et codegouvfr-consolidate-data).

Pour les champs actuels de repos.json, comment récupérer les données de
l'API de data.code.gouv.fr ?

Quels champs supprimer ?

Quels champs ajouter ?


# Tableau

<table border="2" cellspacing="0" cellpadding="6" rules="groups" frame="hsides">


<colgroup>
<col  class="org-left" />

<col  class="org-right" />

<col  class="org-left" />

<col  class="org-left" />
</colgroup>
<thead>
<tr>
<th scope="col" class="org-left">champ long</th>
<th scope="col" class="org-right">champ court</th>
<th scope="col" class="org-left">nouveau champ</th>
<th scope="col" class="org-left">url</th>
</tr>
</thead>
<tbody>
<tr>
<td class="org-left">repos</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">&#xa0;</td>
<td class="org-left">&#xa0;</td>
</tr>
</tbody>
<tbody>
<tr>
<td class="org-left">id</td>
<td class="org-right">?</td>
<td class="org-left">?</td>
<td class="org-left">?</td>
</tr>

<tr>
<td class="org-left">name</td>
<td class="org-right">n</td>
<td class="org-left">full_name</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">platform</td>
<td class="org-right">p</td>
<td class="org-left">"host" : { "name" : "string" }</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">repository_url</td>
<td class="org-right">r</td>
<td class="org-left">repository_url</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">description</td>
<td class="org-right">d</td>
<td class="org-left">description</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">default_branch</td>
<td class="org-right">nan</td>
<td class="org-left">default_branch</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">is_fork</td>
<td class="org-right">f?</td>
<td class="org-left">fork</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">is_archived</td>
<td class="org-right">a?</td>
<td class="org-left">archived</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">creation_date</td>
<td class="org-right">nan</td>
<td class="org-left">created_at</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">last_update</td>
<td class="org-right">u</td>
<td class="org-left">update_at ?</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">last modification</td>
<td class="org-right">nan</td>
<td class="org-left">pushed_at ?</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">homepage</td>
<td class="org-right">nan</td>
<td class="org-left">homepage</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">stars_count</td>
<td class="org-right">s</td>
<td class="org-left">subscribers_count ?</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">licence</td>
<td class="org-right">li</td>
<td class="org-left">license</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">open_issues_count</td>
<td class="org-right">nan</td>
<td class="org-left">open_issues_count</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">language</td>
<td class="org-right">l</td>
<td class="org-left">language</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">topics</td>
<td class="org-right">nan</td>
<td class="org-left">topics</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">software_heritage_exists</td>
<td class="org-right">nan</td>
<td class="org-left">nan</td>
<td class="org-left">nan</td>
</tr>

<tr>
<td class="org-left">software_heritage_url</td>
<td class="org-right">nan</td>
<td class="org-left">nan</td>
<td class="org-left">nan</td>
</tr>
</tbody>
<tbody>
<tr>
<td class="org-left">organizations</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">&#xa0;</td>
<td class="org-left">&#xa0;</td>
</tr>
</tbody>
<tbody>
<tr>
<td class="org-left">login</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">login</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup">https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup</a></td>
</tr>

<tr>
<td class="org-left">description</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">description</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup">https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup</a></td>
</tr>

<tr>
<td class="org-left">name</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">name</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup">https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup</a></td>
</tr>

<tr>
<td class="org-left">organization_url</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">owner_url</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup">https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup</a></td>
</tr>

<tr>
<td class="org-left">avatar_url</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">icon_url</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup">https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup</a></td>
</tr>

<tr>
<td class="org-left">website</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">website</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup">https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup</a></td>
</tr>

<tr>
<td class="org-left">location</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">location</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup">https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup</a></td>
</tr>

<tr>
<td class="org-left">email</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">email</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup">https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup</a></td>
</tr>

<tr>
<td class="org-left">is_verified</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">nan</td>
<td class="org-left">nan</td>
</tr>

<tr>
<td class="org-left">repositories_count</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">repositories_count</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup">https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup</a></td>
</tr>

<tr>
<td class="org-left">creation_date</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">created_at</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup">https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup</a></td>
</tr>

<tr>
<td class="org-left">platform</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">nan (but can be found in "html_url"</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup">https://data.code.gouv.fr/api/v1/hosts/%3CHostName%3E/owners/lookup</a></td>
</tr>
</tbody>
<tbody>
<tr>
<td class="org-left">libraries</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">&#xa0;</td>
<td class="org-left">&#xa0;</td>
</tr>
</tbody>
<tbody>
<tr>
<td class="org-left">deprecation_reason</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">nan</td>
<td class="org-left">nan</td>
</tr>

<tr>
<td class="org-left">description</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">description</td>
<td class="org-left">(from packages software) <a href="https://data.code.gouv.fr/api/v1/packages/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/packages/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a> (other possible fields include: ecosystem, name, sort, order</td>
</tr>

<tr>
<td class="org-left">name</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">name</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/packages/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/packages/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">forks</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">fork</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">homepage</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">homepage</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/packages/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/packages/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">keywords</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">keywords_array</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/packages/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/packages/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">language</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">nan</td>
<td class="org-left">nan</td>
</tr>

<tr>
<td class="org-left">stars</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">stargazers_count</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">latest_download_url</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">download_url</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">latest_stable_release_number</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">name ? (Firt object)</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/hosts/%3ChostName%3E/repositories/%3CrepositoryName%3E/releases">https://data.code.gouv.fr/api/v1/hosts/%3ChostName%3E/repositories/%3CrepositoryName%3E/releases</a></td>
</tr>

<tr>
<td class="org-left">latest_stable_release_published_at</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">published_at (Firts object)</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/hosts/%3ChostName%3E/repositories/%3CrepositoryName%3E/releases">https://data.code.gouv.fr/api/v1/hosts/%3ChostName%3E/repositories/%3CrepositoryName%3E/releases</a></td>
</tr>

<tr>
<td class="org-left">license_normalized</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">?</td>
<td class="org-left">?</td>
</tr>

<tr>
<td class="org-left">normalized_licenses</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">normalized_licenses</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/packages/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/packages/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">library_manager_url</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">nan</td>
<td class="org-left">nan</td>
</tr>

<tr>
<td class="org-left">platform</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">?</td>
<td class="org-left">?</td>
</tr>

<tr>
<td class="org-left">rank</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">rankings ?</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/packages/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/packages/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">status</td>
<td class="org-right">&#xa0;</td>
<td class="org-left">status</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?repository_url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>
</tbody>
</table>


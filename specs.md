
# Contexte

On va reconstruire <https://code.gouv.fr/data/repos.json> à partir des données de <https://data.code.gouv.fr> plutôt qu'à partir des scripts actuels (codegouvfr-fetch-data et codegouvfr-consolidate-data).

Pour les champs actuels de repos.json, comment récupérer les données de l'API de data.code.gouv.fr ?

Quels champs supprimer ?

Quels champs ajouter ?


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
<td class="org-left">id</td>
<td class="org-left">?</td>
<td class="org-left">?</td>
<td class="org-left">?</td>
</tr>

<tr>
<td class="org-left">name</td>
<td class="org-left">n</td>
<td class="org-left">full_name</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">platform</td>
<td class="org-left">p</td>
<td class="org-left">"host" : { "name" : "string" }</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">repository_url</td>
<td class="org-left">r</td>
<td class="org-left">repository_url</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">description</td>
<td class="org-left">d</td>
<td class="org-left">description</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">default_branch</td>
<td class="org-left">nan</td>
<td class="org-left">default_branch</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">is_fork</td>
<td class="org-left">f?</td>
<td class="org-left">fork</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">is_archived</td>
<td class="org-left">a?</td>
<td class="org-left">archived</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">creation_date</td>
<td class="org-left">nan</td>
<td class="org-left">created_at</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">last_update</td>
<td class="org-left">u</td>
<td class="org-left">update_at ?</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">last modification</td>
<td class="org-left">nan</td>
<td class="org-left">pushed_at ?</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">homepage</td>
<td class="org-left">nan</td>
<td class="org-left">homepage</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">stars_count</td>
<td class="org-left">s</td>
<td class="org-left">subscribers_count ?</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">licence</td>
<td class="org-left">li</td>
<td class="org-left">license</td>
<td class="org-left"><a href="https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E">https://data.code.gouv.fr/api/v1/repositories/lookup?url=%3Crepo_url%3E&amp;purl=%3Cpackage_url%3E</a></td>
</tr>

<tr>
<td class="org-left">open_issues_count</td>
<td class="org-left">nan</td>
<td class="org-left">open_issues_count</td>
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
<td class="org-left">software_heritage_exists</td>
<td class="org-left">nan</td>
<td class="org-left">nan</td>
<td class="org-left">nan</td>
</tr>

<tr>
<td class="org-left">software_heritage_url</td>
<td class="org-left">nan</td>
<td class="org-left">nan</td>
<td class="org-left">nan</td>
</tr>
</tbody>
</table>


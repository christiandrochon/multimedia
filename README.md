# Recherche d'images par similarité

Projet de vision par ordinateur permettant la recherche d'images par similarité à partir de descripteurs classiques et d'embeddings générés par réseau de neurones.

## Présentation

Ce projet implémente un moteur de recherche d’images par similarité reposant sur plusieurs familles de descripteurs visuels et sémantiques.

L'application permet à un utilisateur de soumettre une image requête puis de retrouver automatiquement les images les plus proches au sein d'une base documentaire.
Les descripteurs des images sont pré-calculés et indexés de manière asynchrone afin de réduire les temps de réponse lors des recherches.

Le système combine :

- descripteurs visuels classiques (couleur, niveaux de gris, forme) ;
- descripteurs textuels ;
- embeddings générés par un réseau de neurones convolutionnel ResNet ;
- mécanismes de fusion et de pondération des scores ;
- évaluation quantitative à partir d'une vérité terrain.

L'application est développée sous forme d'une architecture full-stack Angular / Spring Boot et peut être exécutée intégralement via Docker Compose.

---

## Cas d'usage

- Recherche d’images similaires dans une base documentaire.
- Exploration de collections multimédias.
- Évaluation et comparaison de méthodes de recherche visuelle.
- Démonstration de techniques de vision par ordinateur et de recherche vectorielle.

---

## Exécution via Docker 

Le projet peut être lancé **dans son intégralité (backend + frontend)** via Docker Compose.

### Prérequis
- Docker
- Docker Compose

### Lancement de l’application complète

Depuis la **racine du projet** :

```bash
docker compose -f compose.yaml up -d
``` 
Cette commande :
* construit les images nécessaires,
* démarre l’ensemble des services en arrière-plan,
* rend l’application immédiatement accessible.

### Accès aux services

Frontend :
`http://localhost:4200`

Backend API :
`http://localhost:8087`

### Arrêt des services
```bash
docker compose -f compose.yaml down
``` 
### Remarques
Les ports exposés peuvent être modifiés dans compose.yaml.
Les volumes Docker permettent de persister les données (images, indexs, embeddings).
Cette méthode est recommandée pour les démonstrations et les audits techniques, car elle garantit un environnement reproductible.

--- 

## Fonctionnalités principales

- Upload d’une image requête via interface web
- Recherche par similarité selon différents modes :
    - couleur
    - forme
    - texte
    - deep features (ResNet)
    - combinaison pondérée des méthodes
- Retour des résultats classés par score de similarité
- Évaluation des performances (précision, rappel) à partir de vérité terrain
- Architecture modulaire facilitant l’ajout de nouveaux descripteurs

---

## Compétences démontrées

- Conception d’une architecture full-stack Angular / Spring Boot.
- Développement d’une API REST de recherche multimédia.
- Traitements asynchrones pour la génération et l’indexation des descripteurs.
- Implémentation d’algorithmes de recherche par similarité d’images.
- Extraction et comparaison de descripteurs visuels (couleur, niveaux de gris, forme et texte).
- Utilisation d’un réseau de neurones convolutionnel (ResNet) pour la génération d’embeddings.
- Mise en œuvre d’une recherche vectorielle basée sur les plus proches voisins.
- Combinaison et pondération de plusieurs métriques de similarité.
- Évaluation des performances à l’aide d’indicateurs de précision et de rappel.
- Conteneurisation et exécution reproductible avec Docker Compose.

---

## Architecture du projet
````text
.
├── multimedias-ui/ # Frontend Angular
├── multimedias/    # Backend Spring Boot / Java
├── data/
│ ├── images/       # Jeux d’images
│ ├── indexes/      # Indexs de descripteurs
│ └── ground_truth/ # Vérité terrain (CSV / JSON)
├── LICENSE
└── README.md
````

## Technologies utilisées

### Backend
- Java 17
- Spring Boot
- Maven

### Frontend
- Angular
- TypeScript

### IA / Vision par ordinateur
- ResNet
- Embeddings vectoriels
- Recherche par similarité d'images

### Infrastructure
- Docker
- Docker Compose


---
## Backend — Spring Boot / Java
Dossier : `multimedias` 

### Lancement
```bash
cd multimedias
mvn spring-boot:run
```
Ou build du JAR :
```bash
mvn clean package
java -jar target/*.jar
```

Port par défaut : `8087`


---

## Frontend — Angular

**Dossier** : `multimedias-ui/`

### Lancement en développement

```bash
cd multimedias-ui
npm install
ng serve
````  

Application accessible par défaut sur : `http://localhost:4200`

### Build production
```bash
ng build --prod
```
Les fichiers build sont générés dans `multimedias-ui/dist/`.

### Fonctionnalités UI
* Upload d’une image requête
* Sélection du mode de recherche (couleur / forme / texte / deep / combiné)
* Affichage des résultats :
  * miniature
  * score de similarité 
  * métadonnées
* Visualisation des métriques (si activées)

### Configuration proxy (développement)
Un fichier proxy.conf.json peut être utilisé pour rediriger /api vers le backend :
````json
{
  "/api": {
    "target": "http://localhost:8087",
    "secure": false
  }
}
````

--- 
## API REST — Endpoints principaux
Recherche par image
`POST /api/search`

### Paramètres (multipart/form-data) :
* file : image requête
* topK (optionnel) : nombre de résultats
* mode (optionnel) :
  * color
  * shape
  * text
  * deep
  * combined

### Réponse :
````json
[
  {
    "id": "img123",
    "score": 0.92,
    "thumbnailUrl": "/images/thumbs/img123.jpg",
    "metadata": {
      "tags": ["beach"],
      "dominantColor": "blue"
    }
  }
]
````

### Métadonnées image
`GET /api/images/{id}`

### Évaluation
`GET /api/metrics/precision-recall`

Retourne les métriques calculées à partir de la vérité terrain (si activée).

--- 
## Descripteurs et pipeline
### Prétraitement
* Redimensionnement
* Normalisation
* Conversion RGB / niveaux de gris

### Couleur
* Histogrammes par canal
* Quantification des niveaux
* Distances : euclidienne, χ²

### Niveaux de gris
* Histogrammes
* Comparaison par distance

### Forme
* Extraction de contours
* Moments invariants
* Descripteurs de forme globaux

### Texte
* Extraction OCR et/ou métadonnées
* Indexation textuelle
* Recherche basée sur similarité lexicale

### Deep features (ResNet)
* Extraction d’embeddings à partir d’un réseau ResNet pré-entraîné
* Indexation vectorielle et recherche des plus proches voisins.
* Combinaison possible avec les scores des autres descripteurs

--- 
## Vérité terrain et évaluation
Les fichiers de vérité terrain sont stockés dans :
````text
data/ground_truth/
````

Format typique (CSV ou JSON) :
````text
query_id, relevant_id1, relevant_id2, ...
````

### Métriques calculées :
* précision
* rappel
* courbes précision / rappel

### Exemple d’appel curl
````bash
curl -X POST http://localhost:8087/api/search \
  -F file=@image.jpg \
  -F topK=10 \
  -F mode=combined
````

## Déploiement
* Backend : JAR Spring Boot derrière Nginx ou Docker
* Frontend : fichiers `dist/` servis via Nginx ou CDN
* Indexes et embeddings stockés sur volume persistant

---
## Tests
### Backend :
* JUnit
* tests d’intégration Spring Boot

### Frontend :
* tests unitaires Angular
* e2e (Cypress / Protractor)

---
## Licence MIT

Ce projet est distribué sous licence MIT.
Voir le fichier [LICENSE](LICENSE).

---
## Contact
Mainteneur du projet : christiandrochon (GitHub)
# Recherche d'images par similarité


## Présentation

Ce projet implémente une application complète de **recherche par similarité d’images à partir d’une image requête**.  
L’utilisateur soumet une image, le système extrait différents types de descripteurs et retourne les images les plus similaires selon une métrique donnée.

L’approche combine :
- descripteurs **classiques** (couleur, niveaux de gris, forme, texte),
- descripteurs **profonds** (embeddings extraits par réseau de neurones convolutionnel de type ResNet),
- une **évaluation quantitative** basée sur des classes de vérité terrain.

Le projet est structuré en frontend (Angular) et backend (Spring Boot / Java).

---

## Ce que démontre ce projet

Ce projet met en évidence les compétences suivantes :

### Conception logicielle et architecture
- Conception d’une application **full-stack** structurée (Angular / Spring Boot)
- Séparation claire des responsabilités (UI, API, services, indexation, évaluation)
- Architecture modulaire permettant l’ajout ou le remplacement de descripteurs

### Traitement d’images et vision par ordinateur
- Implémentation de **descripteurs visuels classiques** (couleur, niveaux de gris, forme)
- Mise en œuvre de **pipelines de prétraitement** (normalisation, redimensionnement)
- Compréhension des limites et complémentarités entre descripteurs hand-crafted et deep learning

### Machine Learning et deep learning appliqué
- Utilisation de **réseaux de neurones convolutionnels (ResNet)** pour l’extraction d’embeddings
- Indexation vectorielle et recherche de voisins proches (nearest neighbors)
- Combinaison de scores issus de différentes méthodes de similarité

### Recherche d’information et évaluation
- Mise en place d’une **vérité terrain** pour l’évaluation expérimentale
- Calcul de métriques standards : précision, rappel, mAP
- Analyse comparative des performances selon les méthodes utilisées

### Développement backend et API
- Conception d’API REST pour la recherche multimédia
- Gestion d’upload de fichiers et de réponses structurées JSON
- Tests unitaires et tests d’intégration backend

### Bonnes pratiques de projet
- Projet versionné, documenté et distribuable (licence MIT)
- README structuré et orienté compréhension rapide
- Déploiement possible en environnement de production (Docker / Nginx)

Ce projet illustre une capacité à **relier des concepts théoriques (vision, ML, recherche d’information) à une implémentation logicielle complète et exploitable**.


--- 

## Fonctionnalités principales

- Upload d’une image requête aléatoire via interface web
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

## Architecture du projet
````text
.
├── multimedias-ui/ # Frontend Angular
├── backend/ # Backend Spring Boot / Java
├── data/
│ ├── images/ # Jeux d’images
│ ├── indexes/ # Indexs de descripteurs
│ └── ground_truth/ # Vérité terrain (CSV / JSON)
├── LICENSE
└── README.md
````

---
## Backend — Spring Boot / Java
Dossier : `multimedias` 

### Lancement
```bash
cd backend
mvn spring-boot:run
```
Ou build du JAR :
```bash
mvn clean package
java -jar target/*.jar
```

Port par défaut : `8080`


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
    "target": "http://localhost:8080",
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
* Indexation vectorielle (FAISS, Annoy ou implémentation maison)
* Recherche par nearest neighbors
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
curl -X POST http://localhost:8080/api/search \
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
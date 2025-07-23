# Documentation API - Gestion des Tâches

## Vue d'ensemble

Cette API RESTful permet de gérer des tâches. Elle est documentée avec OpenAPI 3.0 et Swagger UI.

## Technologies utilisées

- **Spring Boot 3.5.3**
- **Kotlin**
- **SpringDoc OpenAPI 2.3.0**
- **Bean Validation (Jakarta)**

## Endpoints disponibles

### Gestion des tâches (`/api/tasks`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/api/tasks` | Récupère toutes les tâches (paginé) |
| `GET` | `/api/tasks/pending` | Récupère les tâches en attente (paginé) |
| `GET` | `/api/tasks/{id}` | Récupère une tâche par son ID |
| `POST` | `/api/tasks` | Crée une nouvelle tâche |
| `PATCH` | `/api/tasks/{id}/status` | Met à jour le statut d'une tâche |

## Modèles de données

### Task
```json
{
  "id": 1,
  "label": "Terminer la documentation API",
  "description": "Ajouter la documentation OpenAPI avec Swagger UI",
  "completed": false
}
```

### CreateTaskRequest
```json
{
  "label": "Nouvelle tâche",
  "description": "Description de la nouvelle tâche"
}
```

### UpdateTaskStatusRequest
```json
{
  "completed": true
}
```

### PageResponse (Réponse paginée)
```json
{
  "content": [
    {
      "id": 1,
      "label": "Terminer la documentation API",
      "description": "Ajouter la documentation OpenAPI avec Swagger UI",
      "completed": false
    },
    {
      "id": 2,
      "label": "Tester l'API",
      "description": "Effectuer des tests d'intégration",
      "completed": true
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 15,
  "totalPages": 2,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false
}
```

## Pagination

Les endpoints qui retournent des listes supportent la pagination avec les paramètres suivants :

- `page` : Numéro de page (commence à 0)
- `size` : Nombre d'éléments par page (défaut : 10)
- `sort` : Critère de tri (ex: `id,desc`)

Exemple : `/api/tasks?page=0&size=5&sort=id,desc`

### Structure de la réponse paginée

Tous les endpoints paginés retournent un objet `PageResponse` contenant :

- `content` : Liste des éléments de la page courante
- `pageNumber` : Numéro de la page courante (base 0)
- `pageSize` : Taille de la page demandée
- `totalElements` : Nombre total d'éléments disponibles
- `totalPages` : Nombre total de pages
- `first` : Indique si c'est la première page
- `last` : Indique si c'est la dernière page
- `hasNext` : Indique s'il y a une page suivante
- `hasPrevious` : Indique s'il y a une page précédente

## Validation

Les requêtes sont validées automatiquement :

- **label** : Obligatoire, maximum 100 caractères
- **description** : Obligatoire, maximum 500 caractères
- **completed** : Obligatoire pour les mises à jour de statut

## Codes de réponse HTTP

- `200 OK` : Succès
- `201 Created` : Ressource créée avec succès
- `400 Bad Request` : Données de requête invalides
- `404 Not Found` : Ressource non trouvée

## Documentation interactive

### Accès à Swagger UI

Une fois l'application démarrée, accédez à la documentation interactive :

**URL locale** : http://localhost:8080/swagger-ui.html

### Accès aux spécifications OpenAPI

Les spécifications OpenAPI au format JSON sont disponibles à :

**URL locale** : http://localhost:8080/api-docs

## Démarrage de l'application

1. **Prérequis** :
   - Java 21+
   - Maven 3.6+

2. **Lancement** :
   ```bash
   cd api
   ./mvnw spring-boot:run
   ```

3. **Accès à la documentation** :
   - Swagger UI : http://localhost:8080/swagger-ui.html
   - API Docs : http://localhost:8080/api-docs

## Exemples d'utilisation

### Créer une tâche
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "label": "Ma nouvelle tâche",
    "description": "Description de ma tâche"
  }'
```

### Récupérer toutes les tâches (avec pagination)
```bash
curl http://localhost:8080/api/tasks?page=0&size=10
```

**Réponse :**
```json
{
  "content": [
    {
      "id": 1,
      "label": "Boire une bière",
      "description": "Retrouver les potes et boire une bière (minimum)",
      "completed": true
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 15,
  "totalPages": 2,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false
}
```

### Récupérer les tâches en attente
```bash
curl http://localhost:8080/api/tasks/pending?page=0&size=5&sort=label,asc
```

### Mettre à jour le statut d'une tâche
```bash
curl -X PATCH http://localhost:8080/api/tasks/1/status \
  -H "Content-Type: application/json" \
  -d '{"completed": true}'
```

## Configuration personnalisée

La configuration OpenAPI peut être modifiée dans :
- `OpenApiConfiguration.kt` pour les métadonnées
- `application.properties` pour les paramètres Swagger UI
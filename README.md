# 🚀 TestGithubActions - CI/CD Pipeline

Pipeline automatisé de déploiement pour une application Spring Boot sur VM avec séparation des environnements DEV/PROD.

## 📋 Table des matières

- [Architecture](#architecture)
- [Prérequis](#prérequis)
- [Configuration](#configuration)
- [Utilisation](#utilisation)
- [Gestion des services](#gestion-des-services)
- [Dépannage](#dépannage)
- [Commandes utiles](#commandes-utiles)

## 🏗️ Architecture

### Environnements

| Environnement | Branche   | Port | Répertoire                           | Service systemd              |
|---------------|-----------|------|--------------------------------------|------------------------------|
| **DEV**       | `develop` | 9091 | `/opt/apps/dev/java/TestGithubActions` | `testgithubactions-dev`      |
| **PROD**      | `main`    | 9092 | `/opt/apps/prod/java/TestGithubActions`| `testgithubactions-prod`     |

### Pipeline CI/CD
```
┌─────────────┐
│   Commit    │
│  & Push     │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ 1. BUILD    │ ← Compile + Tests
│   & TEST    │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ 2. CONFIG   │ ← Détection env
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ 3. DEPLOY   │ ← Transfert + systemd
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ 4. VERIFY   │ ← Health checks
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ 5. NOTIFY   │ ← Rapport
└─────────────┘
```

## 📦 Prérequis

### Sur votre machine locale

- Java 17+
- Maven 3.6+
- Git

### Sur la VM de déploiement

- Ubuntu 24.04 (ou compatible)
- Java 17+ installé
- systemd actif
- Accès SSH avec clé
- Ports 9091 et 9092 ouverts
- `sudo` activé pour l'utilisateur de déploiement

### Installation Java sur la VM
```bash
# Installation OpenJDK 17
sudo apt update
sudo apt install -y openjdk-17-jdk

# Vérification
java -version
```

## ⚙️ Configuration

### 1. Secrets GitHub

Configurez les secrets dans **Settings → Secrets and variables → Actions** :

#### Secrets au niveau repository (partagés)

| Secret             | Description                      | Exemple             |
|--------------------|----------------------------------|---------------------|
| `HOST`             | Adresse IP ou hostname de la VM  | `207.180.240.150`   |
| `USERNAME`         | Utilisateur SSH                  | `appuser`           |
| `SSH_PRIVATE_KEY`  | Clé SSH privée (format PEM)      | `-----BEGIN RSA...` |
| `PORT`             | Port SSH                         | `22`                |

### 2. Environnements GitHub

Créez deux environnements dans **Settings → Environments** :

#### Environnement DEV
- Nom : `DEV`
- Protection rules : Aucune (déploiement automatique)

#### Environnement PROD
- Nom : `PROD`
- Protection rules :
    - ☑️ Required reviewers (ajoutez-vous)
    - ☑️ Wait timer : 0 minutes
    - Branches autorisées : `main`

### 3. Structure des répertoires sur la VM
```bash
# Création de la structure
sudo mkdir -p /opt/apps/dev/java/TestGithubActions
sudo mkdir -p /opt/apps/prod/java/TestGithubActions

# Attribution des permissions
sudo chown -R appuser:appuser /opt/apps/
```

## 🚀 Utilisation

### Déploiement automatique

#### Sur DEV (développement)
```bash
git checkout develop
git add .
git commit -m "feat: nouvelle fonctionnalité"
git push origin develop
```

➡️ **Déploiement automatique sur DEV (port 9091)**

#### Sur PROD (production)
```bash
# Fusion de develop vers main
git checkout main
git merge develop
git push origin main
```

➡️ **Déploiement sur PROD après approbation manuelle (port 9092)**

### Déclenchement manuel

Via l'interface GitHub :
1. Allez dans **Actions**
2. Sélectionnez le workflow **Java CI/CD with Maven**
3. Cliquez sur **Run workflow**
4. Choisissez la branche (`develop` ou `main`)

## 🔧 Gestion des services

### Commandes systemd

#### Sur DEV
```bash
# Statut du service
sudo systemctl status testgithubactions-dev

# Démarrer
sudo systemctl start testgithubactions-dev

# Arrêter
sudo systemctl stop testgithubactions-dev

# Redémarrer
sudo systemctl restart testgithubactions-dev

# Logs en temps réel
sudo journalctl -u testgithubactions-dev -f

# Dernières 100 lignes de logs
sudo journalctl -u testgithubactions-dev -n 100 --no-pager
```

#### Sur PROD
```bash
# Même chose avec testgithubactions-prod
sudo systemctl status testgithubactions-prod
sudo journalctl -u testgithubactions-prod -f
```

### Vérification manuelle
```bash
# Vérifier si l'application répond
curl http://localhost:9091/  # DEV
curl http://localhost:9092/  # PROD

# Vérifier les ports en écoute
lsof -i:9091  # DEV
lsof -i:9092  # PROD

# Vérifier les processus Java
ps aux | grep java
```

## 🐛 Dépannage

### Problème : L'application ne démarre pas
```bash
# 1. Vérifier les logs systemd
sudo journalctl -u testgithubactions-dev -n 100 --no-pager

# 2. Vérifier le fichier de log applicatif
tail -50 /opt/apps/dev/java/TestGithubActions/app-dev.log

# 3. Vérifier que le JAR existe
ls -lh /opt/apps/dev/java/TestGithubActions/app.jar

# 4. Tester le JAR manuellement
cd /opt/apps/dev/java/TestGithubActions
java -jar app.jar --server.port=9091
```

### Problème : Port déjà utilisé
```bash
# Trouver le processus qui utilise le port
lsof -i:9091

# Arrêter le processus
sudo systemctl stop testgithubactions-dev

# Ou tuer le processus manuellement
kill -9 <PID>
```

### Problème : Permissions insuffisantes
```bash
# Vérifier les permissions du répertoire
ls -lah /opt/apps/dev/java/TestGithubActions

# Corriger les permissions
sudo chown -R appuser:appuser /opt/apps/dev/java/TestGithubActions
```

### Problème : Service ne redémarre pas automatiquement
```bash
# Réactiver le service
sudo systemctl enable testgithubactions-dev

# Vérifier le statut
systemctl is-enabled testgithubactions-dev
```

## 📚 Commandes utiles

### Gestion des logs
```bash
# Rotation des logs applicatifs
cd /opt/apps/dev/java/TestGithubActions
ls -lt *.log | tail -n +6 | xargs rm -f  # Garde les 5 derniers

# Compresser les anciens logs
gzip app-dev.log.old

# Voir les logs systemd depuis le dernier boot
sudo journalctl -u testgithubactions-dev -b
```

### Monitoring en temps réel
```bash
# CPU et mémoire du processus Java
top -p $(pgrep -f "app.jar.*9091")

# Statistiques détaillées
ps aux | grep "app.jar.*9091"

# Connexions réseau
netstat -tulpn | grep :9091
```

### Sauvegarde et restauration
```bash
# Sauvegarder l'application actuelle
cp /opt/apps/prod/java/TestGithubActions/app.jar \
   /opt/apps/prod/java/TestGithubActions/app.jar.backup-$(date +%Y%m%d)

# Restaurer une version précédente
sudo systemctl stop testgithubactions-prod
cp /opt/apps/prod/java/TestGithubActions/app.jar.backup-20250208 \
   /opt/apps/prod/java/TestGithubActions/app.jar
sudo systemctl start testgithubactions-prod
```

## 📊 Métriques et monitoring

### URLs de santé
```bash
# Health check endpoint (si Spring Actuator activé)
curl http://localhost:9091/actuator/health

# Métriques
curl http://localhost:9091/actuator/metrics
```

### Ajouter Spring Boot Actuator

Dans votre `pom.xml` :
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Dans `application.yml` :
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

## 🔐 Sécurité

### Bonnes pratiques

- ✅ Les secrets sont stockés dans GitHub Secrets (jamais dans le code)
- ✅ Accès SSH par clé (pas de mot de passe)
- ✅ Service systemd exécuté avec utilisateur non-root
- ✅ Logs rotatés automatiquement
- ✅ Approbation manuelle pour la production

### Recommandations supplémentaires
```bash
# Configurer le firewall (ufw)
sudo ufw allow 9091/tcp  # DEV
sudo ufw allow 9092/tcp  # PROD
sudo ufw enable

# Limiter les connexions SSH
sudo ufw allow from <VOTRE_IP> to any port 22
```

## 🤝 Contribution

1. Créer une branche feature : `git checkout -b feature/ma-fonctionnalite`
2. Commiter les changements : `git commit -m "feat: description"`
3. Pusher la branche : `git push origin feature/ma-fonctionnalite`
4. Créer une Pull Request vers `develop`

## 📝 Changelog

### Version 1.0.0 (2025-02-09)
- ✅ Pipeline CI/CD complet
- ✅ Tests automatisés
- ✅ Déploiement avec systemd
- ✅ Séparation DEV/PROD
- ✅ Health checks automatiques

## 📞 Support

Pour toute question ou problème :
- Consultez les logs : `sudo journalctl -u testgithubactions-dev`
- Vérifiez les actions GitHub : Repository → Actions
- Créez une issue : Repository → Issues

## 📄 Licence

[Votre licence ici]

---

**Auteur** : ISK-SOFTOOLS  
**Dernière mise à jour** : 2025-02-09
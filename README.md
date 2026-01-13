# ⚽ Système Distribué de Prédiction de Football

> Architecture microservices événementielle pour l'analyse et la prédiction sportive.

## 📋 Description
Cette application a pour but de fournir des prédictions fiables sur les matchs de football en analysant l'historique des performances des équipes. Elle abandonne l'approche monolithique pour une architecture distribuée, garantissant scalabilité et résilience.

Le système gère le cycle de vie complet des données : de la récupération du calendrier des matchs à l'ingestion des statistiques, jusqu'au calcul des probabilités de victoire.

## 🛠 Stack Technique
* **Architecture :** Microservices
* **Backend :** Java, Spring Boot (Spring Cloud)
* **Messaging / Streaming :** Apache Kafka
* **Data & ML :** Python (pour le module de prédiction), PostgreSQL/MongoDB
* **Discovery & Config :** Eureka, Config Server

## ⚙️ Fonctionnalités Clés
* **Microservice Calendrier :** Gestion des fixtures et des mises à jour de matchs.
* **Microservice Statistiques :** Agrégation des données historiques (buts, possession, fautes).
* **Pipeline Kafka :** Communication asynchrone et découplée entre les services.
* **Moteur de Prédiction :** Algorithme de Machine Learning consommant les données traitées pour générer des pronostics.

## 🚀 Architecture
1.  Le **Service Calendrier** publie les nouveaux matchs dans un topic Kafka.
2.  Le **Service Stats** enrichit ces données avec l'historique des équipes.
3.  Le **Moteur ML** consomme ces données enrichies pour produire une prédiction.

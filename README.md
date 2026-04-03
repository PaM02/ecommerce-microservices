# Guide Complet : Microservices avec Spring Boot

## 📚 Table des matières

1. [Introduction aux Microservices](#introduction)
2. [Phase 1 : Créer les Microservices de base](#phase-1)
3. [Phase 2 : Eureka Server - Service Discovery](#phase-2)
4. [Phase 3 : Feign Client - Communication entre services](#phase-3)
5. [Phase 4 : API Gateway - Point d'entrée unique](#phase-4)
6. [Phase 5 : Config Server - Centralisation de la configuration](#phase-5)
7. [Phase 6 : Base de données - PostgreSQL avec Spring Data JPA](#phase-6)
8. [Architecture finale](#architecture-finale)
9. [Troubleshooting](#troubleshooting)

---

## 🎯 Introduction aux Microservices {#introduction}

### Qu'est-ce qu'un microservice ?

**Analogie du restaurant** :
- **Monolithique** : Un seul chef fait tout (commandes, cuisine, service, comptabilité)
- **Microservices** : Plusieurs chefs spécialisés (un pour les entrées, un pour les plats, un pour les desserts)

### Architecture de base

Au lieu d'avoir UNE grosse application, on a plusieurs petites applications indépendantes :
- **Service Produits** : Gère le catalogue
- **Service Commandes** : Gère les achats
- **Service Utilisateurs** : Gère les comptes

### Avantages

- ✅ **Indépendance** : Modifier un service sans toucher aux autres
- ✅ **Scalabilité** : Multiplier seulement les services qui ont beaucoup de charge
- ✅ **Technologie mixte** : Chaque service peut utiliser sa propre base de données
- ✅ **Équipes séparées** : Une équipe par service

---

## 📦 Phase 1 : Créer les Microservices de base {#phase-1}

### 1.1 Product Service

#### Création du projet

**Sur Spring Initializr** (https://start.spring.io/) :
- **Project** : Maven
- **Language** : Java
- **Spring Boot** : 3.2.x
- **Group** : com.ecommerce
- **Artifact** : product-service
- **Package name** : com.ecommerce.productservice
- **Java** : 17 ou 21
- **Dépendances** :
  - Spring Web
  - Spring Boot DevTools
  - Lombok (optionnel)

#### Structure du projet

```
product-service/
├── src/main/java/com/ecommerce/productservice/
│   ├── ProductServiceApplication.java
│   ├── model/
│   │   └── Product.java
│   ├── controller/
│   │   └── ProductController.java
│   └── service/
│       └── ProductService.java
└── src/main/resources/
    └── application.properties
```

#### Code : Product.java

```java
package com.ecommerce.productservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    private String name;
    private Double price;
    private Integer stock;
}
```

#### Code : ProductService.java

```java
package com.ecommerce.productservice.service;

import com.ecommerce.productservice.model.Product;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
    
    private List<Product> products = new ArrayList<>();

    public ProductService() {
        // Données de test
        products.add(new Product(1L, "Ordinateur", 999.99, 10));
        products.add(new Product(2L, "Souris", 29.99, 50));
        products.add(new Product(3L, "Clavier", 79.99, 30));
    }

    public List<Product> getAllProducts() {
        return products;
    }

    public Product getProductById(Long id) {
        return products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
```

#### Code : ProductController.java

```java
package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.model.Product;
import com.ecommerce.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }
}
```

#### Configuration : application.properties

```properties
spring.application.name=product-service
server.port=8081
```

#### Test

```bash
GET http://localhost:8081/api/products
GET http://localhost:8081/api/products/1
```

---

### 1.2 Order Service

#### Création du projet

**Sur Spring Initializr** :
- **Artifact** : order-service
- **Package name** : com.ecommerce.orderservice
- Mêmes dépendances que product-service

#### Structure du projet

```
order-service/
├── src/main/java/com/ecommerce/orderservice/
│   ├── OrderServiceApplication.java
│   ├── model/
│   │   └── Order.java
│   ├── controller/
│   │   └── OrderController.java
│   └── service/
│       └── OrderService.java
└── src/main/resources/
    └── application.properties
```

#### Code : Order.java

```java
package com.ecommerce.orderservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Entity
@Table(name = "orders")
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double totalPrice;
}
```

#### Code : OrderService.java

```java
package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.model.Order;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    
    private List<Order> orders = new ArrayList<>();
    private Long nextId = 1L;

    public OrderService() {
        orders.add(new Order(nextId++, 1L, "Ordinateur", 1, 999.99));
        orders.add(new Order(nextId++, 2L, "Souris", 2, 59.98));
    }

    public List<Order> getAllOrders() {
        return orders;
    }

    public Order getOrderById(Long id) {
        return orders.stream()
                .filter(o -> o.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
```

#### Code : OrderController.java

```java
package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }
}
```

#### Configuration : application.properties

```properties
spring.application.name=order-service
server.port=8082
```

#### Test

```bash
GET http://localhost:8082/api/orders
GET http://localhost:8082/api/orders/1
```

---

### 1.3 Organisation multi-modules (Recommandé)

Pour gérer plusieurs microservices dans IntelliJ :

#### Structure du projet parent

```
ecommerce-microservices/
├── pom.xml (parent)
├── product-service/
│   └── pom.xml
├── order-service/
│   └── pom.xml
└── eureka-server/
    └── pom.xml
```

#### pom.xml parent

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.ecommerce</groupId>
    <artifactId>ecommerce-microservices</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>

    <name>E-Commerce Microservices</name>

    <modules>
        <module>product-service</module>
        <module>order-service</module>
        <module>eureka-server</module>
        <module>api-gateway</module>
    </modules>

    <properties>
        <java.version>17</java.version>
        <spring.boot.version>3.2.0</spring.boot.version>
    </properties>
</project>
```

---

## 🔍 Phase 2 : Eureka Server - Service Discovery {#phase-2}

### Qu'est-ce qu'Eureka ?

**Analogie** : Eureka est un annuaire téléphonique pour microservices.
- Chaque service s'enregistre automatiquement
- Quand un service veut appeler un autre, il demande à Eureka son adresse

### 2.1 Créer Eureka Server

#### Création du projet

**Sur Spring Initializr** :
- **Artifact** : eureka-server
- **Dépendances** :
  - Eureka Server

#### Code : EurekaServerApplication.java

```java
package com.ecommerce.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer  // ⭐ Active Eureka Server
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

#### Configuration : application.properties

```properties
spring.application.name=eureka-server
server.port=8761

# Eureka ne doit pas s'enregistrer lui-même
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

#### Test

```bash
http://localhost:8761
```

Vous devriez voir le dashboard Eureka.

---

### 2.2 Enregistrer product-service dans Eureka

#### Ajoutez dans pom.xml

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>

<!-- Dans dependencyManagement -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>2023.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

#### Modifiez ProductServiceApplication.java

```java
package com.ecommerce.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient  // ⭐ Active l'enregistrement dans Eureka
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
```

#### Ajoutez dans application.properties

```properties
spring.application.name=product-service
server.port=8081

# Configuration Eureka Client
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.hostname=localhost
eureka.instance.prefer-ip-address=false
```

---

### 2.3 Enregistrer order-service dans Eureka

**Faites exactement la même chose** que pour product-service :

1. Ajoutez la dépendance Eureka Client dans pom.xml
2. Ajoutez `@EnableDiscoveryClient` dans OrderServiceApplication.java
3. Ajoutez la configuration Eureka dans application.properties

```properties
spring.application.name=order-service
server.port=8082

# Configuration Eureka Client
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.hostname=localhost
eureka.instance.prefer-ip-address=false
```

---

### 2.4 Test complet

#### Ordre de démarrage

1. **Eureka Server** (port 8761)
2. **Product Service** (port 8081)
3. **Order Service** (port 8082)

#### Vérification

Allez sur http://localhost:8761

Vous devriez voir dans "Instances currently registered with Eureka" :
- **PRODUCT-SERVICE** (1 instance)
- **ORDER-SERVICE** (1 instance)

---

## 🔗 Phase 3 : Feign Client - Communication entre services {#phase-3}

### Qu'est-ce que Feign ?

**Avant Feign** (code compliqué) :
```java
RestTemplate restTemplate = new RestTemplate();
String url = "http://localhost:8081/api/products/1";
Product product = restTemplate.getForObject(url, Product.class);
```

**Avec Feign** (simple) :
```java
@FeignClient(name = "product-service")
public interface ProductClient {
    @GetMapping("/api/products/{id}")
    Product getProductById(@PathVariable Long id);
}
```

Feign génère automatiquement l'implémentation !

---

### 3.1 Ajouter Feign dans order-service

#### Ajoutez dans pom.xml

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>

<!-- Optionnel mais recommandé -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
```

#### Activez Feign dans OrderServiceApplication.java

```java
package com.ecommerce.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients  // ⭐ Active Feign
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
```

---

### 3.2 Créer le modèle Product dans order-service

**Note** : Oui, on duplique le modèle. C'est normal en microservices (chaque service est indépendant).

#### Code : order-service/model/Product.java

```java
package com.ecommerce.orderservice.model;

public class Product {
    private Long id;
    private String name;
    private Double price;
    private Integer stock;

    public Product() {}  // Constructeur vide important pour Feign

    public Product(Long id, String name, Double price, Integer stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}
```

---

### 3.3 Créer le Feign Client

#### Créez : order-service/client/ProductClient.java

```java
package com.ecommerce.orderservice.client;

import com.ecommerce.orderservice.model.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")  // ⭐ Nom du service dans Eureka
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    Product getProductById(@PathVariable Long id);
}
```

**Explications** :
- `@FeignClient(name = "product-service")` : Feign cherche ce service dans Eureka
- Le chemin `/api/products/{id}` : l'endpoint du product-service
- Feign génère automatiquement l'implémentation !

---

### 3.4 Utiliser Feign dans OrderService

#### Modifiez OrderService.java

```java
package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.client.ProductClient;
import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    
    @Autowired
    private ProductClient productClient;  // ⭐ Injection du Feign Client
    
    private List<Order> orders = new ArrayList<>();
    private Long nextId = 1L;

    public OrderService() {
        orders.add(new Order(nextId++, 1L, "Ordinateur", 1, 999.99));
        orders.add(new Order(nextId++, 2L, "Souris", 2, 59.98));
    }

    public List<Order> getAllOrders() {
        return orders;
    }

    public Order getOrderById(Long id) {
        return orders.stream()
                .filter(o -> o.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // ⭐ NOUVELLE MÉTHODE : Créer une commande en récupérant le produit
    public Order createOrderFromProduct(Long productId, Integer quantity) {
        // Appel au product-service via Feign
        Product product = productClient.getProductById(productId);
        
        if (product == null) {
            throw new RuntimeException("Produit non trouvé !");
        }
        
        Double totalPrice = product.getPrice() * quantity;
        Order order = new Order(nextId++, productId, product.getName(), quantity, totalPrice);
        orders.add(order);
        return order;
    }
}
```

---

### 3.5 Ajouter l'endpoint dans OrderController

#### Modifiez OrderController.java

```java
package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    // ⭐ NOUVEAU : Créer une commande automatiquement
    @PostMapping("/create")
    public Order createOrder(@RequestParam Long productId, 
                            @RequestParam Integer quantity) {
        return orderService.createOrderFromProduct(productId, quantity);
    }
}
```

---

### 3.6 Configuration Feign (optionnelle mais recommandée)

#### Ajoutez dans application.properties de order-service

```properties
spring.application.name=order-service
server.port=8082

# Configuration Eureka Client
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.hostname=localhost
eureka.instance.prefer-ip-address=false

# Configuration Feign Timeout
feign.client.config.default.connectTimeout=5000
feign.client.config.default.readTimeout=5000
```

---

### 3.7 Test de la communication

#### Ordre de démarrage

1. Eureka Server
2. Product Service
3. Order Service

#### Testez

```bash
POST http://localhost:8082/api/orders/create?productId=1&quantity=2
```

**Résultat attendu** :
```json
{
  "id": 3,
  "productId": 1,
  "productName": "Ordinateur",
  "quantity": 2,
  "totalPrice": 1999.98,
  "orderDate": "2024-..."
}
```

#### Ce qui se passe en coulisses

1. Order-service reçoit la requête
2. OrderService appelle `productClient.getProductById(1)`
3. Feign demande à Eureka : "Où est product-service ?"
4. Eureka répond : "Sur localhost:8081"
5. Feign appelle : `http://localhost:8081/api/products/1`
6. Product-service répond avec les infos du produit
7. Order-service calcule le prix total et crée la commande

**Tout automatiquement ! 🚀**

---

## 🚪 Phase 4 : API Gateway - Point d'entrée unique {#phase-4}

### Qu'est-ce qu'un API Gateway ?

**Analogie de l'hôtel** :
- **Sans réceptionniste** : Les clients doivent connaître tous les numéros de chambres
- **Avec réceptionniste** : Une seule personne à l'accueil qui redirige

**L'API Gateway est le réceptionniste de vos microservices !**

### Situation avant Gateway

```
Client → http://localhost:8081/api/products  (product-service)
Client → http://localhost:8082/api/orders    (order-service)
```

Le client doit connaître tous les ports !

### Situation avec Gateway

```
Client → http://localhost:8080/api/products  → Gateway → product-service
Client → http://localhost:8080/api/orders    → Gateway → order-service
```

Une seule adresse pour tout !

---

### 4.1 Créer l'API Gateway

#### Création du projet

**Sur Spring Initializr** :
- **Artifact** : api-gateway
- **Dépendances** :
  - Gateway (Spring Cloud Gateway)
  - Eureka Discovery Client

#### Code : ApiGatewayApplication.java

```java
package com.ecommerce.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient  // ⭐ Pour s'enregistrer dans Eureka
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```

---

### 4.2 Configuration du Gateway

#### Créez application.yml (pas .properties)

**Important** : Supprimez `application.properties` et créez `application.yml`

```yaml
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        # Route pour product-service
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/products/**

        # Route pour order-service
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**

server:
  port: 8080

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    hostname: localhost
    prefer-ip-address: false
```

#### Explication des routes

```yaml
- id: product-service           # Nom unique de la route
  uri: lb://product-service     # lb = load balancer via Eureka
  predicates:
    - Path=/api/products/**     # Si l'URL commence par /api/products
```

**Exemple** :
```
Requête : GET http://localhost:8080/api/products/1

1. Gateway voit : /api/products/1
2. Route "product-service" est déclenchée
3. Gateway demande à Eureka : "Où est product-service ?"
4. Eureka répond : "localhost:8081"
5. Gateway appelle : http://localhost:8081/api/products/1
```

#### Note sur le Discovery Locator (optionnel)

Dans le fichier `application.yml` reel du projet, vous verrez peut-etre cette configuration supplementaire :

```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true              # Active la decouverte automatique via Eureka
          lower-case-service-id: true # URLs en minuscules
```

**Cette partie est OPTIONNELLE !** Elle permet d'acceder aux services directement par leur **nom Eureka** dans l'URL, par exemple :

```
http://localhost:8080/product-service/api/products
http://localhost:8080/order-service/api/orders
```

Au lieu de :

```
http://localhost:8080/api/products
http://localhost:8080/api/orders
```

**Vous n'en avez PAS BESOIN** car les routes specifiques definies dans `routes:` suffisent largement. Le discovery locator est surtout utile en phase de debug rapide ou quand on a beaucoup de services et qu'on ne veut pas definir chaque route manuellement. En production, les routes explicites sont preferables car elles offrent plus de controle.

> `lower-case-service-id: true` permet d'utiliser `product-service` en minuscules dans l'URL au lieu de `PRODUCT-SERVICE` (le nom en majuscules tel qu'enregistre dans Eureka).

---

### 4.3 Comment appeler les services via le Gateway (Important)

L'API Gateway agit comme un **proxy intelligent**. Le principe est simple : **chaque route du Gateway correspond a un mapping vers un microservice**, et le chemin de l'URL reste identique.

#### Le mecanisme en detail

Dans `application.yml`, chaque route est definie ainsi :

```yaml
- id: product-service
  uri: lb://product-service    # lb = load balancer via Eureka
  predicates:
    - Path=/api/products/**    # Pattern d'URL qui declenche cette route
```

Voici ce que chaque element signifie :

| Element | Role | Exemple |
|---------|------|---------|
| `id` | Nom unique de la route (pour les logs/debug) | `product-service` |
| `uri: lb://product-service` | Le service cible. `lb://` indique que le Gateway doit demander a Eureka l'adresse reelle du service, puis faire du load balancing si plusieurs instances existent | Eureka resout `product-service` → `localhost:8081` |
| `Path=/api/products/**` | Le predicate : si l'URL de la requete commence par `/api/products/`, cette route est activee. `**` signifie "n'importe quoi apres" | `/api/products`, `/api/products/1`, `/api/products/search?name=pc` |

#### La regle cle : le path est transmis tel quel

Quand le Gateway recoit une requete, il **transmet le meme chemin** au microservice cible. Cela fonctionne parce que les controllers des microservices utilisent exactement les memes chemins que ceux declares dans les predicates du Gateway.

**Exemple concret avec product-service :**

1. Le `ProductController` dans product-service a l'annotation `@RequestMapping("/api/products")`
2. Le Gateway a le predicate `Path=/api/products/**`
3. Donc quand vous appelez `http://localhost:8080/api/products`, le Gateway :
   - Detecte que `/api/products` correspond au predicate `/api/products/**`
   - Demande a Eureka : "Ou est `product-service` ?"
   - Eureka repond : `localhost:8081`
   - Le Gateway transmet la requete a `http://localhost:8081/api/products` (meme chemin)

#### Tableau recapitulatif : URLs via le Gateway vs acces direct

| Action | Acces direct (sans Gateway) | Via le Gateway (port 8080) |
|--------|---------------------------|---------------------------|
| Lister tous les produits | `GET http://localhost:8081/api/products` | `GET http://localhost:8080/api/products` |
| Obtenir un produit par ID | `GET http://localhost:8081/api/products/1` | `GET http://localhost:8080/api/products/1` |
| Lister toutes les commandes | `GET http://localhost:8082/api/orders` | `GET http://localhost:8080/api/orders` |
| Obtenir une commande par ID | `GET http://localhost:8082/api/orders/1` | `GET http://localhost:8080/api/orders/1` |
| Creer une commande | `POST http://localhost:8082/api/orders/create?productId=1&quantity=2` | `POST http://localhost:8080/api/orders/create?productId=1&quantity=2` |

**En resume** : il suffit de remplacer `localhost:<port_du_service>` par `localhost:8080` (le port du Gateway). Le reste de l'URL ne change pas.

#### Pourquoi ca marche ?

La correspondance est possible grace a la convention de nommage :

```
Controller du service         →  @RequestMapping("/api/products")
Predicate du Gateway          →  Path=/api/products/**
```

Les deux utilisent le meme prefixe `/api/products`. Si demain vous ajoutez un nouveau service (par ex. `user-service` avec `@RequestMapping("/api/users")`), il suffit d'ajouter une route dans le Gateway :

```yaml
- id: user-service
  uri: lb://user-service
  predicates:
    - Path=/api/users/**
```

Et vous pourrez appeler `http://localhost:8080/api/users` sans connaitre le port reel du service.

#### Le role du Load Balancer (`lb://`)

Le prefixe `lb://` dans l'URI est crucial. Il signifie que le Gateway ne va **pas** appeler une adresse en dur, mais va :

1. Demander a **Eureka** toutes les instances disponibles du service
2. Utiliser un **load balancer** (Round Robin par defaut) pour choisir une instance
3. Router la requete vers cette instance

Cela permet de lancer **plusieurs instances** du meme service (par ex. 3 instances de `product-service` sur les ports 8081, 8083, 8084) et le Gateway distribuera automatiquement les requetes entre elles.

---

### 4.4 Verifier le pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>com.ecommerce</groupId>
    <artifactId>api-gateway</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    <name>api-gateway</name>
    
    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
    </properties>
    
    <dependencies>
        <!-- Spring Cloud Gateway -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
        
        <!-- Eureka Client -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
    </dependencies>
    
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>2023.0.0</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

### 4.5 Test complet

#### Ordre de démarrage

1. **Eureka Server** (port 8761)
2. **Product Service** (port 8081)
3. **Order Service** (port 8082)
4. **API Gateway** (port 8080)

#### Vérification Eureka

http://localhost:8761 → Vous devez voir **4 services** :
- EUREKA-SERVER
- PRODUCT-SERVICE
- ORDER-SERVICE
- API-GATEWAY

#### Testez via le Gateway

**Nouveaux endpoints (via Gateway)** :
```bash
# Products
GET http://localhost:8080/api/products
GET http://localhost:8080/api/products/1

# Orders
GET http://localhost:8080/api/orders
POST http://localhost:8080/api/orders/create?productId=1&quantity=2
```

**Les anciens endpoints fonctionnent toujours** (accès direct) :
```bash
GET http://localhost:8081/api/products
GET http://localhost:8082/api/orders
```

---

## ⚙️ Phase 5 : Config Server - Centralisation de la configuration {#phase-5}

### Pourquoi un Config Server ?

#### Probleme actuel

Les memes configurations sont repetees dans chaque service :

**product-service**, **order-service**, **api-gateway** ont tous :
```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.hostname=localhost
eureka.instance.prefer-ip-address=false
```

**Problemes** :
1. Configuration repetee dans chaque service
2. Pour changer l'adresse Eureka, il faut modifier 4 fichiers
3. Besoin de redeployer tous les services pour une simple modification
4. Risque d'oublier un service lors des modifications

#### Solution : Config Server

**Analogie** : Le Config Server est comme un serveur de fichiers partage.

- Toutes les configurations sont dans **UN SEUL endroit** (depot Git)
- Les services demandent leur configuration au demarrage
- On peut changer la config sans redeployer (avec refresh)

```
Config Server (port 8888)
    |
config-repo/
├── product-service.properties   (config specifique)
├── order-service.properties     (config specifique)
├── api-gateway.yaml             (config specifique)
└── application.properties       (config commune a tous)

Tous les services --> Demandent leur config au Config Server au demarrage
```

---

### 5.1 Creer le Config Server

#### Creation du projet

**Sur Spring Initializr** :
- **Artifact** : config-server
- **Package name** : com.ecommerce.configserver
- **Dependances** :
  - Config Server
  - Eureka Discovery Client

#### Code : ConfigServerApplication.java

```java
package com.ecommerce.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer  // Active le Config Server
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

#### Configuration : application.yaml

```yaml
spring:
  application:
    name: config-server
  profiles:
    active: native
  cloud:
    config:
      server:
        git:
          uri: file:///${user.dir}/../  # racine du projet global avec .git
          search-paths: config-repo     # sous-dossier contenant les configs
          #ou
        #native:
          # chemin vers le dossier config-repo
          #search-locations: file:///${user.dir}/../config-repo/

server:
  port: 8888

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    hostname: localhost
    prefer-ip-address: false
```

**Point important sur la configuration Git** :

Ce projet est un **monorepo** (un seul depot Git contenant tous les microservices). L'arborescence est :

```
ecommerce-microservices/        <-- .git est ici (racine du repo)
├── config-server/
├── config-repo/                <-- fichiers de configuration centralises
├── eureka-server/
├── product-service/
├── order-service/
└── api-gateway/
```

C'est pourquoi la configuration utilise :
- `uri: file:///${user.dir}/../` : pointe vers le **repertoire parent** du config-server, c'est-a-dire la racine du projet qui contient le `.git`
- `search-paths: config-repo` : indique de chercher les configs dans le sous-dossier `config-repo/`

> `${user.dir}` correspond au repertoire de travail du processus Java, soit `config-server/`. En remontant d'un niveau (`../`), on arrive a la racine `ecommerce-microservices/` qui est le vrai depot Git.

---

### 5.2 Creer le depot de configurations

Le dossier `config-repo/` a la racine du projet contient les fichiers de configuration centralises :

#### 1. application.properties (configuration commune a tous les services)

```properties
# Configuration Eureka commune
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.hostname=localhost
eureka.instance.prefer-ip-address=false

# Configuration Feign commune
feign.client.config.default.connectTimeout=5000
feign.client.config.default.readTimeout=5000
```

#### 2. product-service.properties

```properties
# Configuration specifique au product-service
server.port=8081
spring.application.name=product-service

# Message personnalise (pour tester)
app.message=Configuration chargee depuis Config Server pour Product Service
```

#### 3. order-service.properties

```properties
# Configuration specifique au order-service
server.port=8082
spring.application.name=order-service
```

#### 4. api-gateway.yaml

```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/products/**

        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
```

> **Rappel** : Le Config Server necessite que le dossier soit dans un **depot Git**. Comme `config-repo/` fait partie du monorepo `ecommerce-microservices/`, c'est deja le cas.

---

### 5.3 Modifier les services pour utiliser le Config Server

#### 1. Ajouter les dependances dans pom.xml (pour chaque service)

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bootstrap</artifactId>
</dependency>
```

#### 2. Creer bootstrap.properties (lu AVANT application.properties au demarrage)

**product-service** : `src/main/resources/bootstrap.properties`
```properties
spring.application.name=product-service
spring.cloud.config.uri=http://localhost:8888
spring.profiles.active=default
```

**order-service** : `src/main/resources/bootstrap.properties`
```properties
spring.application.name=order-service
spring.cloud.config.uri=http://localhost:8888
spring.profiles.active=default
```

**api-gateway** : `src/main/resources/bootstrap.yaml`
```yaml
spring:
  application:
    name: api-gateway
  cloud:
    config:
      uri: http://localhost:8888
  profiles:
    active: default
```

#### 3. Simplifier les application.properties des services

Les proprietes qui sont desormais dans le Config Server (Eureka, ports, etc.) peuvent etre supprimees des `application.properties` locaux. Le service les recevra du Config Server au demarrage.

---

### 5.4 Test du Config Server

#### Ordre de demarrage (IMPORTANT - nouvel ordre !)

1. **Config Server** (port 8888) - NOUVEAU - **avant tout le reste**
2. **Eureka Server** (port 8761)
3. **Product Service** (port 8081)
4. **Order Service** (port 8082)
5. **API Gateway** (port 8080)

#### Test 1 : Verifier le Config Server directement

Avant de demarrer les autres services :
```bash
# Config de product-service
GET http://localhost:8888/product-service/default

# Config de order-service
GET http://localhost:8888/order-service/default

# Config commune
GET http://localhost:8888/application/default
```

Vous devriez voir les configurations au format JSON.

#### Test 2 : Verifier les logs au demarrage des services

Au demarrage de product-service, vous devriez voir dans les logs :
```
Fetching config from server at : http://localhost:8888
Located environment: name=product-service, profiles=[default]
```

#### Test 3 : Endpoint de test (optionnel)

Si vous ajoutez dans `ProductController` :
```java
@Value("${app.message:No config}")
private String configMessage;

@GetMapping("/config")
public String getConfig() {
    return configMessage;
}
```

Alors `GET http://localhost:8081/api/products/config` retournera :
```
Configuration chargee depuis Config Server pour Product Service
```

---

### 5.5 Avantages du Config Server

| Avant (sans Config Server) | Apres (avec Config Server) |
|----------------------------|----------------------------|
| Modifier 4 fichiers pour changer l'URL Eureka | Modifier 1 seul fichier dans `config-repo/` |
| Redeployer tous les services | Git commit + refresh ou redemarrage |
| Risque d'oubli d'un service | Configuration centralisee et coherente |
| Duplication partout | Un seul `application.properties` commun |

---

## 🗄️ Phase 6 : Base de données - PostgreSQL avec Spring Data JPA {#phase-6}

### Pourquoi une base de données ?

Jusqu'ici, nos services stockaient les données dans des `ArrayList` en mémoire. A chaque redemarrage, les donnees etaient perdues. Maintenant, on passe a PostgreSQL pour une persistance reelle.

**Principe fondamental des microservices** : Chaque service a sa propre base de donnees.

```
product-service  →  Base "products_db"   (table : products)
order-service    →  Base "orders_db"     (table : orders)
```

### 6.1 Prerequis

- **Base de donnees** : PostgreSQL
- **Outil de gestion** : pgAdmin

### 6.2 Creer les bases de donnees

Dans pgAdmin (Query Tool) :

```sql
CREATE DATABASE products_db;
CREATE DATABASE orders_db;
```

### 6.3 Dependances ajoutees (product-service et order-service)

Dans le `pom.xml` de chaque service :

```xml
<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- PostgreSQL Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### 6.4 Configuration dans config-repo

#### product-service.properties

```properties
# Serveur
server.port=8081
spring.application.name=product-service

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/products_db
spring.datasource.username=postgres
spring.datasource.password=TON_MOT_DE_PASSE
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# Message test
app.message=Configuration chargee depuis Config Server pour Product Service
```

#### order-service.properties

```properties
# Serveur
server.port=8082
spring.application.name=order-service

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/orders_db
spring.datasource.username=postgres
spring.datasource.password=TON_MOT_DE_PASSE
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true
```

### 6.5 Entites JPA (modeles mis a jour)

Les classes `Product` et `Order` sont maintenant des entites JPA avec les annotations `@Entity`, `@Table`, `@Id`, `@GeneratedValue` et utilisent Lombok (`@Data`, `@AllArgsConstructor`, `@NoArgsConstructor`).

> Voir les modeles mis a jour dans la Phase 1 ci-dessus.

### 6.6 Repositories

#### ProductRepository.java

```java
package com.ecommerce.productservice.repository;

import com.ecommerce.productservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Spring Data JPA genere automatiquement toutes les methodes CRUD !
    // findAll(), findById(), save(), deleteById() ... tout est deja la
}
```

#### OrderRepository.java

```java
package com.ecommerce.orderservice.repository;

import com.ecommerce.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
```

### 6.7 Services mis a jour

#### ProductService.java

```java
package com.ecommerce.productservice.service;

import com.ecommerce.productservice.model.Product;
import com.ecommerce.productservice.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
```

#### OrderService.java

```java
package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.client.ProductClient;
import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.model.Product;
import com.ecommerce.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductClient productClient;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public Order createOrderFromProduct(Long productId, Integer quantity) {
        Product product = productClient.getProductById(productId);

        if (product == null) {
            throw new RuntimeException("Produit non trouve !");
        }

        Double totalPrice = product.getPrice() * quantity;
        Order order = new Order(null, productId, product.getName(), quantity, totalPrice);

        return orderRepository.save(order);
    }
}
```

### 6.8 Controllers mis a jour

#### ProductController.java

```java
package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.model.Product;
import com.ecommerce.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product saved = productService.createProduct(product);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
```

#### OrderController.java

```java
package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }

    @PostMapping("/create")
    public ResponseEntity<Order> createOrder(@RequestParam Long productId,
                                             @RequestParam Integer quantity) {
        Order order = orderService.createOrderFromProduct(productId, quantity);
        return ResponseEntity.ok(order);
    }
}
```

### 6.9 Structure mise a jour des services

```
product-service/
└── src/main/java/com/ecommerce/productservice/
    ├── ProductServiceApplication.java
    ├── model/
    │   └── Product.java          ← Entite JPA
    ├── repository/
    │   └── ProductRepository.java ← NOUVEAU
    ├── controller/
    │   └── ProductController.java ← CRUD complet
    └── service/
        └── ProductService.java    ← Utilise le Repository

order-service/
└── src/main/java/com/ecommerce/orderservice/
    ├── OrderServiceApplication.java
    ├── model/
    │   ├── Order.java             ← Entite JPA
    │   └── Product.java           ← DTO pour Feign
    ├── repository/
    │   └── OrderRepository.java   ← NOUVEAU
    ├── client/
    │   └── ProductClient.java
    ├── controller/
    │   └── OrderController.java
    └── service/
        └── OrderService.java      ← Utilise le Repository + Feign
```

### 6.10 Endpoints disponibles

| Methode | URL | Description |
|---------|-----|-------------|
| GET | `/api/products` | Lister tous les produits |
| GET | `/api/products/{id}` | Un produit par ID |
| POST | `/api/products` | Creer un produit |
| DELETE | `/api/products/{id}` | Supprimer un produit |
| GET | `/api/orders` | Lister toutes les commandes |
| GET | `/api/orders/{id}` | Une commande par ID |
| POST | `/api/orders/create?productId=1&quantity=2` | Creer une commande |

### 6.11 Test

```bash
# Creer un produit
POST http://localhost:8081/api/products
Content-Type: application/json
{
    "name": "Ordinateur",
    "price": 999.99,
    "stock": 10
}

# Lister les produits
GET http://localhost:8081/api/products

# Creer une commande (via API Gateway)
POST http://localhost:8080/api/orders/create?productId=1&quantity=2

# Lister les commandes
GET http://localhost:8080/api/orders
```

### 6.12 Ce qui a change

| Avant (Phase 1-5) | Apres (Phase 6) |
|--------------------|------------------|
| `ArrayList` en memoire | PostgreSQL avec Spring Data JPA |
| Donnees perdues au redemarrage | Donnees persistees en base |
| Pas de CRUD complet | GET, POST, DELETE fonctionnels |
| Pas de Repository | `JpaRepository` genere le CRUD automatiquement |
| Getters/Setters manuels | Lombok (`@Data`) simplifie le code |

### 6.13 Verification dans pgAdmin

Apres les tests, verifier dans pgAdmin :
- `products_db` → Schemas → Tables → `products` (avec les donnees)
- `orders_db` → Schemas → Tables → `orders` (avec les commandes)

JPA cree automatiquement les tables grace a `spring.jpa.hibernate.ddl-auto=update`.

### 6.14 Ordre de demarrage

```
1. Config Server (8888)
2. Eureka Server (8761)
3. Product Service (8081)
4. Order Service (8082)
5. API Gateway (8080)
```

Au demarrage de product-service et order-service, les logs doivent afficher :
```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
```
Cela confirme que la connexion a PostgreSQL est etablie.

---

## 🏗️ Architecture finale {#architecture-finale}

### Schéma de l'architecture

```
┌─────────────────────────────────────────────────────┐
│                     Client                          │
│              (Postman, Browser, App)                │
└──────────────────────┬──────────────────────────────┘
                       │
                       │ HTTP (port 8080)
                       ▼
┌─────────────────────────────────────────────────────┐
│                  API Gateway                        │
│              (Point d'entrée unique)                │
│                   Port 8080                         │
└───────────┬─────────────────────────┬───────────────┘
            │                         │
            │                         │
            ▼                         ▼
┌───────────────────────┐  ┌───────────────────────┐
│   Product Service     │  │    Order Service      │
│      Port 8081        │  │      Port 8082        │
└───────────┬───────────┘  └───────┬───────────────┘
            │                      │
            │                      │ Feign Client
            │                      └───────┐
            │                              │
            └──────────────┬───────────────┘
                           │
                           ▼
            ┌──────────────────────────────┐
            │      Eureka Server           │
            │   (Service Discovery)        │
            │        Port 8761             │
            └──────────────────────────────┘

            ┌──────────────────────────────┐
            │      Config Server           │
            │ (Configuration centralisée)  │
            │        Port 8888             │
            │              │               │
            │       config-repo/           │
            │  (application.properties,    │
            │   product-service.properties,│
            │   order-service.properties,  │
            │   api-gateway.yaml)          │
            └──────────────────────────────┘
     Tous les services récupèrent leur config
              au démarrage via Config Server
```

### Flux d'une requête

**Exemple** : Créer une commande

```
1. Client → POST http://localhost:8080/api/orders/create?productId=1&quantity=2

2. API Gateway reçoit la requête
   - Vérifie la route : /api/orders/** → order-service

3. Gateway demande à Eureka : "Où est order-service ?"
   - Eureka répond : "localhost:8082"

4. Gateway route vers : http://localhost:8082/api/orders/create

5. Order Service reçoit la requête
   - Appelle productClient.getProductById(1)

6. Feign Client demande à Eureka : "Où est product-service ?"
   - Eureka répond : "localhost:8081"

7. Feign appelle : http://localhost:8081/api/products/1

8. Product Service répond avec les infos du produit

9. Order Service crée la commande avec les vraies données

10. Order Service répond au Gateway

11. Gateway répond au Client

✅ Commande créée avec succès !
```

---

## 🏃 Guide de démarrage rapide

### Ordre de démarrage (IMPORTANT)

1. **Config Server** (8888) - Toujours en premier
2. **Eureka Server** (8761)
3. **Product Service** (8081)
4. **Order Service** (8082)
5. **API Gateway** (8080)

### Attendre l'enregistrement

Après chaque démarrage, **attendez 30 secondes** que les services s'enregistrent dans Eureka.

### Vérification

1. Eureka Dashboard : http://localhost:8761
2. Tous les services doivent apparaître en "UP"

### Tests de base

```bash
# Via Gateway
GET http://localhost:8080/api/products
GET http://localhost:8080/api/orders
POST http://localhost:8080/api/orders/create?productId=1&quantity=2

# Accès direct (pour debug)
GET http://localhost:8081/api/products
GET http://localhost:8082/api/orders
```

---

## 🔧 Troubleshooting {#troubleshooting}

### Problème : Services ne s'enregistrent pas dans Eureka

#### Symptômes
- Dashboard Eureka vide
- Services n'apparaissent pas

#### Solutions

1. **Vérifiez l'ordre de démarrage** : Eureka doit démarrer en premier

2. **Attendez 30 secondes** après chaque démarrage

3. **Vérifiez les propriétés Eureka** :
```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.hostname=localhost
eureka.instance.prefer-ip-address=false
```

4. **Vérifiez les annotations** :
```java
@EnableDiscoveryClient  // Doit être présent
```

5. **Vérifiez la dépendance** dans pom.xml :
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

---

### Problème : Feign Timeout / Connect timed out

#### Symptômes
```
feign.RetryableException: Connect timed out executing GET http://product-service/api/products/1
```

#### Causes possibles

1. **Services enregistrés avec des adresses différentes**

**Solution** : Forcer localhost dans tous les services

```properties
eureka.instance.hostname=localhost
eureka.instance.prefer-ip-address=false
```

2. **LoadBalancer manquant** (rare mais possible)

**Solution** : Ajouter dans pom.xml de order-service
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
```

3. **Timeout trop court**

**Solution** : Augmenter le timeout dans application.properties
```properties
feign.client.config.default.connectTimeout=5000
feign.client.config.default.readTimeout=5000
```

4. **Product-service pas démarré**

**Solution** : Vérifier que product-service tourne bien sur le port 8081
```bash
GET http://localhost:8081/api/products
```

---

### Problème : Gateway ne route pas correctement

#### Symptômes
- 404 Not Found via le Gateway
- Les accès directs fonctionnent

#### Solutions

1. **Vérifiez les routes** dans application.yml :
```yaml
routes:
  - id: product-service
    uri: lb://product-service  # lb = load balancer
    predicates:
      - Path=/api/products/**
```

2. **Vérifiez que Gateway est enregistré dans Eureka** :
   - http://localhost:8761 → API-GATEWAY doit apparaître

3. **Redémarrez dans l'ordre** :
   - Eureka → Services → Gateway

4. **Vérifiez le port du Gateway** :
```yaml
server:
  port: 8080
```

---

### Problème : Maven ne trouve pas les dépendances

#### Symptômes
```
Could not find artifact org.springframework.cloud:spring-cloud-starter-gateway
```

#### Solutions

1. **Vérifiez la version Spring Boot** dans pom.xml :
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>  <!-- Pas 4.x ! -->
</parent>
```

2. **Vérifiez dependencyManagement** :
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>2023.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

3. **Nettoyage Maven** :
```bash
mvn clean install
```

4. **Reload Maven** dans IntelliJ :
   - Clic droit sur le projet → Maven → Reload Project

---

### Problème : Port déjà utilisé

#### Symptômes
```
Port 8080 was already in use
```

#### Solutions

1. **Changez le port** dans application.properties :
```properties
server.port=8085
```

2. **Tuez le processus** qui utilise le port :

**Windows** :
```bash
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

**Linux/Mac** :
```bash
lsof -i :8080
kill -9 <PID>
```

---

## 📋 Checklist de vérification

### Avant de démarrer

- [ ] Tous les pom.xml ont les bonnes dépendances
- [ ] Toutes les annotations sont présentes (@EnableDiscoveryClient, @EnableFeignClients, @EnableEurekaServer)
- [ ] Les application.properties/yml sont correctement configurés
- [ ] Les ports sont différents (8761, 8080, 8081, 8082)

### Au démarrage

- [ ] Config Server demarre en premier (port 8888)
- [ ] Verifier : `GET http://localhost:8888/product-service/default` retourne du JSON
- [ ] Eureka Server demarre ensuite (port 8761)
- [ ] Attendre 10 secondes
- [ ] Démarrer product-service
- [ ] Attendre 30 secondes
- [ ] Vérifier sur http://localhost:8761 que product-service apparaît
- [ ] Démarrer order-service
- [ ] Attendre 30 secondes
- [ ] Démarrer api-gateway
- [ ] Attendre 30 secondes
- [ ] Vérifier que tous les services sont "UP" dans Eureka

### Tests

- [ ] http://localhost:8761 → Dashboard Eureka accessible
- [ ] http://localhost:8081/api/products → Liste des produits
- [ ] http://localhost:8082/api/orders → Liste des commandes
- [ ] http://localhost:8080/api/products → Via Gateway
- [ ] POST http://localhost:8080/api/orders/create?productId=1&quantity=2 → Création de commande

---

## 🎓 Concepts clés à retenir

### Microservices
- Petites applications indépendantes
- Chacune a sa responsabilité
- Peuvent être déployées séparément
- Communiquent via HTTP/REST

### Service Discovery (Eureka)
- Annuaire centralisé des services
- Enregistrement automatique
- Découverte dynamique
- Pas besoin de connaître les adresses en dur

### Feign Client
- Simplifie les appels HTTP entre services
- Interface Java → implémentation automatique
- Intégration avec Eureka
- Gestion automatique du load balancing

### API Gateway
- Point d'entrée unique
- Routage intelligent
- Simplifie l'accès pour les clients
- Permet d'ajouter sécurité, logs, rate limiting

### Config Server
- Centralisation de toutes les configurations
- Un seul endroit pour les propriétés communes (Eureka, Feign, etc.)
- Les services récupèrent leur config au démarrage via `bootstrap.properties`
- Modification sans redéploiement (avec refresh)

---

## 📚 Ressources supplémentaires

### Documentation officielle
- Spring Boot : https://spring.io/projects/spring-boot
- Spring Cloud : https://spring.io/projects/spring-cloud
- Netflix Eureka : https://github.com/Netflix/eureka
- OpenFeign : https://github.com/OpenFeign/feign
- Spring Cloud Gateway : https://spring.io/projects/spring-cloud-gateway

### Table de compatibilité

| Spring Boot | Spring Cloud | Java |
|-------------|--------------|------|
| 3.2.x       | 2023.0.x     | 17+  |
| 3.1.x       | 2022.0.x     | 17+  |
| 2.7.x       | 2021.0.x     | 11+  |

---

## 🚀 Prochaines étapes

Maintenant que vous maîtrisez les bases, vous pouvez explorer :

### 1. Base de données
- Ajouter PostgreSQL ou MySQL
- Chaque service a sa propre base
- Spring Data JPA

### 2. ~~Configuration centralisée~~ (FAIT - Phase 5)
- ~~Spring Cloud Config Server~~
- ~~Externaliser toutes les configurations~~
- ~~Changer la config sans redéployer~~

### 3. Résilience
- Circuit Breaker avec Resilience4j
- Gérer les pannes de services
- Fallback methods
- Retry automatique

### 4. Sécurité
- JWT Authentication
- OAuth2
- Sécuriser l'API Gateway
- Authorization entre services

### 5. Observabilité
- Spring Boot Actuator
- Logs centralisés (ELK Stack)
- Monitoring (Prometheus + Grafana)
- Distributed Tracing (Zipkin)

### 6. Conteneurisation
- Docker pour chaque service
- Docker Compose pour tout orchestrer
- Kubernetes pour la production

---

## 📝 Notes importantes

### Bonnes pratiques

1. **Toujours demarrer Config Server en premier, puis Eureka**
2. **Attendre l'enregistrement** (30 secondes) entre chaque service
3. **Utiliser localhost** en développement (pas d'IP)
4. **Un service = un port** (pas de conflit)
5. **Tester les services individuellement** avant de les connecter
6. **Vérifier Eureka Dashboard** régulièrement

### Pièges à éviter

1. ❌ Demarrer les services avant Config Server et Eureka
2. ❌ Ne pas attendre l'enregistrement
3. ❌ Mélanger les versions Spring Boot/Cloud
4. ❌ Oublier @EnableDiscoveryClient
5. ❌ Utiliser des ports déjà occupés
6. ❌ Ne pas vérifier que les services sont "UP" dans Eureka

---

## 🎯 Résumé ultra-rapide

```bash
# 1. Créer les projets
product-service (8081) + order-service (8082)

# 2. Ajouter Eureka
eureka-server (8761)
+ @EnableEurekaServer
+ Les services s'enregistrent avec @EnableDiscoveryClient

# 3. Communication Feign
order-service → @EnableFeignClients
+ ProductClient interface
+ Appelle product-service automatiquement

# 4. API Gateway
api-gateway (8080)
+ Routes dans application.yml
+ Point d'entrée unique

# 5. Config Server
config-server (8888) → Centralise toutes les configs

# 6. Base de donnees
PostgreSQL + Spring Data JPA
products_db (product-service) + orders_db (order-service)
ArrayList → JpaRepository (persistance reelle)

# 7. Demarrage
Config Server → Eureka → Product → Order → Gateway

# 8. Test
http://localhost:8080/api/products ✅
```

---

**Félicitations ! Vous maîtrisez maintenant les microservices Spring Boot ! 🎉**

---

*Guide créé le 2024 - Version 1.0*

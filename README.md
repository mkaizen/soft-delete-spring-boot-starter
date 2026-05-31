# soft-delete-spring-boot-starter

A zero-friction soft delete library for Spring Boot + JPA / Hibernate.

Drop it in, extend one class, swap one interface — and your entities gain
automatic soft-delete, audit fields, restore support, and hard-delete escape
hatches without touching a single SQL file.

[![Build](https://img.shields.io/github/actions/workflow/status/mkaizen/soft-delete-spring-boot-starter/ci.yml?branch=main)](https://github.com/mkaizen/soft-delete-spring-boot-starter/actions)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.softdelete/soft-delete-spring-boot-starter)](https://central.sonatype.com/artifact/io.github.mkaizen/soft-delete-spring-boot-starter)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## Features

| Feature | Description |
|---|---|
| `@SoftDeletable` | Marker annotation for your entities |
| Audit fields | `deleted`, `deletedAt`, `deletedBy` columns — auto-populated |
| Filtered reads | `findAll()`, `findById()`, `count()` silently exclude deleted rows |
| Restore | `repository.restore(id)` clears all delete state |
| Hard delete | `hardDeleteById(id)` when you genuinely need `DELETE FROM` |
| Custom auditor | Plug in Spring Security or any user-resolution strategy |

---

## Requirements

- Java 17+
- Spring Boot 3.x
- Any JPA-compatible database (tested with H2, PostgreSQL, MySQL)

---

## Installation

```xml
<dependency>
    <groupId>io.github.softdelete</groupId>
    <artifactId>soft-delete-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

---

## Quick Start

### 1. Enable soft delete

Add `@EnableSoftDelete` to your application class:

```java
@SpringBootApplication
@EnableSoftDelete
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

> **Already using `@EnableJpaRepositories`?** Add
> `repositoryFactoryBeanClass = SoftDeleteRepositoryFactoryBean.class` to it
> instead of using `@EnableSoftDelete`.

### 2. Extend `SoftDeletableEntity`

```java
@Entity
@Table(name = "products")
@SoftDeletable
public class Product extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private BigDecimal price;

    // constructors, getters, setters…
}
```

Three columns are added automatically:

| Column | Type | Notes |
|---|---|---|
| `deleted` | `boolean` | `false` for live records |
| `deleted_at` | `timestamp` | Set at soft-delete time |
| `deleted_by` | `varchar(255)` | Principal name at soft-delete time |

### 3. Extend `SoftDeleteRepository`

```java
public interface ProductRepository
        extends SoftDeleteRepository<Product, Long> {

    // Derived queries work as normal — deleted rows are excluded:
    List<Product> findByPriceLessThan(BigDecimal price);
}
```

### 4. Use it

```java
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository products;

    // Soft-delete — sets deleted=true, deletedAt, deletedBy
    public void remove(Long id) {
        products.softDeleteById(id);
    }

    // Restore — clears all delete state
    public void restore(Long id) {
        products.restore(id);
    }

    // Standard reads silently exclude soft-deleted rows
    public List<Product> listActive() {
        return products.findAll();
    }

    // Audit / admin queries
    public List<Product> listDeleted() {
        return products.findAllDeleted();
    }
}
```

---

## Custom Auditor (Spring Security example)

Override the default `"system"` value by exposing a `SoftDeleteAuditorProvider` bean:

```java
@Bean
public SoftDeleteAuditorProvider softDeleteAuditorProvider() {
    return () -> {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated())
            ? auth.getName()
            : "anonymous";
    };
}
```

---

## API Reference

### `SoftDeleteRepository<T, ID>`

| Method | Description |
|---|---|
| `softDeleteById(id)` | Soft-deletes by id, populates audit fields |
| `softDelete(entity)` | Soft-deletes a managed entity |
| `softDeleteAll(entities)` | Bulk soft-delete |
| `restore(id)` → `Optional<T>` | Restores a soft-deleted entity |
| `restore(entity)` | Restores a managed entity |
| `findByIdIncludingDeleted(id)` | Finds by id regardless of deleted state |
| `findAllIncludingDeleted()` | All rows including deleted |
| `findAllDeleted()` | Only soft-deleted rows |
| `countDeleted()` | Count of soft-deleted rows |
| `hardDeleteById(id)` | Issues a real `DELETE` — permanent |
| `hardDelete(entity)` | Issues a real `DELETE` — permanent |

Standard `JpaRepository` methods (`findAll`, `findById`, `count`, `delete`,
`deleteById`, `existsById`) all respect soft delete — they exclude deleted rows
and redirect deletes to `softDelete*`.

---

## How It Works

Standard read operations in `SoftDeleteRepositoryImpl` append
`WHERE e.deleted = false` via JPQL before execution. The `delete` / `deleteById`
methods are overridden to call `softDelete` instead of issuing a SQL `DELETE`.

No Hibernate filters, no `@SQLRestriction`, no magic — just predictable JPQL.
This means the filter applies to all repository-layer queries; custom `@Query`
annotations or `EntityManager` calls written outside the repository will bypass
it (intentionally, for full flexibility).

---

## Limitations & Roadmap

- **Composite keys** are not supported in v0.1. Single `@Id` field only.
- **Derived queries** (e.g. `findByUsername`) are generated by Spring Data and
  do **not** automatically filter deleted rows. Workaround: add
  `And DeletedFalse` to the method name, e.g. `findByUsernameAndDeletedFalse`.
- Planned for v0.2: optional `@SQLRestriction` mode for Hibernate-level filtering
  that also covers derived queries.

---

## Contributing

Pull requests are welcome. Run `mvn verify` to execute the full test suite before
opening a PR. Please add a test for any new behaviour.

---

## License

[MIT](LICENSE)

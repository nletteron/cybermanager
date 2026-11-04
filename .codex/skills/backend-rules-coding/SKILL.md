---
name: backend-rules-coding
description: Java backend coding rules for CyberManager. Use when creating, editing, reviewing, or formatting backend Java/Spring code, including classes, records, enums, interfaces, Spring configuration, services, use cases, controllers, adapters, and any method body where return placement must be checked.
---

# Backend Rules Coding

## Overview

Apply CyberManager backend Java coding conventions while editing code. These rules complement DDD/package rules and must be checked before delivery of backend Java changes.

## Spacing Rules

- Use one blank line between methods and constructors.
- Never keep a Java declaration or block body inline after an opening brace; after `{`, continue on the next line.
- Use one blank line before the final closing brace of a class, record, enum, or interface when it contains members.
- Apply the same spacing rules inside interfaces, including between method declarations.
- Add one blank line between the class, record, enum, or interface opening brace and the first field, constant, constructor, or method declaration.
- Do not add blank lines between an annotation and the declaration it annotates.
- Prefer a truly empty line; do not insert spaces or tabs on blank lines.

## Return Rules

- Use exactly one `return` statement per method when the method returns a value.
- Refactor early returns into a local result variable and return it at the end of the method.
- For `void` methods, avoid `return;` unless there is no clean alternative; prefer condition blocks.
- Do not use multiple guard-clause returns.
- Do not hide extra returns inside lambdas, anonymous classes, or nested helpers inside the same method to bypass this rule.

## Type Structure Rules

- Do not declare a class, record, enum, or interface inside another class, record, enum, or interface.
- Keep one Java type per source file: one file contains one class, record, enum, or interface.
- Keep one intention per class: a use case class represents one precise use case and exposes exactly one `execute` method.
- Do not group several use case intentions in a shared `*UseCaseSupport` class.
- Avoid `upsert` commands and use cases; model explicit CRUD intentions such as create, update, delete, read, get, or search.
- Extract helper records/classes into their own files in the same package when they are only used locally.
- Use a public type only when it is consumed outside its package; otherwise prefer package-private visibility.

## Class Example

Use this layout:

```java
@Configuration
public class ApiCorsConfiguration {

    @Bean
    CorsFilter corsFilter() {
        var configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return new CorsFilter(source);
    }

}
```

For several methods:

```java
public class CustomerService {

    CustomerView create(CreateCustomerCommand command) {
        return mapper.toView(repository.save(Customer.create(command.name())));
    }

    CustomerView update(UpdateCustomerCommand command) {
        return mapper.toView(repository.save(Customer.update(command.id(), command.name())));
    }

}
```

## Interface Example

Use this layout:

```java
public interface CustomerRepository {

    Optional<Customer> findById(CustomerId id);

    Customer save(Customer customer);

}
```

## Single Return Example

Preferred:

```java
CustomerView execute(FindCustomerQuery query) {
    CustomerView result;

    if (query.id() == null) {
        result = CustomerView.empty();
    } else {
        result = repository.findById(query.id())
                .map(mapper::toView)
                .orElse(CustomerView.empty());
    }

    return result;
}
```

Avoid:

```java
CustomerView execute(FindCustomerQuery query) {
    if (query.id() == null) {
        return CustomerView.empty();
    }

    return repository.findById(query.id())
            .map(mapper::toView)
            .orElse(CustomerView.empty());
}
```

## Delivery Check

Before final response after backend Java edits:

- scan touched Java types for blank lines after opening braces and before final closing braces;
- scan touched Java annotations to ensure no blank line separates an annotation from its target;
- scan touched Java methods for multiple `return` statements;
- scan touched use cases to ensure each use case class has exactly one `execute` method and no `*UseCaseSupport` grouping class remains;
- scan touched interfaces for one blank line between method declarations.
- scan backend Java sources to ensure there are no nested types and no file declares several Java types.

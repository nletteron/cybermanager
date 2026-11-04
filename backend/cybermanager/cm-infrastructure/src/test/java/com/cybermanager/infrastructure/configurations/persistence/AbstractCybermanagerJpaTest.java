package com.cybermanager.infrastructure.configurations.persistence;

import com.cybermanager.domain.businessobjects.company.CompanyProfile;
import com.cybermanager.domain.businessobjects.sales.Invoice;
import com.cybermanager.domain.businessobjects.users.User;
import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.sales.InvoiceLine;
import com.cybermanager.domain.valueobjects.sales.InvoiceStatus;
import com.cybermanager.domain.valueobjects.sales.SaleLine;
import com.cybermanager.domain.valueobjects.shared.Money;
import com.cybermanager.domain.valueobjects.users.AppRole;
import com.cybermanager.domain.valueobjects.users.EmailAddress;
import com.cybermanager.domain.valueobjects.users.PasswordHash;
import com.cybermanager.domain.valueobjects.users.UserId;
import com.cybermanager.infrastructure.out.adapters.persistences.catalog.CategoryRepositoryAdapter;
import com.cybermanager.infrastructure.out.adapters.persistences.catalog.ProductRepositoryAdapter;
import com.cybermanager.infrastructure.out.adapters.persistences.company.CompanyProfileRepositoryAdapter;
import com.cybermanager.infrastructure.out.adapters.persistences.customer.CustomerRepositoryAdapter;
import com.cybermanager.infrastructure.out.adapters.persistences.customer.DebtRepositoryAdapter;
import com.cybermanager.infrastructure.out.adapters.persistences.customer.GeneratedArchiveFileRepositoryAdapter;
import com.cybermanager.infrastructure.out.adapters.persistences.errors.ErrorCatalogPersistenceAdapter;
import com.cybermanager.infrastructure.out.adapters.persistences.sales.ConnectionPricingRepositoryAdapter;
import com.cybermanager.infrastructure.out.adapters.persistences.sales.InvoiceRepositoryAdapter;
import com.cybermanager.infrastructure.out.adapters.persistences.sales.SaleRepositoryAdapter;
import com.cybermanager.infrastructure.out.adapters.persistences.session.CafeSessionRepositoryAdapter;
import com.cybermanager.infrastructure.out.adapters.persistences.subscription.SubscriptionOfferRepositoryAdapter;
import com.cybermanager.infrastructure.out.adapters.persistences.users.UserRepositoryAdapter;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@DataJpaTest
@ContextConfiguration(classes = JpaTestApplication.class)
@Import({
        CategoryRepositoryAdapter.class,
        ProductRepositoryAdapter.class,
        CompanyProfileRepositoryAdapter.class,
        CustomerRepositoryAdapter.class,
        DebtRepositoryAdapter.class,
        ErrorCatalogPersistenceAdapter.class,
        GeneratedArchiveFileRepositoryAdapter.class,
        ConnectionPricingRepositoryAdapter.class,
        InvoiceRepositoryAdapter.class,
        SaleRepositoryAdapter.class,
        CafeSessionRepositoryAdapter.class,
        SubscriptionOfferRepositoryAdapter.class,
        UserRepositoryAdapter.class

})
public abstract class AbstractCybermanagerJpaTest {

    protected CompanyProfile companyProfile(String legalName, LocalDateTime updatedAt) {
        return CompanyProfile.create(
                legalName,
                "12345678900010",
                "0102030405",
                "contact@cybermanager.local",
                "1 rue du test",
                "",
                "75000",
                "Paris",
                "France",
                updatedAt
        );
    }

    protected SaleLine saleLine(String label, String amount) {
        return new SaleLine(label, 1, Money.of(amount), Money.of(amount));
    }

    protected Invoice invoice(String number, String customerName, InvoiceStatus status, LocalDateTime issuedAt, CustomerId customerId) {
        return new Invoice(
                UUID.randomUUID(),
                UUID.randomUUID(),
                number,
                customerId,
                customerName,
                issuedAt,
                status,
                Money.of("4.00"),
                List.of(new InvoiceLine("Connexion", 1, Money.of("4.00"), Money.of("4.00")))
        );
    }

    protected User user(String email, String firstName, Set<AppRole> roles) {
        return User.create(
                UserId.newId(),
                new EmailAddress(email),
                firstName,
                "User",
                new PasswordHash("hash"),
                roles
        );
    }

}

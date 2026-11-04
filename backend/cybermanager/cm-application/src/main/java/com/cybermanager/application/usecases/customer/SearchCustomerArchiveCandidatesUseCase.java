package com.cybermanager.application.usecases.customer;

import com.cybermanager.application.services.shared.BusinessException;
import com.cybermanager.application.views.customer.*;
import com.cybermanager.domain.businessobjects.customer.Customer;
import com.cybermanager.domain.businessobjects.customer.DebtRecord;
import com.cybermanager.domain.businessobjects.sales.Sale;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import com.cybermanager.domain.ports.customer.CustomerRepository;
import com.cybermanager.domain.ports.customer.DebtRepository;
import com.cybermanager.domain.ports.customer.GeneratedArchiveFileRepository;
import com.cybermanager.domain.ports.sales.SaleRepository;
import com.cybermanager.domain.ports.session.CafeSessionRepository;
import com.cybermanager.domain.ports.subscription.SubscriptionOfferRepository;
import com.cybermanager.domain.queries.customer.SearchCustomerArchiveCandidatesQuery;
import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.customer.CustomerType;
import com.cybermanager.domain.valueobjects.customer.DebtStatus;
import com.cybermanager.domain.valueobjects.sales.SaleLine;
import com.cybermanager.domain.valueobjects.sales.SaleType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class SearchCustomerArchiveCandidatesUseCase {

    private static final DateTimeFormatter CSV_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String ARCHIVE_MEDIA_TYPE_CSV = "text/csv";
    private static final String ARCHIVE_MEDIA_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String DEBT_REPORT_MEDIA_TYPE_PDF = "application/pdf";

    private final CustomerRepository customerRepository;
    private final SubscriptionOfferRepository subscriptionOfferRepository;
    private final SaleRepository saleRepository;
    private final DebtRepository debtRepository;
    private final GeneratedArchiveFileRepository generatedArchiveFileRepository;
    private final CafeSessionRepository sessionRepository;

    public SearchCustomerArchiveCandidatesUseCase(
            CustomerRepository customerRepository,
            SubscriptionOfferRepository subscriptionOfferRepository,
            SaleRepository saleRepository,
            DebtRepository debtRepository,
            GeneratedArchiveFileRepository generatedArchiveFileRepository,
            CafeSessionRepository sessionRepository
    ) {
        this.customerRepository = customerRepository;
        this.subscriptionOfferRepository = subscriptionOfferRepository;
        this.saleRepository = saleRepository;
        this.debtRepository = debtRepository;
        this.generatedArchiveFileRepository = generatedArchiveFileRepository;
        this.sessionRepository = sessionRepository;
    }


    @Transactional(readOnly = true)
    public List<CustomerArchiveCandidateView> execute(SearchCustomerArchiveCandidatesQuery query) {
        return archiveCandidates(query.startDate(), query.endDate(), query.type()).stream()
                .map(candidate -> new CustomerArchiveCandidateView(
                        candidate.customer().id().value(),
                        candidate.customer().name(),
                        candidate.customer().type().name(),
                        candidate.customer().status().name(),
                        candidate.customer().remainingMinutes(),
                        candidate.latestActivityAt(),
                        candidate.sessions().size(),
                        candidate.sales().size(),
                        candidate.debts().size(),
                        candidate.salesTotal(),
                        candidate.debtTotal()
                ))
                .toList();
    }


    private CustomerView toView(Customer customer) {
        var totalDebt = debtRepository.findByCustomerId(customer.id()).stream()
                .filter(debt -> debt.status() == DebtStatus.OPEN)
                .map(DebtRecord::amount)
                .reduce(com.cybermanager.domain.valueobjects.shared.Money.of("0"), com.cybermanager.domain.valueobjects.shared.Money::add);
        return new CustomerView(customer.id().value(), customer.name(), customer.type().name(), customer.status().name(), customer.remainingMinutes(), totalDebt.amount());
    }


    private CustomerSaleView toSaleView(Sale sale) {
        String debtLabel = debtLabelForSale(sale);
        String label = sale.lines().isEmpty() ? sale.type().name() : sale.lines().stream().map(SaleLine::label).reduce((first, second) -> first + ", " + second).orElse(sale.type().name());
        boolean openDebt = debtRepository.findByCustomerId(sale.customerId()).stream()
                .anyMatch(debt -> debt.status() == DebtStatus.OPEN
                        && debt.label().equals(debtLabel)
                        && debt.amount().amount().compareTo(sale.totalAmount().amount()) == 0);
        return new CustomerSaleView(sale.id().value(), sale.sessionId(), sale.type().name(), label, debtLabel, sale.soldAt(), sale.totalAmount().amount(), openDebt);
    }


    private String debtLabelForSale(Sale sale) {
        return switch (sale.type()) {
            case PRODUCTS -> sale.lines().stream()
                    .map(line -> line.quantity() + " x " + line.label())
                    .reduce((left, right) -> left + ", " + right)
                    .map(label -> label + " du " + com.cybermanager.application.services.shared.DateTimeLabelFormatter.format(sale.soldAt()))
                    .orElse("Vente produits du " + com.cybermanager.application.services.shared.DateTimeLabelFormatter.format(sale.soldAt()));
            case SUBSCRIPTION -> "Vente abonnement du " + com.cybermanager.application.services.shared.DateTimeLabelFormatter.format(sale.soldAt());
            case CONNECTION_TIME -> "Vente temps du " + com.cybermanager.application.services.shared.DateTimeLabelFormatter.format(sale.soldAt());
        };
    }


    private SaleType saleTypeForDebtLabel(String debtLabel) {
        SaleType result;

        if (debtLabel.startsWith("Vente abonnement du ")) {
            result = SaleType.SUBSCRIPTION;
        } else if (debtLabel.startsWith("Vente temps du ") || debtLabel.startsWith("Session du ")) {
            result = SaleType.CONNECTION_TIME;
        } else {
            result = SaleType.PRODUCTS;
        }

        return result;
    }


    private List<SaleLine> saleLinesForDebt(DebtRecord debt) {
        List<SaleLine> result;

        if (debt.label().startsWith("Vente abonnement du ")) {
            result = List.of(new SaleLine("Abonnement", 1, debt.amount(), debt.amount()));
        } else if (debt.label().startsWith("Vente temps du ") || debt.label().startsWith("Session du ")) {
            result = List.of(new SaleLine("Temps de connexion", 1, debt.amount(), debt.amount()));
        } else {
            String label = debt.label();
            int dateIndex = label.lastIndexOf(" du ");
            String productLabel = dateIndex > 0 ? label.substring(0, dateIndex) : label;
            result = List.of(new SaleLine(productLabel, 1, debt.amount(), debt.amount()));
        }

        return result;
    }


    private CustomerDebtSummaryView toDebtSummaryView(Map.Entry<CustomerId, List<DebtRecord>> entry) {
        Customer customer = customerRepository.findById(entry.getKey())
                .orElseThrow(() -> new BusinessException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.CUSTOMER_NOT_FOUND, "Customer not found"));
        var total = entry.getValue().stream()
                .map(DebtRecord::amount)
                .reduce(com.cybermanager.domain.valueobjects.shared.Money.of("0"), com.cybermanager.domain.valueobjects.shared.Money::add);

        return new CustomerDebtSummaryView(
                customer.id().value(),
                customer.name(),
                customer.type().name(),
                total.amount(),
                entry.getValue().stream().map(this::toDebtView).toList()
        );
    }


    private CustomerDebtView toDebtView(DebtRecord debtRecord) {
        return new CustomerDebtView(
                debtRecord.id().value(),
                debtRecord.label(),
                debtRecord.comment(),
                debtRecord.amount().amount(),
                debtRecord.status().name(),
                debtRecord.createdAt(),
                debtRecord.settledAt()
        );
    }


    private List<ArchiveCustomerCandidate> archiveCandidates(LocalDate startDate, LocalDate endDate, String type) {
        validateArchivePeriod(startDate, endDate);
        CustomerType customerType = requestedArchiveCustomerType(type);
        List<Customer> customers = customerRepository.search("", customerType);
        List<ArchiveCustomerCandidate> result;

        if (customers.isEmpty()) {
            result = List.of();
        } else {
            var customerIds = customers.stream().map(Customer::id).toList();
            Map<CustomerId, List<Sale>> salesByCustomer = saleRepository.findByCustomerIds(customerIds).stream()
                    .collect(Collectors.groupingBy(Sale::customerId));
            Map<CustomerId, List<DebtRecord>> debtsByCustomer = debtRepository.findByCustomerIds(customerIds).stream()
                    .collect(Collectors.groupingBy(DebtRecord::customerId));
            Map<CustomerId, List<com.cybermanager.domain.businessobjects.session.CafeSession>> sessionsByCustomer = sessionRepository.findByCustomerIds(customerIds).stream()
                    .collect(Collectors.groupingBy(com.cybermanager.domain.businessobjects.session.CafeSession::customerId));

            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();

            result = customers.stream()
                    .map(customer -> buildArchiveCandidate(customer, salesByCustomer.getOrDefault(customer.id(), List.of()), debtsByCustomer.getOrDefault(customer.id(), List.of()), sessionsByCustomer.getOrDefault(customer.id(), List.of()), start, endExclusive))
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(ArchiveCustomerCandidate::latestActivityAt).thenComparing(candidate -> candidate.customer().name()))
                    .toList();
        }

        return result;
    }


    private ArchiveCustomerCandidate buildArchiveCandidate(
            Customer customer,
            List<Sale> sales,
            List<DebtRecord> debts,
            List<com.cybermanager.domain.businessobjects.session.CafeSession> sessions,
            LocalDateTime start,
            LocalDateTime endExclusive
    ) {
        boolean hasActiveSession = sessions.stream().anyMatch(session -> session.endedAt() == null && !session.paid());
        boolean hasOpenDebt = debts.stream().anyMatch(debt -> debt.status() == DebtStatus.OPEN);
        ArchiveCustomerCandidate result = null;

        if (!hasActiveSession && customer.type() == CustomerType.WALK_IN && !hasOpenDebt && customer.remainingMinutes() <= 0) {
            LocalDateTime latestActivityAt = latestActivityAt(sales, debts, sessions);

            if (latestActivityAt != null && !latestActivityAt.isBefore(start) && latestActivityAt.isBefore(endExclusive)) {
                BigDecimal salesTotal = sales.stream()
                        .map(sale -> sale.totalAmount().amount())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal debtTotal = debts.stream()
                        .map(debt -> debt.amount().amount())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                result = new ArchiveCustomerCandidate(customer, latestActivityAt, sessions, sales, debts, salesTotal, debtTotal);
            }
        }

        return result;
    }


    private List<DebtReportEntry> debtReportEntries() {
        Map<CustomerId, List<DebtRecord>> openDebtsByCustomer = debtRepository.findOpenDebts().stream()
                .collect(Collectors.groupingBy(DebtRecord::customerId));
        List<DebtReportEntry> result;

        if (openDebtsByCustomer.isEmpty()) {
            result = List.of();
        } else {
            result = openDebtsByCustomer.entrySet().stream()
                    .map(this::toDebtReportEntry)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(entry -> entry.customer().name()))
                    .toList();
        }

        return result;
    }


    private DebtReportEntry toDebtReportEntry(Map.Entry<CustomerId, List<DebtRecord>> entry) {
        return customerRepository.findById(entry.getKey())
                .map(customer -> new DebtReportEntry(
                        customer,
                        entry.getValue().stream().sorted(Comparator.comparing(DebtRecord::createdAt)).toList(),
                        entry.getValue().stream().map(debt -> debt.amount().amount()).reduce(BigDecimal.ZERO, BigDecimal::add)
                ))
                .orElse(null);
    }


    private LocalDateTime latestActivityAt(
            List<Sale> sales,
            List<DebtRecord> debts,
            List<com.cybermanager.domain.businessobjects.session.CafeSession> sessions
    ) {
        return java.util.stream.Stream.of(
                        sales.stream().map(Sale::soldAt),
                        debts.stream().map(DebtRecord::createdAt),
                        sessions.stream().map(session -> session.endedAt() != null ? session.endedAt() : session.startedAt())
                )
                .flatMap(Function.identity())
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }


    private byte[] buildArchiveCsv(LocalDate startDate, LocalDate endDate, List<ArchiveCustomerCandidate> candidates) {
        StringBuilder csv = new StringBuilder();
        csv.append("customerId,name,type,status,remainingMinutes,periodStart,periodEnd,latestActivityAt,sessionCount,saleCount,debtCount,salesTotal,debtTotal,sessions,sales,debts")
                .append(System.lineSeparator());
        for (ArchiveCustomerCandidate candidate : candidates) {
            csv.append(csv(candidate.customer().id().value()))
                    .append(',')
                    .append(csv(candidate.customer().name()))
                    .append(',')
                    .append(csv(candidate.customer().type().name()))
                    .append(',')
                    .append(csv(candidate.customer().status().name()))
                    .append(',')
                    .append(candidate.customer().remainingMinutes())
                    .append(',')
                    .append(csv(startDate))
                    .append(',')
                    .append(csv(endDate))
                    .append(',')
                    .append(csv(candidate.latestActivityAt()))
                    .append(',')
                    .append(candidate.sessions().size())
                    .append(',')
                    .append(candidate.sales().size())
                    .append(',')
                    .append(candidate.debts().size())
                    .append(',')
                    .append(candidate.salesTotal())
                    .append(',')
                    .append(candidate.debtTotal())
                    .append(',')
                    .append(csv(candidate.sessions().stream()
                            .sorted(Comparator.comparing(com.cybermanager.domain.businessobjects.session.CafeSession::startedAt))
                            .map(session -> session.workstation() + " du " + csvValue(session.startedAt()) + " au " + csvValue(session.endedAt()))
                            .collect(Collectors.joining(" | "))))
                    .append(',')
                    .append(csv(candidate.sales().stream()
                            .sorted(Comparator.comparing(Sale::soldAt))
                            .map(sale -> sale.type().name() + " " + csvValue(sale.soldAt()) + " " + sale.totalAmount().amount())
                            .collect(Collectors.joining(" | "))))
                    .append(',')
                    .append(csv(candidate.debts().stream()
                            .sorted(Comparator.comparing(DebtRecord::createdAt))
                            .map(debt -> debt.status().name() + " " + csvValue(debt.createdAt()) + " " + debt.label() + " " + debt.amount().amount())
                            .collect(Collectors.joining(" | "))))
                    .append(System.lineSeparator());
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }


    private ArchiveDocument buildArchiveDocument(LocalDate startDate, LocalDate endDate, String type, String format, List<ArchiveCustomerCandidate> candidates) {
        String normalizedFormat = format == null || format.isBlank() ? "csv" : format.toLowerCase();
        ArchiveDocument result;

        if ("xlsx".equals(normalizedFormat)) {
            result = new ArchiveDocument(
                    archiveFileName(startDate, endDate, type, "xlsx"),
                    ARCHIVE_MEDIA_TYPE_XLSX,
                    buildArchiveXlsx(startDate, endDate, candidates)
            );
        } else {
            result = new ArchiveDocument(
                    archiveFileName(startDate, endDate, type, "csv"),
                    ARCHIVE_MEDIA_TYPE_CSV,
                    buildArchiveCsv(startDate, endDate, candidates)
            );
        }

        return result;
    }


    private byte[] buildArchiveXlsx(LocalDate startDate, LocalDate endDate, List<ArchiveCustomerCandidate> candidates) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Archivage");
            String[] headers = {
                    "customerId", "name", "type", "status", "remainingMinutes", "periodStart", "periodEnd", "latestActivityAt",
                    "sessionCount", "saleCount", "debtCount", "salesTotal", "debtTotal", "sessions", "sales", "debts"
            };
            Row headerRow = sheet.createRow(0);
            for (int index = 0; index < headers.length; index++) {
                headerRow.createCell(index).setCellValue(headers[index]);
            }
            int rowIndex = 1;
            for (ArchiveCustomerCandidate candidate : candidates) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(candidate.customer().id().value().toString());
                row.createCell(1).setCellValue(candidate.customer().name());
                row.createCell(2).setCellValue(candidate.customer().type().name());
                row.createCell(3).setCellValue(candidate.customer().status().name());
                row.createCell(4).setCellValue(candidate.customer().remainingMinutes());
                row.createCell(5).setCellValue(startDate.toString());
                row.createCell(6).setCellValue(endDate.toString());
                row.createCell(7).setCellValue(csvValue(candidate.latestActivityAt()));
                row.createCell(8).setCellValue(candidate.sessions().size());
                row.createCell(9).setCellValue(candidate.sales().size());
                row.createCell(10).setCellValue(candidate.debts().size());
                row.createCell(11).setCellValue(candidate.salesTotal().doubleValue());
                row.createCell(12).setCellValue(candidate.debtTotal().doubleValue());
                row.createCell(13).setCellValue(candidate.sessions().stream()
                        .sorted(Comparator.comparing(com.cybermanager.domain.businessobjects.session.CafeSession::startedAt))
                        .map(session -> session.workstation() + " du " + csvValue(session.startedAt()) + " au " + csvValue(session.endedAt()))
                        .collect(Collectors.joining(" | ")));
                row.createCell(14).setCellValue(candidate.sales().stream()
                        .sorted(Comparator.comparing(Sale::soldAt))
                        .map(sale -> sale.type().name() + " " + csvValue(sale.soldAt()) + " " + sale.totalAmount().amount())
                        .collect(Collectors.joining(" | ")));
                row.createCell(15).setCellValue(candidate.debts().stream()
                        .sorted(Comparator.comparing(DebtRecord::createdAt))
                        .map(debt -> debt.status().name() + " " + csvValue(debt.createdAt()) + " " + debt.label() + " " + debt.amount().amount())
                        .collect(Collectors.joining(" | ")));
            }
            for (int index = 0; index < headers.length; index++) {
                sheet.autoSizeColumn(index);
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (Exception exception) {
            throw new BusinessException(BusinessErrorType.CONFLICT, BusinessErrorCode.ARCHIVE_EXPORT_FAILED, "Archive XLSX export failed");
        }
    }


    private byte[] buildDebtReportPdf(List<DebtReportEntry> entries) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream stream = new PDPageContentStream(document, page);
            float y = 800;
            stream.beginText();
            stream.setFont(boldFont, 14);
            stream.newLineAtOffset(50, y);
            stream.showText("Rapport complet des dettes ouvertes");
            stream.endText();
            y -= 28;

            for (DebtReportEntry entry : entries) {
                if (y < 120) {
                    stream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    stream = new PDPageContentStream(document, page);
                    y = 800;
                }
                stream.beginText();
                stream.setFont(boldFont, 11);
                stream.newLineAtOffset(50, y);
                stream.showText(entry.customer().name() + " - " + entry.customer().type().name() + " - Total " + entry.total().toPlainString() + " EUR");
                stream.endText();
                y -= 18;
                for (DebtRecord debt : entry.debts()) {
                    if (y < 80) {
                        stream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        stream = new PDPageContentStream(document, page);
                        y = 800;
                    }
                    stream.beginText();
                    stream.setFont(font, 9);
                    stream.newLineAtOffset(60, y);
                    stream.showText(csvValue(debt.createdAt()) + " | " + debt.label() + " | " + debt.amount().amount().toPlainString() + " EUR");
                    stream.endText();
                    y -= 14;
                }
                y -= 10;
            }
            stream.close();
            document.save(output);
            return output.toByteArray();
        } catch (Exception exception) {
            throw new BusinessException(BusinessErrorType.CONFLICT, BusinessErrorCode.DEBT_REPORT_EXPORT_FAILED, "Debt report export failed");
        }
    }


    private String archiveFileName(LocalDate startDate, LocalDate endDate, String type, String extension) {
        return "customers-archive-" + normalizedArchiveType(type) + "-" + startDate + "-to-" + endDate + "." + extension;
    }


    private void validateArchivePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessException(BusinessErrorType.VALIDATION, BusinessErrorCode.ARCHIVE_PERIOD_REQUIRED, "Archive period is required");
        }
        if (endDate.isBefore(startDate)) {
            throw new BusinessException(BusinessErrorType.VALIDATION, BusinessErrorCode.ARCHIVE_PERIOD_INVALID, "Archive end date must be after start date");
        }
    }


    private String csv(Object value) {
        return "\"" + csvValue(value).replace("\"", "\"\"") + "\"";
    }


    private String csvValue(Object value) {
        String result;

        if (value == null) {
            result = "";
        } else if (value instanceof LocalDateTime localDateTime) {
            result = localDateTime.format(CSV_DATE_TIME);
        } else {
            result = String.valueOf(value);
        }

        return result;
    }


    private CustomerType requestedArchiveCustomerType(String type) {
        CustomerType result;

        if (type == null || type.isBlank()) {
            result = CustomerType.WALK_IN;
        } else {
            CustomerType requestedType = CustomerType.valueOf(type.toUpperCase());
            result = requestedType == CustomerType.SUBSCRIBER
                    ? CustomerType.SUBSCRIBER
                    : CustomerType.WALK_IN;
        }

        return result;
    }


    private String normalizedArchiveType(String type) {
        return type == null || type.isBlank() ? "walk_in" : type.toLowerCase();
    }

}

ALTER TABLE cybermanager.cm_customer_debts ADD COLUMN sale_id UUID;

-- Preserve distinct historical rows by pairing equal legacy labels in chronological order.
-- No new sale or debt is created, merged or removed.
WITH sale_labels AS (
    SELECT s.id, s.customer_id, s.sold_at, s.total_amount,
           CASE s.type
               WHEN 'SUBSCRIPTION' THEN 'Vente abonnement du ' || to_char(s.sold_at, 'DD/MM/YYYY HH24:MI')
               WHEN 'CONNECTION_TIME' THEN 'Vente temps du ' || to_char(s.sold_at, 'DD/MM/YYYY HH24:MI')
               ELSE coalesce(
                   (SELECT string_agg(l.quantity::text || ' x ' || l.label, ', ' ORDER BY l.id)
                    FROM cybermanager.cm_sale_lines l WHERE l.sale_id = s.id),
                   'Vente produits'
               ) || ' du ' || to_char(s.sold_at, 'DD/MM/YYYY HH24:MI')
           END AS debt_label
    FROM cybermanager.cm_sales s
), ranked_sales AS (
    SELECT *, row_number() OVER (
        PARTITION BY customer_id, debt_label, total_amount ORDER BY sold_at, id
    ) AS occurrence
    FROM sale_labels
), ranked_debts AS (
    SELECT *, row_number() OVER (
        PARTITION BY customer_id, label, amount ORDER BY created_at, id
    ) AS occurrence
    FROM cybermanager.cm_customer_debts
    WHERE status = 'OPEN'
)
UPDATE cybermanager.cm_customer_debts debt
SET sale_id = sale.id
FROM ranked_debts candidate
JOIN ranked_sales sale
  ON sale.customer_id = candidate.customer_id
 AND sale.debt_label = candidate.label
 AND sale.total_amount = candidate.amount
 AND sale.occurrence = candidate.occurrence
WHERE debt.id = candidate.id;

CREATE INDEX idx_cm_customer_debts_sale_id ON cybermanager.cm_customer_debts (sale_id);
CREATE UNIQUE INDEX uq_cm_customer_debts_open_sale ON cybermanager.cm_customer_debts (sale_id)
    WHERE status = 'OPEN' AND sale_id IS NOT NULL;

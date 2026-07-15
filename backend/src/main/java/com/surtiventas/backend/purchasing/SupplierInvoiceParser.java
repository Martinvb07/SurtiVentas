package com.surtiventas.backend.purchasing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns the raw OCR text of a supplier invoice into a {@link ParsedInvoice}: a
 * detected grand total plus candidate line items. Purely heuristic and
 * defensive — OCR output is noisy — so it never throws on bad input; it just
 * returns whatever it could confidently read (and null/empty otherwise). The
 * authoritative reconciliation is done by a human against these hints.
 */
@Component
public class SupplierInvoiceParser {

    /** A run of digits with embedded thousands/decimal separators, e.g. 1.234.567,89 */
    private static final Pattern MONEY = Pattern.compile("\\d[\\d.,]*\\d");
    private static final Pattern LEADING_QUANTITY = Pattern.compile("^\\s*(\\d{1,4})\\b");

    /** Lines whose grand total we prefer, most specific first. */
    private static final List<String> STRONG_TOTAL_KEYWORDS = List.of(
            "TOTAL A PAGAR", "NETO A PAGAR", "VALOR A PAGAR", "TOTAL FACTURA", "GRAN TOTAL");

    /** Lines that are never product lines. */
    private static final List<String> NON_ITEM_KEYWORDS = List.of(
            "TOTAL", "SUBTOTAL", "IVA", "IMPUESTO", "DESCUENTO", "FACTURA", "NIT",
            "FECHA", "CLIENTE", "PROVEEDOR", "DIRECCION", "TELEFONO", "CUFE", "RESOLUCION");

    /**
     * Lines carrying identifiers or dates (NIT, invoice number, resolution…)
     * whose long digit runs must never be mistaken for money — a NIT like
     * "900.123.456-7" otherwise reads as the biggest "amount" on the page.
     */
    private static final List<String> ID_LINE_KEYWORDS = List.of(
            "NIT", "CUFE", "RESOLUCION", "FACTURA", "FECHA", "TELEFONO", "CELULAR");

    /** At least a few letters, i.e. a real product description. */
    private static final Pattern LETTERS = Pattern.compile("[A-Za-zÁÉÍÓÚÑáéíóúñ]{3,}");

    private static final int MAX_LINES = 40;

    public ParsedInvoice parse(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return new ParsedInvoice(null, List.of());
        }
        String[] lines = rawText.split("\\r?\\n");
        BigDecimal detectedTotal = detectTotal(lines);
        List<ParsedInvoice.ParsedLine> items = detectLineItems(lines);
        return new ParsedInvoice(detectedTotal, items);
    }

    private BigDecimal detectTotal(String[] lines) {
        BigDecimal strong = null;
        BigDecimal weak = null;
        BigDecimal maxOverall = null;

        for (String line : lines) {
            String upper = line.toUpperCase();
            boolean isStrong = STRONG_TOTAL_KEYWORDS.stream().anyMatch(upper::contains);
            // Skip identifier/date lines (unless they are the grand-total line
            // itself) so a NIT or invoice number never counts as an amount.
            if (!isStrong && ID_LINE_KEYWORDS.stream().anyMatch(kw -> containsWord(upper, kw))) {
                continue;
            }
            BigDecimal lineMax = largestAmount(line);
            if (lineMax == null) {
                continue;
            }
            if (maxOverall == null || lineMax.compareTo(maxOverall) > 0) {
                maxOverall = lineMax;
            }
            if (isStrong) {
                if (strong == null || lineMax.compareTo(strong) > 0) {
                    strong = lineMax;
                }
            } else if (upper.contains("TOTAL") && !upper.contains("SUBTOTAL")) {
                if (weak == null || lineMax.compareTo(weak) > 0) {
                    weak = lineMax;
                }
            }
        }

        if (strong != null) {
            return strong;
        }
        if (weak != null) {
            return weak;
        }
        // No "total" label found: the grand total is almost always the largest
        // number on the invoice, so fall back to that.
        return maxOverall;
    }

    private List<ParsedInvoice.ParsedLine> detectLineItems(String[] lines) {
        List<ParsedInvoice.ParsedLine> items = new ArrayList<>();
        for (String line : lines) {
            if (items.size() >= MAX_LINES) {
                break;
            }
            String trimmed = line.strip();
            if (trimmed.isEmpty()) {
                continue;
            }
            String upper = trimmed.toUpperCase();
            if (NON_ITEM_KEYWORDS.stream().anyMatch(kw -> containsWord(upper, kw))) {
                continue;
            }

            List<BigDecimal> amounts = allAmounts(trimmed);
            Integer quantity = leadingQuantity(trimmed);
            // A product line needs a real (letter-bearing) description, a price,
            // and either a quantity or a second amount (unit price + subtotal);
            // this filters out stray text and the bare amounts OCR sometimes
            // splits into their own column/lines.
            boolean looksLikeItem = LETTERS.matcher(trimmed).find()
                    && !amounts.isEmpty() && (quantity != null || amounts.size() >= 2);
            if (!looksLikeItem) {
                continue;
            }

            BigDecimal amount = amounts.stream().max(BigDecimal::compareTo).orElse(null);
            String description = describe(trimmed, quantity);
            if (description.isEmpty()) {
                description = trimmed;
            }
            items.add(new ParsedInvoice.ParsedLine(description, quantity, amount));
        }
        return items;
    }

    /** Strips the leading quantity and any money tokens to leave the description. */
    private String describe(String line, Integer quantity) {
        String result = line;
        if (quantity != null) {
            result = result.replaceFirst("^\\s*\\d{1,4}\\b", "");
        }
        result = MONEY.matcher(result).replaceAll(" ");
        return result.replaceAll("[\\s$.,-]{2,}", " ").strip();
    }

    /** Whole-word match so, e.g., "IVA" doesn't fire on "Aceite de Oliva". */
    private boolean containsWord(String upperText, String keyword) {
        return Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b").matcher(upperText).find();
    }

    private Integer leadingQuantity(String line) {
        Matcher m = LEADING_QUANTITY.matcher(line);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private BigDecimal largestAmount(String line) {
        return allAmounts(line).stream().max(BigDecimal::compareTo).orElse(null);
    }

    private List<BigDecimal> allAmounts(String line) {
        List<BigDecimal> amounts = new ArrayList<>();
        Matcher m = MONEY.matcher(line);
        while (m.find()) {
            BigDecimal value = normalizeAmount(m.group());
            if (value != null) {
                amounts.add(value);
            }
        }
        return amounts;
    }

    /**
     * Normalises a raw money token to a {@link BigDecimal}, resolving Colombian
     * ("1.234.567,89") and US ("1,234,567.89") separator conventions. Returns
     * null when the token can't be parsed.
     */
    BigDecimal normalizeAmount(String token) {
        String t = token.replace(" ", "");
        int lastDot = t.lastIndexOf('.');
        int lastComma = t.lastIndexOf(',');
        String normalized;

        if (lastDot >= 0 && lastComma >= 0) {
            char decimalSep = lastDot > lastComma ? '.' : ',';
            char thousandsSep = decimalSep == '.' ? ',' : '.';
            normalized = t.replace(String.valueOf(thousandsSep), "")
                    .replace(decimalSep, '.');
        } else if (lastComma >= 0) {
            normalized = isDecimalSeparator(t, lastComma) ? t.replace(',', '.') : t.replace(",", "");
        } else if (lastDot >= 0) {
            normalized = isDecimalSeparator(t, lastDot) ? t : t.replace(".", "");
        } else {
            normalized = t;
        }

        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /** A single separator followed by 1–2 digits reads as a decimal point. */
    private boolean isDecimalSeparator(String token, int sepIndex) {
        long separators = token.chars().filter(c -> c == token.charAt(sepIndex)).count();
        int digitsAfter = token.length() - sepIndex - 1;
        return separators == 1 && digitsAfter >= 1 && digitsAfter <= 2;
    }
}

package com.surtiventas.backend.billing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns the raw OCR text of a payment receipt (comprobante) into a
 * {@link ParsedReceipt}: the paid amount and the reference/transaction number.
 * Purely heuristic and defensive — OCR output is noisy — so it never throws and
 * returns null for anything it can't read confidently.
 */
@Component
public class PaymentReceiptParser {

    private static final Pattern MONEY = Pattern.compile("\\d[\\d.,]*\\d");
    private static final Pattern REFERENCE_DIGITS = Pattern.compile("\\d{5,}");

    /** Lines whose amount is the payment value. */
    private static final List<String> VALUE_KEYWORDS = List.of(
            "VALOR", "MONTO", "PAGO", "PAGADO", "PAGASTE", "TRANSFERIDO", "TRANSFERENCIA",
            "ENVIADO", "ENVIASTE", "TOTAL", "ABONO", "CONSIGNACION");

    /** Lines carrying identifiers/dates whose numbers must not be read as money. */
    private static final List<String> ID_KEYWORDS = List.of(
            "CUENTA", "REFERENCIA", "COMPROBANTE", "APROBACION", "AUTORIZACION", "NUMERO",
            "CELULAR", "TELEFONO", "NIT", "CUS", "TRANSACCION", "FECHA", "HORA", "DOCUMENTO");

    /** Lines that carry the reference/transaction number. */
    private static final List<String> REFERENCE_KEYWORDS = List.of(
            "REFERENCIA", "COMPROBANTE", "APROBACION", "AUTORIZACION", "CUS", "TRANSACCION");

    public ParsedReceipt parse(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return new ParsedReceipt(null, null);
        }
        String[] lines = rawText.split("\\r?\\n");

        BigDecimal strong = null;
        BigDecimal maxOverall = null;
        String reference = null;

        for (String line : lines) {
            String upper = line.toUpperCase();
            boolean isValueLine = VALUE_KEYWORDS.stream().anyMatch(upper::contains);
            boolean isIdLine = ID_KEYWORDS.stream().anyMatch(kw -> containsWord(upper, kw));

            if (reference == null && REFERENCE_KEYWORDS.stream().anyMatch(kw -> containsWord(upper, kw))) {
                reference = extractReference(line);
            }

            BigDecimal amount = largestAmount(line);
            if (amount == null || isIdLine) {
                continue;
            }
            if (maxOverall == null || amount.compareTo(maxOverall) > 0) {
                maxOverall = amount;
            }
            if (isValueLine && (strong == null || amount.compareTo(strong) > 0)) {
                strong = amount;
            }
        }

        BigDecimal detectedAmount = strong != null ? strong : maxOverall;
        return new ParsedReceipt(detectedAmount, reference);
    }

    private String extractReference(String line) {
        Matcher m = REFERENCE_DIGITS.matcher(line);
        return m.find() ? m.group() : null;
    }

    private boolean containsWord(String upperText, String keyword) {
        return Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b").matcher(upperText).find();
    }

    private BigDecimal largestAmount(String line) {
        BigDecimal max = null;
        Matcher m = MONEY.matcher(line);
        while (m.find()) {
            BigDecimal value = normalizeAmount(m.group());
            if (value != null && (max == null || value.compareTo(max) > 0)) {
                max = value;
            }
        }
        return max;
    }

    /** Resolves Colombian ("1.234.567,89") and US ("1,234,567.89") conventions. */
    BigDecimal normalizeAmount(String token) {
        String t = token.replace(" ", "");
        int lastDot = t.lastIndexOf('.');
        int lastComma = t.lastIndexOf(',');
        String normalized;

        if (lastDot >= 0 && lastComma >= 0) {
            char decimalSep = lastDot > lastComma ? '.' : ',';
            char thousandsSep = decimalSep == '.' ? ',' : '.';
            normalized = t.replace(String.valueOf(thousandsSep), "").replace(decimalSep, '.');
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

    private boolean isDecimalSeparator(String token, int sepIndex) {
        long separators = token.chars().filter(c -> c == token.charAt(sepIndex)).count();
        int digitsAfter = token.length() - sepIndex - 1;
        return separators == 1 && digitsAfter >= 1 && digitsAfter <= 2;
    }
}

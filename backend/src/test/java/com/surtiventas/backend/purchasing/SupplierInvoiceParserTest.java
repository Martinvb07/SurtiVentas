package com.surtiventas.backend.purchasing;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SupplierInvoiceParserTest {

    private final SupplierInvoiceParser parser = new SupplierInvoiceParser();

    @Test
    void normalisesColombianAndUsAmountConventions() {
        assertThat(parser.normalizeAmount("1.234.567")).isEqualByComparingTo("1234567");
        assertThat(parser.normalizeAmount("1.234.567,89")).isEqualByComparingTo("1234567.89");
        assertThat(parser.normalizeAmount("1,234,567.89")).isEqualByComparingTo("1234567.89");
        assertThat(parser.normalizeAmount("12,50")).isEqualByComparingTo("12.50");
        assertThat(parser.normalizeAmount("1.500")).isEqualByComparingTo("1500");
        assertThat(parser.normalizeAmount("250")).isEqualByComparingTo("250");
    }

    @Test
    void prefersTheGrandTotalLabelOverSubtotalAndTax() {
        String text = """
                Proveedor Distribuidora XYZ
                SUBTOTAL       1.000.000
                IVA (19%)        190.000
                TOTAL A PAGAR  1.190.000
                """;

        ParsedInvoice parsed = parser.parse(text);

        assertThat(parsed.detectedTotal()).isEqualByComparingTo("1190000");
    }

    @Test
    void fallsBackToLargestAmountWhenNoTotalLabel() {
        String text = """
                Arroz Diana    25.000
                Aceite Premier 48.000
                """;

        ParsedInvoice parsed = parser.parse(text);

        assertThat(parsed.detectedTotal()).isEqualByComparingTo("48000");
    }

    @Test
    void extractsCandidateLineItemsWithQuantityAndAmount() {
        String text = """
                2 Arroz Diana 500g    12.500   25.000
                3 Aceite Premier 1L    8.000   24.000
                TOTAL A PAGAR                  49.000
                """;

        ParsedInvoice parsed = parser.parse(text);

        assertThat(parsed.lines()).hasSize(2);
        ParsedInvoice.ParsedLine first = parsed.lines().get(0);
        assertThat(first.quantity()).isEqualTo(2);
        assertThat(first.amount()).isEqualByComparingTo("25000");
        assertThat(first.description()).contains("Arroz");
    }

    @Test
    void doesNotTreatTaxOrHeaderLinesAsItems() {
        String text = """
                FACTURA DE VENTA No 001
                IVA                    190.000
                2 Aceite de Oliva 1L    12.000   24.000
                """;

        ParsedInvoice parsed = parser.parse(text);

        // The IVA and FACTURA lines are skipped; the olive-oil line is kept
        // (the "IVA" inside "Oliva" must not trip the whole-word filter).
        assertThat(parsed.lines()).hasSize(1);
        assertThat(parsed.lines().get(0).description()).contains("Oliva");
    }

    @Test
    void ignoresNitAndReadsGrandTotalWhenOcrSplitsAmountsIntoTheirOwnColumn() {
        // Mirrors real Tesseract output: the NIT is a huge digit run, and the
        // amounts get separated from their labels onto their own lines.
        String text = """
                DISTRIBUIDORA XYZ S.A.S.
                NIT: 900.123.456-7
                FACTURA DE VENTA No FV-2045
                2 Arroz Diana 500g 12.500
                3 Aceite Premier 1L 8.000
                SUBTOTAL
                IVA (19%)
                TOTAL A PAGAR
                25.000
                24.000
                100.000
                19.000
                119.000
                """;

        ParsedInvoice parsed = parser.parse(text);

        // The NIT (900123456) must not win; the grand total is 119.000.
        assertThat(parsed.detectedTotal()).isEqualByComparingTo("119000");
        // Only real product rows survive as items — no bare amount lines.
        assertThat(parsed.lines()).hasSize(2);
        assertThat(parsed.lines()).allSatisfy(l -> assertThat(l.description()).matches(".*[A-Za-z].*"));
    }

    @Test
    void handlesEmptyOrBlankTextGracefully() {
        assertThat(parser.parse(null).detectedTotal()).isNull();
        assertThat(parser.parse(null).lines()).isEmpty();
        assertThat(parser.parse("   ").lines()).isEmpty();
    }
}

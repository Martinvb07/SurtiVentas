package com.surtiventas.backend.billing;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentReceiptParserTest {

    private final PaymentReceiptParser parser = new PaymentReceiptParser();

    @Test
    void detectsAmountAndReferenceIgnoringAccountAndDateNumbers() {
        String text = """
                Bancolombia
                Comprobante de pago
                Cuenta origen 12345678901
                Fecha 2026-07-20
                Valor $150.000
                Referencia 987654321
                """;

        ParsedReceipt parsed = parser.parse(text);

        // The account number (12345678901) must not win; the value line does.
        assertThat(parsed.detectedAmount()).isEqualByComparingTo("150000");
        assertThat(parsed.detectedReference()).isEqualTo("987654321");
    }

    @Test
    void prefersTheValueLineOverALargerUnlabelledAmount() {
        String text = """
                Saldo disponible 5.000.000
                Valor pagado 200.000
                """;

        assertThat(parser.parse(text).detectedAmount()).isEqualByComparingTo("200000");
    }

    @Test
    void fallsBackToTheLargestAmountWhenNoValueLabel() {
        String text = """
                Transferencia exitosa
                75.000
                """;

        assertThat(parser.parse(text).detectedAmount()).isEqualByComparingTo("75000");
    }

    @Test
    void handlesBlankTextGracefully() {
        assertThat(parser.parse(null).detectedAmount()).isNull();
        assertThat(parser.parse("   ").detectedAmount()).isNull();
        assertThat(parser.parse("   ").detectedReference()).isNull();
    }
}

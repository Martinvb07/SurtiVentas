package com.surtiventas.backend.dashboard.dto;

import java.math.BigDecimal;

/** A single labelled value for a chart series (e.g. a day of sales). */
public record SeriesPoint(String label, BigDecimal value) {
}

package com.surtiventas.backend.dashboard.dto;

/** A count grouped by some status/category, used for doughnut and bar charts. */
public record StatusCount(String key, String label, long count) {
}

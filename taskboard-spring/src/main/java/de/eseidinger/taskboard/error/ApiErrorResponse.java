package de.eseidinger.taskboard.error;

import java.util.List;

public record ApiErrorResponse(int status, String code, String message, List<ApiErrorDetail> details) {
}

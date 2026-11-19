package kz.site.roomi.common;

import java.util.List;

public record ItemsResponse<T>(List<T> items) {
}

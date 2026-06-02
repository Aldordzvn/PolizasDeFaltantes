package com.rdzvn.polizasdefaltantes.dto.response;


import java.time.LocalDateTime;

public record ApiResponse<T>(
        boolean exito,
        String mensaje,
        T datos,
        LocalDateTime timestamp
) {

    public static <T> ApiResponse<T> ok (T datos) {
        return new ApiResponse<>(
                true, "OK", datos, LocalDateTime.now()
        );
    }

    public static <T> ApiResponse<T> ok (T datos, String mensaje){
        return new ApiResponse<>(true, mensaje, datos, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error (String mensaje){
        return new ApiResponse<>(false,  mensaje, null, LocalDateTime.now());
    }
}

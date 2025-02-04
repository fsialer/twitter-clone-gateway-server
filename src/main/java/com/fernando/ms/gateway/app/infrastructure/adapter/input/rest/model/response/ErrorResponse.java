package com.fernando.ms.gateway.app.infrastructure.adapter.input.rest.model.response;

import com.fernando.ms.gateway.app.infrastructure.adapter.input.rest.model.enums.ErrorType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {
    private String code;
    private ErrorType type; //Functional, System
    private String message;
    private List<String> details;
    private String timestamp;
}

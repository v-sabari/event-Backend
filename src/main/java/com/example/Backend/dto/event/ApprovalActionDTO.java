package com.example.Backend.dto.event;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Body for approve/reject actions. `remarks` is optional on approve,
 * required on reject (enforced in the service, not via @NotBlank here,
 * since the same DTO serves both actions).
 */
@Data
public class ApprovalActionDTO {

    @Size(max = 1000)
    private String remarks;
}

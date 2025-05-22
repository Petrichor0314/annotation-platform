package com.mohamedoujdid.annotationplatform.admin.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSummaryResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String roleName;
} 
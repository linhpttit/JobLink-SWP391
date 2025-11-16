package com.joblink.joblink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployerOpenDto {
	private Long employerId;
	private String companyName;
	private String location;
	private String industry;
	private String description;
	private Integer openPositions;
}



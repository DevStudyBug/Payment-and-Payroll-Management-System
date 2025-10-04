package com.aurionpro.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "concerns")
public class ConcernEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long concernId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employeeId", nullable = false)
	private EmployeeEntity employee;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "orgId", nullable = false)
	private OrganizationEntity organization;

	@NotBlank
	@Column(columnDefinition = "TEXT")
	private String description;

	@OneToOne
	@JoinColumn(name = "documentId")
	private DocumentEntity attachmentDoc;

	@NotBlank
	private String status;

	@CreationTimestamp
	private LocalDateTime createdAt;

	private LocalDateTime resolvedAt;
}

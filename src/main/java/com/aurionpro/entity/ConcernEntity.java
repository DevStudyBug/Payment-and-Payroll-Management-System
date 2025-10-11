package com.aurionpro.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employee_concerns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConcernEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long concernId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_id", nullable = false)
	private EmployeeEntity employee;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "org_id", nullable = false)
	private OrganizationEntity organization;

	@Column(nullable = false)
	private String category;

	@Column(nullable = false)
	private String priority;

	@Column(nullable = false, length = 1000)
	private String description;

	private String attachmentUrl;

	@Column(nullable = false)
	private String status;

	@Column(unique = true, updatable = false)
	private String ticketNumber;

	private String adminResponse;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@PrePersist
	public void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	public void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}

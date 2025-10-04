package com.aurionpro.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "documents")
public class DocumentEntity {
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long documentId;

	    @NotBlank
	    private String fileName;

	    @NotBlank
	    private String fileUrl; // Cloudinary

	    @NotBlank
	    private String fileType; // PDF, IMAGE

	    @NotBlank
	    private String status; // PENDING / APPROVED / REJECTED

	    @CreationTimestamp
	    private LocalDateTime uploadedAt;

	    private LocalDateTime verifiedAt;

	    
	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "orgId")
	    private OrganizationEntity organization;

	    
	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "employeeId")
	    private EmployeeEntity employee;
}

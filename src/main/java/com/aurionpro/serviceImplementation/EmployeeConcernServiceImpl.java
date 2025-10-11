package com.aurionpro.serviceImplementation;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.app.exception.NotFoundException;
import com.aurionpro.constants.ConcernConstants;
import com.aurionpro.dto.request.ConcernRequestDto;
import com.aurionpro.dto.response.ConcernResponseDto;
import com.aurionpro.entity.ConcernEntity;
import com.aurionpro.entity.EmployeeEntity;
import com.aurionpro.entity.OrganizationEntity;
import com.aurionpro.repo.ConcernRepository;
import com.aurionpro.repo.EmployeeRepository;
import com.aurionpro.service.CloudinaryService;
import com.aurionpro.service.EmailService;
import com.aurionpro.service.EmployeeConcernService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeConcernServiceImpl implements EmployeeConcernService {
	private final EmployeeRepository employeeRepo;
	private final ConcernRepository concernRepo;
	private final EmailService emailService;
	private final ModelMapper modelMapper;
	private final CloudinaryService cloudinaryService;

	// Raise Concern
	public ConcernResponseDto raiseConcern(String username, ConcernRequestDto request, MultipartFile file) {
		EmployeeEntity emp = employeeRepo.findByUser_Username(username)
				.orElseThrow(() -> new NotFoundException("Employee not found"));

		OrganizationEntity org = emp.getOrganization();
		if (org == null)
			throw new NotFoundException("Employee is not linked to any organization.");

		// Upload file if present
		String attachmentUrl = null;
		if (file != null && !file.isEmpty()) {
			attachmentUrl = cloudinaryService.uploadFile(file);
		}
		String ticketNumber = String.format("CON-%d-%05d", Year.now().getValue(), concernRepo.count() + 1);

		ConcernEntity concern = ConcernEntity.builder().employee(emp).organization(org).ticketNumber(ticketNumber)
				.category(request.getCategory()).priority(request.getPriority()).description(request.getDescription())
				.attachmentUrl(attachmentUrl).status(ConcernConstants.STATUS_OPEN).createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now()).build();

		concernRepo.save(concern);

		sendEmployeeEmail(emp, concern);
		sendAdminEmail(org, emp, concern);

		return ConcernResponseDto.builder().concernId(concern.getConcernId()).ticketNumber(concern.getTicketNumber())
				.category(concern.getCategory()).priority(concern.getPriority()).description(concern.getDescription())
				.status(concern.getStatus()).adminResponse(concern.getAdminResponse())
				.attachmentUrl(concern.getAttachmentUrl())
				.employeeName(concern.getEmployee().getFirstName() + " " + concern.getEmployee().getLastName())
				.organizationName(concern.getOrganization().getOrgName()).createdAt(concern.getCreatedAt())
				.updatedAt(concern.getUpdatedAt()).build();
	}

	// View All Concerns of LoggedIn Employee
	public List<ConcernResponseDto> getMyConcerns(String username) {
		EmployeeEntity emp = employeeRepo.findByUser_Username(username)
				.orElseThrow(() -> new NotFoundException("Employee not found"));
		
		return concernRepo.findByEmployeeWithDetails(emp.getEmployeeId()).stream()
		        .map(c -> ConcernResponseDto.builder()
		                .concernId(c.getConcernId())
		                .employeeName(c.getEmployee().getFirstName() + " " + c.getEmployee().getLastName())
		                .organizationName(c.getOrganization().getOrgName())
		                .ticketNumber(c.getTicketNumber())
		                .category(c.getCategory())
		                .priority(c.getPriority())
		                .description(c.getDescription())
		                .status(c.getStatus())
		                .attachmentUrl(c.getAttachmentUrl())
		                .createdAt(c.getCreatedAt())
		                .updatedAt(c.getUpdatedAt())
		                .build())
		        .collect(Collectors.toList());

	}

	// View Concern by Ticket Number
	public ConcernResponseDto getConcernByTicket(String username, String ticketNumber) {
		EmployeeEntity emp = employeeRepo.findByUser_Username(username)
				.orElseThrow(() -> new NotFoundException("Employee not found"));
		ConcernEntity concern = concernRepo.findByTicketNumber(ticketNumber);
		if (concern == null || !concern.getEmployee().getUser().getUsername().equals(username)) {
			throw new NotFoundException("Concern not found or unauthorized access");
		}
		return ConcernResponseDto.builder().concernId(concern.getConcernId()).ticketNumber(concern.getTicketNumber())
				.category(concern.getCategory()).priority(concern.getPriority()).description(concern.getDescription())
				.status(concern.getStatus()).adminResponse(concern.getAdminResponse())
				.attachmentUrl(concern.getAttachmentUrl())
				.employeeName(concern.getEmployee().getFirstName() + " " + concern.getEmployee().getLastName())
				.organizationName(concern.getOrganization().getOrgName()).createdAt(concern.getCreatedAt())
				.updatedAt(concern.getUpdatedAt()).build();
	}

	// Close Concern
	public ConcernResponseDto acknowledgeConcern(String username, String ticketNumber) {
		ConcernEntity concern = concernRepo.findByTicketNumber(ticketNumber);
		if (concern == null || !concern.getEmployee().getUser().getUsername().equals(username)) {
			throw new NotFoundException("Concern not found or unauthorized access");
		}

		concern.setStatus(ConcernConstants.STATUS_CLOSED);
		concern.setUpdatedAt(LocalDateTime.now());
		concernRepo.save(concern);

		return ConcernResponseDto.builder().concernId(concern.getConcernId()).ticketNumber(concern.getTicketNumber())
				.category(concern.getCategory()).priority(concern.getPriority()).description(concern.getDescription())
				.status(concern.getStatus()).adminResponse(concern.getAdminResponse())
				.attachmentUrl(concern.getAttachmentUrl())
				.employeeName(concern.getEmployee().getFirstName() + " " + concern.getEmployee().getLastName())
				.organizationName(concern.getOrganization().getOrgName()).createdAt(concern.getCreatedAt())
				.updatedAt(concern.getUpdatedAt()).build();
	}

	private void sendEmployeeEmail(EmployeeEntity emp, ConcernEntity concern) {
		emailService.sendConcernSubmissionEmail(emp, concern.getTicketNumber(), concern.getCategory(),
				concern.getPriority(), concern.getDescription());
	}

	private void sendAdminEmail(OrganizationEntity org, EmployeeEntity emp, ConcernEntity concern) {

		String hrEmail = org.getEmail();

		emailService.sendConcernNotificationToHR(hrEmail, emp, concern.getTicketNumber(), concern.getCategory(),
				concern.getPriority(), concern.getDescription());
	}
}

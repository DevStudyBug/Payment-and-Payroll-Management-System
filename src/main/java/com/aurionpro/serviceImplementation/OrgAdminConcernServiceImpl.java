package com.aurionpro.serviceImplementation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.app.exception.NotFoundException;
import com.aurionpro.constants.ConcernConstants;
import com.aurionpro.dto.response.ConcernResponseDto;
import com.aurionpro.entity.ConcernEntity;
import com.aurionpro.repo.ConcernRepository;
import com.aurionpro.service.OrgAdminConcernService;
import com.aurionpro.service.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrgAdminConcernServiceImpl implements OrgAdminConcernService {

    private final ConcernRepository concernRepo;
    private final EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public Page<ConcernResponseDto> getAllConcerns(Long orgId, String status, String priority, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ConcernEntity> concerns = concernRepo.findAllByFilters(orgId, status, priority, pageable);

        List<ConcernResponseDto> dtoList = concerns.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, concerns.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public ConcernResponseDto getConcernByTicket(String ticketNumber) {
        ConcernEntity concern = concernRepo.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new NotFoundException("Concern not found"));
        return mapToDto(concern);
    }

    @Override
    public ConcernResponseDto respondToConcern(String ticketNumber, String response) {
        ConcernEntity concern = concernRepo.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new NotFoundException("Concern not found"));

        validateStatusTransition(concern, ConcernConstants.STATUS_IN_PROGRESS);

        concern.setAdminResponse(response);
        concern.setStatus(ConcernConstants.STATUS_IN_PROGRESS);
        concern.setUpdatedAt(LocalDateTime.now());
        concernRepo.save(concern);

        emailService.sendConcernStatusUpdateEmail(
                concern.getEmployee(),
                concern.getTicketNumber(),
                concern.getStatus(),
                concern.getAdminResponse()
        );

        return mapToDto(concern);
    }

    @Override
    public ConcernResponseDto resolveConcern(String ticketNumber, String response) {
        ConcernEntity concern = concernRepo.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new NotFoundException("Concern not found"));

        validateStatusTransition(concern, ConcernConstants.STATUS_RESOLVED);

        concern.setAdminResponse(response);
        concern.setStatus(ConcernConstants.STATUS_RESOLVED);
        concern.setUpdatedAt(LocalDateTime.now());
        concernRepo.save(concern);

        emailService.sendConcernStatusUpdateEmail(
                concern.getEmployee(),
                concern.getTicketNumber(),
                concern.getStatus(),
                concern.getAdminResponse()
        );

        return mapToDto(concern);
    }

    @Override
    public ConcernResponseDto rejectConcern(String ticketNumber, String response) {
        ConcernEntity concern = concernRepo.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new NotFoundException("Concern not found"));

        validateStatusTransition(concern, ConcernConstants.STATUS_REJECTED);

        concern.setAdminResponse(response);
        concern.setStatus(ConcernConstants.STATUS_REJECTED);
        concern.setUpdatedAt(LocalDateTime.now());
        concernRepo.save(concern);

        emailService.sendConcernStatusUpdateEmail(
                concern.getEmployee(),
                concern.getTicketNumber(),
                concern.getStatus(),
                concern.getAdminResponse()
        );

        return mapToDto(concern);
    }

   
    //  Helper Methods
    
    private ConcernResponseDto mapToDto(ConcernEntity c) {
        return ConcernResponseDto.builder()
                .concernId(c.getConcernId())
                .ticketNumber(c.getTicketNumber())
                .category(c.getCategory())
                .priority(c.getPriority())
                .description(c.getDescription())
                .status(c.getStatus())
                .adminResponse(c.getAdminResponse())
                .attachmentUrl(c.getAttachmentUrl())
                .employeeName(c.getEmployee() != null
                        ? c.getEmployee().getFirstName() + " " + c.getEmployee().getLastName()
                        : null)
                .organizationName(c.getOrganization() != null ? c.getOrganization().getOrgName() : null)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private void validateStatusTransition(ConcernEntity concern, String newStatus) {
        String current = concern.getStatus();

        
        if (ConcernConstants.STATUS_CLOSED.equalsIgnoreCase(current)) {
            throw new IllegalStateException("This concern is already closed and cannot be updated further.");
        }

      
        if ((ConcernConstants.STATUS_RESOLVED.equalsIgnoreCase(current)
                || ConcernConstants.STATUS_REJECTED.equalsIgnoreCase(current))
                && !ConcernConstants.STATUS_REOPENED.equalsIgnoreCase(newStatus)) {
            throw new IllegalStateException("Resolved or rejected concerns cannot be modified further unless reopened by the employee.");
        }

       
        if (current.equalsIgnoreCase(newStatus)) {
            throw new IllegalStateException("This concern is already marked as " + newStatus + ".");
        }
    }
}

package com.aurionpro.serviceImplementation;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.app.exception.DuplicateResourceException;
import com.aurionpro.app.exception.InvalidOperationException;
import com.aurionpro.app.exception.NotFoundException;
import com.aurionpro.dto.request.ChangePasswordRequestDto;
import com.aurionpro.dto.request.EmployeeRegisterRequestDto;
import com.aurionpro.dto.request.LoginRequestDto;
import com.aurionpro.dto.request.OrgRegisterRequestDto;
import com.aurionpro.dto.response.EmployeeBulkRegisterResponseDto;
import com.aurionpro.dto.response.EmployeeRegisterResponseDto;
import com.aurionpro.dto.response.LoginResponseDto;
import com.aurionpro.dto.response.OrgRegisterResponseDto;
import com.aurionpro.entity.DesignationEntity;
import com.aurionpro.entity.EmployeeEntity;
import com.aurionpro.entity.EmployeeSalaryEntity;
import com.aurionpro.entity.OrganizationEntity;
import com.aurionpro.entity.SalaryTemplateEntity;
import com.aurionpro.entity.UserEntity;
import com.aurionpro.entity.UserRoleEntity;
import com.aurionpro.entity.VerificationTokenEntity;
import com.aurionpro.repo.EmployeeRepository;
import com.aurionpro.repo.EmployeeSalaryRepository;
import com.aurionpro.repo.OrganizationRepository;
import com.aurionpro.repo.SalaryTemplateRepository;
import com.aurionpro.repo.UserRepository;
import com.aurionpro.repo.VerificationTokenRepository;
import com.aurionpro.security.JwtService;
import com.aurionpro.service.AuthService;
import com.aurionpro.service.EmailService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImplementation implements AuthService {
	private final UserRepository userRepository;
	private final OrganizationRepository organizationRepository;
	private final EmployeeRepository employeeRepository;
	private final SalaryTemplateRepository salaryTemplateRepository;
	private final EmployeeSalaryRepository employeeSalaryRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailService emailService;
	private final VerificationTokenRepository tokenRepo;
	private final ModelMapper modelMapper;
	private final JwtService jwtService;

	@Override
	public OrgRegisterResponseDto registerOrganization(OrgRegisterRequestDto req) {
		if (userRepository.findByEmail(req.getEmail()).isPresent()) {
			throw new DuplicateResourceException("Email '" + req.getEmail() + "' is already registered.");
		}

		if (organizationRepository.findByRegistrationNo(req.getRegistrationNo()).isPresent()) {
			throw new DuplicateResourceException(
					"Organization registration number '" + req.getRegistrationNo() + "' is already in use.");
		}
		OrganizationEntity org = modelMapper.map(req, OrganizationEntity.class);
		org.setStatus("PENDING");

		UserEntity user = new UserEntity();
		user.setUsername(req.getUsername());
		user.setEmail(req.getEmail());
		user.setPassword(passwordEncoder.encode(req.getPassword()));
		user.setStatus("PENDING");

		UserRoleEntity role = new UserRoleEntity();
		role.setRole("ORG_ADMIN");
		role.setUser(user);
		user.getRoles().add(role);

		userRepository.save(user);

		org.setUser(user);
		organizationRepository.save(org);

		// Generate verification token
		String token = UUID.randomUUID().toString();
		VerificationTokenEntity vToken = new VerificationTokenEntity(token, user);
		tokenRepo.save(vToken);

		// Send verification email
		try {
			emailService.sendVerificationEmail(user.getEmail(), token);
		} catch (Exception e) {
			throw new InvalidOperationException("failed to send verification email. Please try again later.");
		}
		OrgRegisterResponseDto response = modelMapper.map(org, OrgRegisterResponseDto.class);
		response.setEmail(user.getEmail());
		response.setMessage("Organization registered successfully. Please verify your email.");

		return response;
	}

	@Override
	public LoginResponseDto loginOrganization(LoginRequestDto req) {
		UserEntity user = userRepository.findByUsername(req.getUserName())
				.orElseThrow(() -> new NotFoundException("No account found with username: " + req.getUserName()));

		if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
			throw new InvalidOperationException("Please verify your email before logging in.");
		}

		if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
			throw new InvalidOperationException("Invalid username or password.");
		}

		// Collect all roles
		Set<String> roleNames = user.getRoles().stream().map(UserRoleEntity::getRole).collect(Collectors.toSet());

		// Check if Employee and first time login
		if (roleNames.contains("EMPLOYEE") && user.isFirstLogin()) {
			return LoginResponseDto.builder().userId(user.getUserId()).email(user.getEmail()).roles(roleNames)
					.message("First-time login detected. Please change your password before continuing.")
					.status("FIRST_TIME_LOGIN").build();
		}

		// Generate JWT with roles
		String jwt = jwtService
				.generateToken(
						org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
								.password(user.getPassword()).authorities(roleNames.toArray(new String[0])).build(),
						roleNames);

		return LoginResponseDto.builder().token(jwt).userId(user.getUserId()).email(user.getEmail()).roles(roleNames)
				.message("Login successful.").status("SUCCESS").build();
	}

	@Override
	public EmployeeRegisterResponseDto registerEmployee(
	        Authentication authentication, EmployeeRegisterRequestDto request) {

	    // ✅ 1️⃣ Duplicate email check
	    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
	        throw new DuplicateResourceException(
	                "Email '" + request.getEmail() + "' is already registered.");
	    }

	    // ✅ 2️⃣ Verify logged-in user and organization
	    UserEntity orgAdmin = userRepository.findByUsername(authentication.getName())
	            .orElseThrow(() -> new NotFoundException("User not found"));

	    OrganizationEntity org = orgAdmin.getOrganization();
	    if (org == null) {
	        throw new NotFoundException("User is not associated with any organization.");
	    }

	    // ✅ 3️⃣ Validate age (must be 20+)
	    if (request.getDob() != null) {
	        int age = Period.between(request.getDob(), LocalDate.now()).getYears();
	        if (age < 20) {
	            throw new IllegalArgumentException("Employee must be at least 20 years old.");
	        }
	    }

	    // ✅ 4️⃣ Validate designation exists in organization
	    DesignationEntity designationEntity = org.getDesignations().stream()
	            .filter(d -> d.getName().equalsIgnoreCase(request.getDesignation()))
	            .findFirst()
	            .orElseThrow(() -> new NotFoundException(
	                    "Designation '" + request.getDesignation() + "' not found for this organization."));

	    // ✅ 5️⃣ Check salary template availability for that designation
	    Optional<SalaryTemplateEntity> templateOpt =
	            salaryTemplateRepository.findByOrganizationAndDesignation(org, designationEntity);

	    if (templateOpt.isEmpty()) {
	        throw new IllegalArgumentException(
	                "No salary template found for designation: " + request.getDesignation());
	    }

	    // ✅ 6️⃣ Generate unique username and temporary password
	    String baseUsername = (request.getFirstName() + "." + request.getLastName()).toLowerCase();
	    String username = generateUniqueUsername(baseUsername, org.getOrgId());

	    String rawPassword = generateRandomPassword(10);
	    String hashedPassword = passwordEncoder.encode(rawPassword);

	    // ✅ 7️⃣ Create UserEntity
	    UserEntity userEntity = new UserEntity();
	    userEntity.setUsername(username);
	    userEntity.setPassword(hashedPassword);
	    userEntity.setEmail(request.getEmail());
	    userEntity.setFirstLogin(true);
	    userEntity.setStatus("INACTIVE");

	    // Assign EMPLOYEE role
	    UserRoleEntity role = new UserRoleEntity();
	    role.setRole("EMPLOYEE");
	    role.setUser(userEntity);
	    userEntity.getRoles().add(role);
	    userRepository.save(userEntity);

	    // ✅ 8️⃣ Create EmployeeEntity
	    EmployeeEntity employee = new EmployeeEntity();
	    employee.setUser(userEntity);
	    employee.setOrganization(org);
	    employee.setFirstName(request.getFirstName());
	    employee.setLastName(request.getLastName());
	    employee.setDob(request.getDob());
	    employee.setDepartment(request.getDepartment());
	    employee.setDesignation(request.getDesignation());
	    employee.setStatus("PENDING");
	    employeeRepository.save(employee);

	    // ✅ 9️⃣ Link Salary Template (mandatory since already validated)
	    SalaryTemplateEntity template = templateOpt.get();
	    EmployeeSalaryEntity salary = new EmployeeSalaryEntity();
	    salary.setEmployee(employee);
	    salary.setTemplate(template);
	    salary.setCustomAllowances(null);
	    employeeSalaryRepository.save(salary);

	    // ✅ 🔟 Create verification token
	    String token = UUID.randomUUID().toString();
	    VerificationTokenEntity vToken = new VerificationTokenEntity(token, userEntity);
	    tokenRepo.save(vToken);

	    String verificationLink = "http://localhost:8080/api/v1/auth/verify-email?token=" + token;

	    // ✅ 1️⃣1️⃣ Send verification email
	    try {
	        emailService.sendVerificationEmail(
	                request.getEmail(), username, rawPassword, verificationLink);
	    } catch (Exception e) {
	        throw new InvalidOperationException(
	                "Failed to send verification email. Please try again later.");
	    }

	    return EmployeeRegisterResponseDto.builder()
	            .username(username)
	            .temporaryPassword(rawPassword)
	            .status(employee.getStatus())
	            .build();
	}


	@Override
	@Transactional
	public EmployeeBulkRegisterResponseDto registerEmployeesInBulk(OrganizationEntity org,
			List<EmployeeRegisterRequestDto> employeeRequests) {

		List<EmployeeRegisterResponseDto> successList = new ArrayList<>();
		List<String> failureList = new ArrayList<>();

		List<UserEntity> usersToSave = new ArrayList<>();
		List<EmployeeEntity> employeesToSave = new ArrayList<>();
		List<Runnable> asyncEmailTasks = new ArrayList<>();
		List<UserEntity> savedUsers = new ArrayList<>();

		Map<String, String> tempPasswords = new HashMap<>();

		// ✅ Step 1️⃣: Preload all valid designations that have salary templates for
		// this org
		Set<String> validDesignations = salaryTemplateRepository.findAllByOrganization(org).stream()
				.map(SalaryTemplateEntity::getDesignation).filter(Objects::nonNull).map(DesignationEntity::getName)
				.filter(Objects::nonNull).map(String::trim).map(String::toLowerCase).collect(Collectors.toSet());

		// ✅ Step 2️⃣: Iterate over employees
		for (int i = 0; i < employeeRequests.size(); i++) {
			EmployeeRegisterRequestDto req = employeeRequests.get(i);

			try {
				// ✅ Validate age
				if (req.getDob() != null && Period.between(req.getDob(), LocalDate.now()).getYears() < 20)
					throw new IllegalArgumentException("Employee must be at least 20 years old.");

				// ✅ Validate salary template availability
				String designation = Optional.ofNullable(req.getDesignation()).map(String::trim)
						.map(String::toLowerCase).orElse("");

				if (!validDesignations.contains(designation)) {
					throw new IllegalArgumentException(
							"No salary template found for designation: " + req.getDesignation());
				}

				// ✅ Generate username & password
				String baseUsername = (req.getFirstName() + "." + req.getLastName()).toLowerCase();
				String username = generateUniqueUsername(baseUsername, org.getOrgId());
				String rawPassword = generateRandomPassword(10);
				String encodedPassword = passwordEncoder.encode(rawPassword);

				tempPasswords.put(username, rawPassword);

				// ✅ Create user
				UserEntity user = new UserEntity();
				user.setUsername(username);
				user.setPassword(encodedPassword);
				user.setEmail(req.getEmail());
				user.setFirstLogin(true);
				user.setStatus("INACTIVE");

				UserRoleEntity role = new UserRoleEntity();
				role.setRole("EMPLOYEE");
				role.setUser(user);
				user.getRoles().add(role);
				usersToSave.add(user);

				// ✅ Create employee
				EmployeeEntity emp = new EmployeeEntity();
				emp.setUser(user);
				emp.setOrganization(org);
				emp.setFirstName(req.getFirstName());
				emp.setLastName(req.getLastName());
				emp.setDob(req.getDob());
				emp.setDepartment(req.getDepartment());
				emp.setDesignation(req.getDesignation());
				emp.setStatus("PENDING");
				employeesToSave.add(emp);

				successList.add(EmployeeRegisterResponseDto.builder().username(username).temporaryPassword(rawPassword)
						.status(emp.getStatus()).build());

			} catch (Exception e) {
				failureList.add("Row " + (i + 1) + ": " + e.getMessage());
			}

			// ✅ Save in batches (to optimize DB I/O)
			if (usersToSave.size() >= 10 || i == employeeRequests.size() - 1) {
				userRepository.saveAll(usersToSave);
				employeeRepository.saveAll(employeesToSave);

				userRepository.flush();
				employeeRepository.flush();

				savedUsers.addAll(usersToSave);
				usersToSave.clear();
				employeesToSave.clear();
			}
		}

		// ✅ Step 3️⃣: Send verification emails asynchronously
		for (UserEntity user : savedUsers) {
			try {
				String token = UUID.randomUUID().toString();
				VerificationTokenEntity vToken = new VerificationTokenEntity(token, user);
				tokenRepo.save(vToken);

				String verificationLink = "http://localhost:8080/api/v1/auth/verify-email?token=" + token;
				String tempPassword = tempPasswords.get(user.getUsername());

				asyncEmailTasks.add(() -> {
					try {
						emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), tempPassword,
								verificationLink);
					} catch (Exception ex) {
						System.err.println("⚠️ Failed to send email to " + user.getUsername() + ": " + ex.getMessage());
					}
				});

			} catch (Exception e) {
				System.err.println("⚠️ Failed to create token for user " + user.getUsername() + ": " + e.getMessage());
			}
		}

		asyncEmailTasks.forEach(Runnable::run);
		tempPasswords.clear();

		return EmployeeBulkRegisterResponseDto.builder().successfulRegistrations(successList)
				.failedRegistrations(failureList).build();
	}

	@Override
	public EmployeeBulkRegisterResponseDto registerEmployeesFromExcel(Authentication authentication,
			MultipartFile file) {
		UserEntity orgAdmin = userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new NotFoundException("User not found"));

		OrganizationEntity org = orgAdmin.getOrganization();
		if (org == null) {
			throw new NotFoundException("User is not associated with any organization.");
		}

		List<EmployeeRegisterRequestDto> validEmployees = new ArrayList<>();
		List<String> errors = new ArrayList<>();

		// Validate file type
		if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
			throw new InvalidOperationException("Invalid file type. Please upload a valid .xlsx Excel file.");
		}

		try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
			Sheet sheet = workbook.getSheetAt(0);

			if (sheet.getPhysicalNumberOfRows() <= 1) {
				throw new InvalidOperationException("Excel file is empty or missing data rows.");
			}

			// Validate header row
			Row header = sheet.getRow(0);
			List<String> expectedHeaders = List.of("First Name", "Last Name", "DOB", "Department", "Designation",
					"Email");
			for (int i = 0; i < expectedHeaders.size(); i++) {
				String actualHeader = getCellString(header, i);
				if (actualHeader == null || !actualHeader.equalsIgnoreCase(expectedHeaders.get(i))) {
					throw new InvalidOperationException("Invalid Excel header at column " + (i + 1) + ". Expected: '"
							+ expectedHeaders.get(i) + "', Found: '" + actualHeader + "'");
				}
			}

			// Process rows
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row == null)
					continue;

				try {
					EmployeeRegisterRequestDto dto = parseRow(row, i);
					validateRow(dto, i);
					validEmployees.add(dto);
				} catch (IllegalArgumentException e) {
					errors.add(e.getMessage());
				} catch (Exception e) {
					errors.add("Row " + (i + 1) + ": Unexpected error - " + e.getMessage());
				}
			}

		} catch (IOException e) {
			throw new RuntimeException("Failed to read Excel file: " + e.getMessage());
		}

		// If no valid rows
		if (validEmployees.isEmpty()) {
			throw new InvalidOperationException("No valid employee records found. Errors: " + errors);
		}

		EmployeeBulkRegisterResponseDto result = registerEmployeesInBulk(org, validEmployees);
		if (result.getFailedRegistrations() == null)
			result.setFailedRegistrations(new ArrayList<>());
		result.getFailedRegistrations().addAll(errors);

		return result;
	}

	@Override
	public void changePassword(ChangePasswordRequestDto request) {
	   
	    UserEntity user = userRepository.findByEmail(request.getEmail())
	            .orElseThrow(() -> new NotFoundException("User not found with email: " + request.getEmail()));

	    // 🔹 Validate old password
	    if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
	        throw new InvalidOperationException("Old password is incorrect.");
	    }

	    // 🔹 Update password and user info
	    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
	    user.setFirstLogin(false);
	    user.setPasswordChanged(true);
	    user.setLastPasswordChangedAt(LocalDateTime.now());

	    userRepository.save(user);
	}


	// Helper function Execel upload

	private EmployeeRegisterRequestDto parseRow(Row row, int rowIndex) {
		try {
			String firstName = getCellString(row, 0);
			String lastName = getCellString(row, 1);
			LocalDate dob = getCellDate(row, 2, rowIndex);
			String department = getCellString(row, 3);
			String designation = getCellString(row, 4);
			String email = getCellString(row, 5);

			return EmployeeRegisterRequestDto.builder().firstName(firstName).lastName(lastName).dob(dob)
					.department(department).designation(designation).email(email).build();
		} catch (Exception e) {
			throw new IllegalArgumentException("Row " + (rowIndex + 1) + ": " + e.getMessage());
		}
	}

	// Validate each DTO’s fields with clear error messages

	private void validateRow(EmployeeRegisterRequestDto dto, int rowIndex) {
		String rowMsg = "Row " + (rowIndex + 1) + ": ";

		if (dto.getFirstName() == null || dto.getFirstName().isBlank())
			throw new IllegalArgumentException(rowMsg + "First Name is required.");

		if (dto.getLastName() == null || dto.getLastName().isBlank())
			throw new IllegalArgumentException(rowMsg + "Last Name is required.");

		if (dto.getDob() == null)
			throw new IllegalArgumentException(rowMsg + "Date of Birth is required.");

		int age = Period.between(dto.getDob(), LocalDate.now()).getYears();
		if (age < 20)
			throw new IllegalArgumentException(rowMsg + "Employee must be at least 20 years old.");

		if (dto.getDepartment() == null || dto.getDepartment().isBlank())
			throw new IllegalArgumentException(rowMsg + "Department is required.");

		if (dto.getDesignation() == null || dto.getDesignation().isBlank())
			throw new IllegalArgumentException(rowMsg + "Designation is required.");

		if (dto.getEmail() == null || dto.getEmail().isBlank())
			throw new IllegalArgumentException(rowMsg + "Email is required.");

		if (!dto.getEmail().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"))
			throw new IllegalArgumentException(rowMsg + "Invalid email format.");

		if (userRepository.findByEmail(dto.getEmail()).isPresent())
			throw new IllegalArgumentException(rowMsg + "Email already exists in the system.");
	}

	/**
	 * Safely read string value from cell
	 */
	private String getCellString(Row row, int cellIndex) {
		Cell cell = row.getCell(cellIndex);
		if (cell == null)
			return null;
		cell.setCellType(CellType.STRING);
		return cell.getStringCellValue().trim();
	}

	/**
	 * Safely read date value from cell (either Excel date or string ISO format)
	 */
	private LocalDate getCellDate(Row row, int cellIndex, int rowIndex) {
		Cell cell = row.getCell(cellIndex);
		if (cell == null)
			return null;

		try {
			if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
				return cell.getLocalDateTimeCellValue().toLocalDate();
			} else if (cell.getCellType() == CellType.STRING) {
				String value = cell.getStringCellValue().trim();
				return LocalDate.parse(value); // must be yyyy-MM-dd
			} else {
				throw new IllegalArgumentException("Invalid date format in DOB column.");
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Row " + (rowIndex + 1) + ": Invalid or unreadable DOB value.");
		}
	}

	// Helper Function
	private String generateUniqueUsername(String baseUsername, Long orgId) {
		int suffix = 1;
		String username = baseUsername;
		while (userRepository.existsByUsername(username)) {
			username = baseUsername + suffix++;
		}
		return username;
	}

	private String generateRandomPassword(int length) {
		final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#&!";
		SecureRandom random = new SecureRandom();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(chars.charAt(random.nextInt(chars.length())));
		}
		return sb.toString();
	}

}

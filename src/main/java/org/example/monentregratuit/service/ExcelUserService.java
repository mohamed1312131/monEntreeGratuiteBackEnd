package org.example.monentregratuit.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.example.monentregratuit.DTO.EmailSendResponse;
import org.example.monentregratuit.DTO.ExcelUploadResponse;
import org.example.monentregratuit.DTO.ExcelUserDTO;
import org.example.monentregratuit.entity.ExcelUser;
import org.example.monentregratuit.entity.Foire;
import org.example.monentregratuit.repo.ExcelUserRepository;
import org.example.monentregratuit.repo.FoireRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ExcelUserService {

    private final ExcelUserRepository excelUserRepository;
    private final FoireRepository foireRepository;
    private final EmailTemplateService emailTemplateService;

    @org.springframework.beans.factory.annotation.Value("${app.frontend.url}")
    private String frontendUrl;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public ExcelUserService(ExcelUserRepository excelUserRepository, 
                           FoireRepository foireRepository,
                           EmailTemplateService emailTemplateService) {
        this.excelUserRepository = excelUserRepository;
        this.foireRepository = foireRepository;
        this.emailTemplateService = emailTemplateService;
    }

    public ExcelUploadResponse uploadExcelFile(MultipartFile file, Long foireId) {
        ExcelUploadResponse response = new ExcelUploadResponse();
        List<String> errors = new ArrayList<>();
        int totalRows = 0;
        int successfulRows = 0;
        int failedRows = 0;

        try {
            Foire foire = foireRepository.findById(foireId)
                    .orElseThrow(() -> new IllegalArgumentException("Foire not found with ID: " + foireId));

            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
                response.setSuccess(false);
                response.setMessage("Invalid file format. Only .xlsx and .xls files are allowed.");
                return response;
            }

            Workbook workbook = getWorkbook(file.getInputStream(), fileName);
            Sheet sheet = workbook.getSheetAt(0);

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                response.setSuccess(false);
                response.setMessage("Excel file is empty or has no header row.");
                workbook.close();
                return response;
            }

            Map<String, Integer> columnIndexMap = getColumnIndexMap(headerRow);

            if (!validateHeaders(columnIndexMap, errors)) {
                response.setSuccess(false);
                response.setMessage("Missing required columns in Excel file.");
                response.setErrors(errors);
                workbook.close();
                return response;
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                totalRows++;

                try {
                    ExcelUser excelUser = parseRowToExcelUser(row, columnIndexMap, foire);
                    
                    if (validateExcelUser(excelUser, errors, i)) {
                        if (excelUserRepository.existsByEmailAndFoireId(excelUser.getEmail(), foireId)) {
                            errors.add("Row " + (i + 1) + ": Email already exists for this foire");
                            failedRows++;
                        } else {
                            excelUserRepository.save(excelUser);
                            successfulRows++;
                        }
                    } else {
                        failedRows++;
                    }
                } catch (Exception e) {
                    errors.add("Row " + (i + 1) + ": " + e.getMessage());
                    failedRows++;
                }
            }

            workbook.close();

            response.setSuccess(successfulRows > 0);
            response.setMessage(successfulRows + " rows imported successfully, " + failedRows + " rows failed.");
            response.setTotalRows(totalRows);
            response.setSuccessfulRows(successfulRows);
            response.setFailedRows(failedRows);
            response.setErrors(errors);

        } catch (IOException e) {
            response.setSuccess(false);
            response.setMessage("Error reading Excel file: " + e.getMessage());
            errors.add(e.getMessage());
            response.setErrors(errors);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error processing Excel file: " + e.getMessage());
            errors.add(e.getMessage());
            response.setErrors(errors);
        }

        return response;
    }

    private Workbook getWorkbook(InputStream inputStream, String fileName) throws IOException {
        if (fileName.endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        } else if (fileName.endsWith(".xls")) {
            return new HSSFWorkbook(inputStream);
        } else {
            throw new IllegalArgumentException("Invalid file format");
        }
    }

    private Map<String, Integer> getColumnIndexMap(Row headerRow) {
        Map<String, Integer> columnIndexMap = new HashMap<>();
        for (Cell cell : headerRow) {
            String headerValue = getCellValueAsString(cell).trim().toLowerCase();
            columnIndexMap.put(headerValue, cell.getColumnIndex());
        }
        return columnIndexMap;
    }

    private boolean validateHeaders(Map<String, Integer> columnIndexMap, List<String> errors) {
        List<String> requiredHeaders = Arrays.asList("nom", "email");
        boolean valid = true;

        for (String header : requiredHeaders) {
            if (!columnIndexMap.containsKey(header)) {
                errors.add("Missing required column: " + header);
                valid = false;
            }
        }
        return valid;
    }

    private ExcelUser parseRowToExcelUser(Row row, Map<String, Integer> columnIndexMap, Foire foire) {
        ExcelUser excelUser = new ExcelUser();
        excelUser.setFoire(foire);

        excelUser.setNom(getCellValue(row, columnIndexMap, "nom"));
        excelUser.setEmail(getCellValue(row, columnIndexMap, "email"));
        excelUser.setDate(getCellValue(row, columnIndexMap, "date"));
        excelUser.setHeure(getCellValue(row, columnIndexMap, "heure"));
        excelUser.setCode(getCellValue(row, columnIndexMap, "code"));

        return excelUser;
    }

    private String getCellValue(Row row, Map<String, Integer> columnIndexMap, String columnName) {
        Integer columnIndex = columnIndexMap.get(columnName);
        if (columnIndex == null) {
            return null;
        }
        Cell cell = row.getCell(columnIndex);
        return cell != null ? getCellValueAsString(cell).trim() : null;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    private boolean isRowEmpty(Row row) {
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell).trim();
                if (!value.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean validateExcelUser(ExcelUser excelUser, List<String> errors, int rowNumber) {
        boolean valid = true;

        if (excelUser.getNom() == null || excelUser.getNom().isEmpty()) {
            errors.add("Row " + (rowNumber + 1) + ": Nom is required");
            valid = false;
        }

        if (excelUser.getEmail() == null || excelUser.getEmail().isEmpty()) {
            errors.add("Row " + (rowNumber + 1) + ": Email is required");
            valid = false;
        } else if (!EMAIL_PATTERN.matcher(excelUser.getEmail()).matches()) {
            errors.add("Row " + (rowNumber + 1) + ": Invalid email format");
            valid = false;
        }

        return valid;
    }

    public List<ExcelUserDTO> getAllExcelUsers() {
        return excelUserRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ExcelUserDTO> getExcelUsersByFoireId(Long foireId) {
        return excelUserRepository.findByFoireId(foireId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ExcelUserDTO convertToDTO(ExcelUser excelUser) {
        ExcelUserDTO dto = new ExcelUserDTO();
        dto.setId(excelUser.getId());
        dto.setNom(excelUser.getNom());
        dto.setEmail(excelUser.getEmail());
        dto.setDate(excelUser.getDate());
        dto.setHeure(excelUser.getHeure());
        dto.setCode(excelUser.getCode());
        dto.setFoireId(excelUser.getFoire().getId());
        dto.setFoireName(excelUser.getFoire().getName());
        dto.setCreatedAt(excelUser.getCreatedAt());
        return dto;
    }

    public EmailSendResponse sendEmailsToSelectedUsers(Long templateId, List<Long> userIds) {
        try {
            List<ExcelUser> users = excelUserRepository.findAllById(userIds);

            if (users.isEmpty()) {
                return EmailSendResponse.builder()
                    .success(false)
                    .message("Aucun utilisateur trouvé")
                    .totalEmails(0)
                    .successfulEmails(0)
                    .failedEmails(0)
                    .build();
            }

            int totalEmails = users.size();
            int successfulEmails = 0;
            int failedEmails = 0;
            List<String> errors = new ArrayList<>();

            for (ExcelUser user : users) {
                try {
                    // Format date and time to be human-readable
                    String formattedDate = formatDate(user.getDate());
                    String formattedTime = formatTime(user.getHeure());
                    
                    emailTemplateService.sendEmailWithTemplate(
                        templateId,
                        user.getEmail(),
                        user.getNom(),
                        formattedDate,
                        formattedTime,
                        user.getCode(),
                        user.getFoire().getName()
                    );
                    successfulEmails++;
                } catch (Exception e) {
                    failedEmails++;
                    errors.add("Failed to send email to " + user.getEmail() + ": " + e.getMessage());
                    System.err.println("Failed to send email to " + user.getEmail() + ": " + e.getMessage());
                }
            }

            return EmailSendResponse.builder()
                .success(successfulEmails > 0)
                .message(successfulEmails + " emails envoyés avec succès, " + failedEmails + " échoués.")
                .totalEmails(totalEmails)
                .successfulEmails(successfulEmails)
                .failedEmails(failedEmails)
                .errors(errors)
                .build();

        } catch (Exception e) {
            return EmailSendResponse.builder()
                .success(false)
                .message("Erreur lors de l'envoi des emails: " + e.getMessage())
                .totalEmails(0)
                .successfulEmails(0)
                .failedEmails(0)
                .build();
        }
    }

    public EmailSendResponse sendEmailsToAllUsers(Long foireId, String templateKey) {
        try {
            List<ExcelUser> users = foireId != null 
                ? excelUserRepository.findByFoireId(foireId)
                : excelUserRepository.findAll();

            if (users.isEmpty()) {
                return EmailSendResponse.builder()
                    .success(false)
                    .message("No users found to send emails.")
                    .totalEmails(0)
                    .successfulEmails(0)
                    .failedEmails(0)
                    .build();
            }

            int totalEmails = users.size();
            int successfulEmails = 0;
            int failedEmails = 0;

            for (ExcelUser user : users) {
                try {
                    Map<String, String> variables = new HashMap<>();
                    variables.put("Customer Name", user.getNom());
                    variables.put("Email", user.getEmail());
                    variables.put("Date", user.getDate() != null ? user.getDate() : "N/A");
                    variables.put("Heure", user.getHeure() != null ? user.getHeure() : "N/A");
                    variables.put("Code", user.getCode() != null ? user.getCode() : "N/A");
                    variables.put("Fair Name", user.getFoire().getName());
                    variables.put("Frontend URL", frontendUrl);

                    emailTemplateService.sendBulkEmails(
                        Collections.singletonList(user.getEmail()),
                        templateKey,
                        variables
                    );
                    successfulEmails++;
                } catch (Exception e) {
                    failedEmails++;
                    System.err.println("Failed to send email to " + user.getEmail() + ": " + e.getMessage());
                }
            }

            return EmailSendResponse.builder()
                .success(successfulEmails > 0)
                .message(successfulEmails + " emails sent successfully, " + failedEmails + " failed.")
                .totalEmails(totalEmails)
                .successfulEmails(successfulEmails)
                .failedEmails(failedEmails)
                .build();

        } catch (Exception e) {
            return EmailSendResponse.builder()
                .success(false)
                .message("Error sending emails: " + e.getMessage())
                .totalEmails(0)
                .successfulEmails(0)
                .failedEmails(0)
                .build();
        }
    }

    public void deleteExcelUser(Long id) {
        if (!excelUserRepository.existsById(id)) {
            throw new RuntimeException("Excel user not found with ID: " + id);
        }
        excelUserRepository.deleteById(id);
    }

    public void deleteExcelUsersByFoireId(Long foireId) {
        List<ExcelUser> users = excelUserRepository.findByFoireId(foireId);
        excelUserRepository.deleteAll(users);
    }

    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "";
        }
        
        try {
            // Try to parse various date formats
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH);
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            // If parsing fails, try to return as is or extract readable parts
            try {
                // Try simple date format
                SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = simpleFormat.parse(dateStr);
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH);
                return outputFormat.format(date);
            } catch (ParseException ex) {
                // Return original if all parsing fails
                return dateStr;
            }
        }
    }

    private String formatTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return "";
        }
        
        try {
            // Try to parse time from full date-time string
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm");
            Date time = inputFormat.parse(timeStr);
            return outputFormat.format(time);
        } catch (ParseException e) {
            // If parsing fails, try to extract time part
            try {
                SimpleDateFormat simpleFormat = new SimpleDateFormat("HH:mm:ss");
                Date time = simpleFormat.parse(timeStr);
                SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm");
                return outputFormat.format(time);
            } catch (ParseException ex) {
                // Return original if all parsing fails
                return timeStr;
            }
        }
    }
}

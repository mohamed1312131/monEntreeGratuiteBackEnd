package org.example.monentregratuit.controller;

import org.example.monentregratuit.DTO.EmailSendResponse;
import org.example.monentregratuit.DTO.ExcelUploadResponse;
import org.example.monentregratuit.DTO.ExcelUserDTO;
import org.example.monentregratuit.service.ExcelUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/excel-users")
public class ExcelUserController {

    private final ExcelUserService excelUserService;

    public ExcelUserController(ExcelUserService excelUserService) {
        this.excelUserService = excelUserService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ExcelUploadResponse> uploadExcelFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("foireId") Long foireId) {
        
        if (file.isEmpty()) {
            ExcelUploadResponse response = new ExcelUploadResponse();
            response.setSuccess(false);
            response.setMessage("Please select a file to upload.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            ExcelUploadResponse response = excelUserService.uploadExcelFile(file, foireId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ExcelUploadResponse response = new ExcelUploadResponse();
            response.setSuccess(false);
            response.setMessage("Error uploading file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<List<ExcelUserDTO>> getAllExcelUsers() {
        try {
            List<ExcelUserDTO> users = excelUserService.getAllExcelUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/foire/{foireId}")
    public ResponseEntity<List<ExcelUserDTO>> getExcelUsersByFoireId(@PathVariable Long foireId) {
        try {
            List<ExcelUserDTO> users = excelUserService.getExcelUsersByFoireId(foireId);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/send-emails")
    public ResponseEntity<EmailSendResponse> sendEmailsToAllUsers(
            @RequestParam(required = false) Long foireId,
            @RequestParam(defaultValue = "excel-user-email") String templateKey) {
        
        try {
            EmailSendResponse response = excelUserService.sendEmailsToAllUsers(foireId, templateKey);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            EmailSendResponse response = new EmailSendResponse();
            response.setSuccess(false);
            response.setMessage("Error sending emails: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/send-emails-selected")
    public ResponseEntity<EmailSendResponse> sendEmailsToSelectedUsers(@RequestBody Map<String, Object> request) {
        try {
            Long templateId = Long.valueOf(request.get("templateId").toString());
            @SuppressWarnings("unchecked")
            List<Long> userIds = (List<Long>) request.get("userIds");

            EmailSendResponse response = excelUserService.sendEmailsToSelectedUsers(templateId, userIds);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            EmailSendResponse response = new EmailSendResponse();
            response.setSuccess(false);
            response.setMessage("Erreur lors de l'envoi des emails: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteExcelUser(@PathVariable Long id) {
        try {
            excelUserService.deleteExcelUser(id);
            return ResponseEntity.ok("Excel user deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting excel user: " + e.getMessage());
        }
    }

    @DeleteMapping("/foire/{foireId}")
    public ResponseEntity<String> deleteExcelUsersByFoireId(@PathVariable Long foireId) {
        try {
            excelUserService.deleteExcelUsersByFoireId(foireId);
            return ResponseEntity.ok("All excel users for foire deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting excel users: " + e.getMessage());
        }
    }
}

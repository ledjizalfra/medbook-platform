package it.pegaso.projectwork.medbook.clinic.controller.assignment;

import it.pegaso.projectwork.medbook.clinic.server.api.AssignmentsApi;
import it.pegaso.projectwork.medbook.clinic.server.model.*;
import it.pegaso.projectwork.medbook.clinic.service.assignment.AssignmentService;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiVoidResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AssignmentController implements AssignmentsApi {

    private final AssignmentService assignmentService;

    @Override
    public ResponseEntity<MedBookApiResponse> postCreateAssignment(
            MedBookContext context, String clinicId, CreateAssignmentRequest createAssignmentRequest) {

        CreateAssignmentOutput output = assignmentService.createAssignment(context, clinicId, createAssignmentRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MedBookApiResponse()
                        .httpStatus(HttpStatus.CREATED.value())
                        .success(true)
                        .timestamp(LocalDateTime.now())
                        .data(output));
    }

    @Override
    public ResponseEntity<MedBookApiResponse> getAllAssignments(
            MedBookContext context, String clinicId, String doctorId, AssignmentStatusApiEnum status) {

        AssignmentListOutput output = assignmentService.getAllAssignments(context, clinicId, doctorId, status);

        return ResponseEntity.ok(new MedBookApiResponse()
                .httpStatus(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(output.getAssignments()));
    }

    @Override
    public ResponseEntity<MedBookApiVoidResponse> patchUpdateAssignment(
            MedBookContext context, String clinicId, String assignmentId,
            UpdateAssignmentRequest updateAssignmentRequest) {

        assignmentService.updateAssignment(context, clinicId, assignmentId, updateAssignmentRequest);

        return ResponseEntity.ok(new MedBookApiVoidResponse()
                .httpStatus(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now()));
    }

    @Override
    public ResponseEntity<MedBookApiVoidResponse> deleteAssignment(
            MedBookContext context, String clinicId, String assignmentId) {

        assignmentService.deleteAssignment(context, clinicId, assignmentId);

        return ResponseEntity.ok(new MedBookApiVoidResponse()
                .httpStatus(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now()));
    }

    @Override
    public ResponseEntity<MedBookApiVoidResponse> patchRestoreAssignment(
            MedBookContext context, String clinicId, String assignmentId) {

        assignmentService.restoreAssignment(context, clinicId, assignmentId);

        return ResponseEntity.ok(new MedBookApiVoidResponse()
                .httpStatus(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now()));
    }
}

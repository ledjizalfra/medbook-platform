package it.pegaso.projectwork.medbook.clinic.service.assignment;

import it.pegaso.projectwork.medbook.clinic.server.model.*;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;

public interface AssignmentService {

    CreateAssignmentOutput createAssignment(MedBookContext context, String clinicId, CreateAssignmentRequest request);

    AssignmentListOutput getAllAssignments(MedBookContext context, String clinicId,
                                           String doctorId, AssignmentStatusApiEnum status);

    void updateAssignment(MedBookContext context, String clinicId, String assignmentId,
                          UpdateAssignmentRequest request);

    void deleteAssignment(MedBookContext context, String clinicId, String assignmentId);

    void restoreAssignment(MedBookContext context, String clinicId, String assignmentId);
}

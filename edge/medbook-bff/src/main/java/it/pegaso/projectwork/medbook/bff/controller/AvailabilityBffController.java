package it.pegaso.projectwork.medbook.bff.controller;

import it.pegaso.projectwork.medbook.bff.server.model.SlotViewResponse;
import it.pegaso.projectwork.medbook.bff.service.availability.AvailabilitySearchService;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookApiResponse;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.commons.api.model.MedBookPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Controller BFF per la ricerca degli slot disponibili.
 * Tutta la logica di generazione slot e in AvailabilitySearchService. */
@RestController
@RequiredArgsConstructor
public class AvailabilityBffController {

    private final AvailabilitySearchService availabilitySearchService;

    /** Restituisce specializzazioni e medici con disponibilita attiva — per popolare i filtri del FE. */
    @GetMapping(value = "/bff/v1/availability/filters", produces = "application/json")
    public ResponseEntity<MedBookApiResponse> getAvailabilityFilters(
            @RequestHeader(value = "X-MedBook-Context", required = false) MedBookContext context) {
        MedBookApiResponse response = new MedBookApiResponse();
        response.setData(availabilitySearchService.getAvailabilityFilters(context));
        response.setSuccess(true);
        response.setHttpStatus(200);
        return ResponseEntity.ok(response);
    }

    /** Ricerca slot disponibili nel range di date indicato con filtri opzionali.
     * Supporta paginazione in-memory tramite i parametri page e size.
     * ROLE_PATIENT, ROLE_DOCTOR, ROLE_RECEPTIONIST, ROLE_ADMIN. */
    // @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/bff/v1/availability", produces = "application/json")
    public ResponseEntity<MedBookApiResponse> getAvailability(
            @RequestHeader(value = "X-MedBook-Context", required = false) MedBookContext context,
            @RequestParam(value = "clinicId", required = false) String clinicId,
            @RequestParam(value = "doctorId", required = false) String doctorId,
            @RequestParam(value = "specialization", required = false) String specialization,
            @RequestParam(value = "dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "dateTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {

        List<SlotViewResponse> allSlots = availabilitySearchService
                .searchAvailableSlots(context, clinicId, doctorId, specialization, dateFrom, dateTo);

        // Paginazione in-memory
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;
        int start = Math.min(pageNum * pageSize, allSlots.size());
        int end = Math.min(start + pageSize, allSlots.size());
        List<SlotViewResponse> pageContent = allSlots.subList(start, end);

        int totalElements = allSlots.size();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        MedBookPageResponse pageResponse = new MedBookPageResponse();
        pageResponse.setTotalElements((long) totalElements);
        pageResponse.setTotalPages(totalPages);
        pageResponse.setSize(pageSize);
        pageResponse.setNumber(pageNum);
        pageResponse.setFirst(pageNum == 0);
        pageResponse.setLast(pageNum >= totalPages - 1);
        pageResponse.setEmpty(pageContent.isEmpty());

        MedBookApiResponse response = new MedBookApiResponse();
        response.setData(pageContent);
        response.setPage(pageResponse);
        response.setSuccess(true);
        response.setHttpStatus(200);
        return ResponseEntity.ok(response);
    }
}

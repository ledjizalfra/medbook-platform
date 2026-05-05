package it.pegaso.projectwork.medbook.bff.helper;

import it.pegaso.projectwork.medbook.bff.server.model.CreatePatientBffRequest;
import it.pegaso.projectwork.medbook.bff.server.model.UpdatePatientBffRequest;
import it.pegaso.projectwork.medbook.commons.formatter.MedBookFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientBffHelperTest {

    @Mock
    private MedBookFormatter formatter;

    @InjectMocks
    private PatientBffHelper helper;

    @Test
    void formatRequest_create_normalizesAllStringFields() {
        // Restituisce sempre il valore in maiuscolo per provare che setFirst* venga effettivamente chiamato
        when(formatter.formatFirstName(anyString())).thenAnswer(inv -> ((String) inv.getArgument(0)).toUpperCase());
        when(formatter.formatLastName(anyString())).thenAnswer(inv -> ((String) inv.getArgument(0)).toUpperCase());
        when(formatter.formatEmail(anyString())).thenAnswer(inv -> ((String) inv.getArgument(0)).toLowerCase());
        when(formatter.formatPhone(anyString())).thenAnswer(inv -> ((String) inv.getArgument(0)).trim());
        when(formatter.trim(anyString())).thenAnswer(inv -> ((String) inv.getArgument(0)).trim());
        when(formatter.formatCity(anyString())).thenAnswer(inv -> ((String) inv.getArgument(0)).toUpperCase());
        when(formatter.formatPostalCode(anyString())).thenAnswer(inv -> ((String) inv.getArgument(0)));
        when(formatter.formatProvince(anyString())).thenAnswer(inv -> ((String) inv.getArgument(0)).toUpperCase());
        when(formatter.formatFiscalCode(anyString())).thenAnswer(inv -> ((String) inv.getArgument(0)).toUpperCase());

        CreatePatientBffRequest req = new CreatePatientBffRequest();
        req.setFirstName("mario");
        req.setLastName("rossi");
        req.setEmail("MARIO@MEDBOOK.IT");
        req.setPhone(" +393331234567 ");
        req.setAddress(" via roma 1 ");
        req.setCity("torino");
        req.setPostalCode("10100");
        req.setProvince("to");
        req.setFiscalCode("rssmra80a01l219x");
        req.setComuneNascita("napoli");
        req.setProvinciaNascita("na");
        req.setRegioneNascita("campania");

        helper.formatRequest(req);

        assertThat(req.getFirstName()).isEqualTo("MARIO");
        assertThat(req.getLastName()).isEqualTo("ROSSI");
        assertThat(req.getEmail()).isEqualTo("mario@medbook.it");
        assertThat(req.getPhone()).isEqualTo("+393331234567");
        assertThat(req.getAddress()).isEqualTo("via roma 1");
        assertThat(req.getCity()).isEqualTo("TORINO");
        assertThat(req.getProvince()).isEqualTo("TO");
        assertThat(req.getFiscalCode()).isEqualTo("RSSMRA80A01L219X");
        assertThat(req.getComuneNascita()).isEqualTo("NAPOLI");
        assertThat(req.getRegioneNascita()).isEqualTo("CAMPANIA");
    }

    @Test
    void formatRequest_update_doesNotTouchFiscalCode() {
        when(formatter.formatFirstName(anyString())).thenAnswer(inv -> inv.getArgument(0));
        when(formatter.formatLastName(anyString())).thenAnswer(inv -> inv.getArgument(0));
        when(formatter.formatEmail(anyString())).thenAnswer(inv -> inv.getArgument(0));
        when(formatter.formatPhone(anyString())).thenAnswer(inv -> inv.getArgument(0));
        when(formatter.trim(anyString())).thenAnswer(inv -> inv.getArgument(0));
        when(formatter.formatCity(anyString())).thenAnswer(inv -> inv.getArgument(0));
        when(formatter.formatPostalCode(anyString())).thenAnswer(inv -> inv.getArgument(0));
        when(formatter.formatProvince(anyString())).thenAnswer(inv -> inv.getArgument(0));

        UpdatePatientBffRequest req = new UpdatePatientBffRequest();
        req.setFirstName("Mario");
        req.setLastName("Rossi");
        req.setEmail("mario@medbook.it");
        req.setPhone("+393331234567");
        req.setAddress("via roma 1");
        req.setCity("Torino");
        req.setPostalCode("10100");
        req.setProvince("TO");

        helper.formatRequest(req);

        verify(formatter, atLeastOnce()).formatFirstName(anyString());
        // formatFiscalCode non e mai invocato per UpdatePatientBffRequest
        org.mockito.Mockito.verify(formatter, org.mockito.Mockito.never()).formatFiscalCode(anyString());
    }

    @Test
    void formatRequest_create_nullFields_passedThroughWithoutException() {
        lenient().when(formatter.formatFirstName(null)).thenReturn(null);
        lenient().when(formatter.formatLastName(null)).thenReturn(null);
        lenient().when(formatter.formatEmail(null)).thenReturn(null);
        lenient().when(formatter.formatPhone(null)).thenReturn(null);
        lenient().when(formatter.trim(null)).thenReturn(null);
        lenient().when(formatter.formatCity(null)).thenReturn(null);
        lenient().when(formatter.formatPostalCode(null)).thenReturn(null);
        lenient().when(formatter.formatProvince(null)).thenReturn(null);
        lenient().when(formatter.formatFiscalCode(null)).thenReturn(null);

        CreatePatientBffRequest req = new CreatePatientBffRequest();

        helper.formatRequest(req);

        assertThat(req.getFirstName()).isNull();
        assertThat(req.getLastName()).isNull();
    }
}

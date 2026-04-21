package it.pegaso.projectwork.medbook.commons.formatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MedBookFormatterTest {

    private MedBookFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new MedBookFormatter();
    }

    // =========================================================================
    // formatFirstName
    // =========================================================================

    @Test
    void formatFirstName_null_returnsNull() {
        assertThat(formatter.formatFirstName(null)).isNull();
    }

    @Test
    void formatFirstName_emptyString_returnsEmpty() {
        assertThat(formatter.formatFirstName("")).isEmpty();
    }

    @Test
    void formatFirstName_onlySpaces_returnsEmpty() {
        assertThat(formatter.formatFirstName("   ")).isEmpty();
    }

    @Test
    void formatFirstName_lowercase_capitalizes() {
        assertThat(formatter.formatFirstName("mario")).isEqualTo("Mario");
    }

    @Test
    void formatFirstName_uppercase_lowersExceptFirst() {
        assertThat(formatter.formatFirstName("MARIO")).isEqualTo("Mario");
    }

    @Test
    void formatFirstName_compoundName_capitalizesEachWord() {
        assertThat(formatter.formatFirstName("marie laure")).isEqualTo("Marie Laure");
    }

    @Test
    void formatFirstName_leadingTrailingSpaces_trimmed() {
        assertThat(formatter.formatFirstName("  anna  ")).isEqualTo("Anna");
    }

    @Test
    void formatFirstName_mixedCase_normalized() {
        assertThat(formatter.formatFirstName("mARIA")).isEqualTo("Maria");
    }

    // =========================================================================
    // formatLastName
    // =========================================================================

    @Test
    void formatLastName_null_returnsNull() {
        assertThat(formatter.formatLastName(null)).isNull();
    }

    @Test
    void formatLastName_lowercase_uppercased() {
        assertThat(formatter.formatLastName("smith")).isEqualTo("SMITH");
    }

    @Test
    void formatLastName_compoundName_allUppercase() {
        assertThat(formatter.formatLastName("kenfact dogmo")).isEqualTo("KENFACT DOGMO");
    }

    @Test
    void formatLastName_withSpaces_trimmedAndUppercased() {
        assertThat(formatter.formatLastName("  rossi  ")).isEqualTo("ROSSI");
    }

    // =========================================================================
    // formatEmail
    // =========================================================================

    @Test
    void formatEmail_null_returnsNull() {
        assertThat(formatter.formatEmail(null)).isNull();
    }

    @Test
    void formatEmail_mixedCase_lowercased() {
        assertThat(formatter.formatEmail("Mario.ROSSI@Gmail.com")).isEqualTo("mario.rossi@gmail.com");
    }

    @Test
    void formatEmail_withTrailingSpace_trimmed() {
        assertThat(formatter.formatEmail("user@example.com ")).isEqualTo("user@example.com");
    }

    @Test
    void formatEmail_alreadyLowercase_unchanged() {
        assertThat(formatter.formatEmail("user@example.com")).isEqualTo("user@example.com");
    }

    // =========================================================================
    // formatPhone
    // =========================================================================

    @Test
    void formatPhone_null_returnsNull() {
        assertThat(formatter.formatPhone(null)).isNull();
    }

    @Test
    void formatPhone_withInternationalPrefix_preservesPlusAndDigits() {
        assertThat(formatter.formatPhone("+39 333 123 4567")).isEqualTo("+393331234567");
    }

    @Test
    void formatPhone_withoutPrefix_onlyDigits() {
        assertThat(formatter.formatPhone("333 123 4567")).isEqualTo("3331234567");
    }

    @Test
    void formatPhone_withDashesAndSpaces_stripped() {
        assertThat(formatter.formatPhone("06-123-456")).isEqualTo("06123456");
    }

    @Test
    void formatPhone_alreadyClean_unchanged() {
        assertThat(formatter.formatPhone("+393331234567")).isEqualTo("+393331234567");
    }

    // =========================================================================
    // formatCity
    // =========================================================================

    @Test
    void formatCity_null_returnsNull() {
        assertThat(formatter.formatCity(null)).isNull();
    }

    @Test
    void formatCity_uppercase_capitalized() {
        assertThat(formatter.formatCity("ROMA")).isEqualTo("Roma");
    }

    @Test
    void formatCity_compoundCity_capitalizesEachWord() {
        assertThat(formatter.formatCity("new york")).isEqualTo("New York");
    }

    // =========================================================================
    // formatProvince
    // =========================================================================

    @Test
    void formatProvince_null_returnsNull() {
        assertThat(formatter.formatProvince(null)).isNull();
    }

    @Test
    void formatProvince_lowercase_uppercased() {
        assertThat(formatter.formatProvince("mi")).isEqualTo("MI");
    }

    @Test
    void formatProvince_withTrailingSpace_trimmedAndUppercased() {
        assertThat(formatter.formatProvince("mi ")).isEqualTo("MI");
    }

    // =========================================================================
    // formatPostalCode
    // =========================================================================

    @Test
    void formatPostalCode_null_returnsNull() {
        assertThat(formatter.formatPostalCode(null)).isNull();
    }

    @Test
    void formatPostalCode_withInternalSpace_onlyDigits() {
        assertThat(formatter.formatPostalCode("20 121")).isEqualTo("20121");
    }

    @Test
    void formatPostalCode_alreadyClean_unchanged() {
        assertThat(formatter.formatPostalCode("80100")).isEqualTo("80100");
    }

    // =========================================================================
    // formatFiscalCode
    // =========================================================================

    @Test
    void formatFiscalCode_null_returnsNull() {
        assertThat(formatter.formatFiscalCode(null)).isNull();
    }

    @Test
    void formatFiscalCode_lowercase_uppercased() {
        assertThat(formatter.formatFiscalCode("rssmra80a01h501u")).isEqualTo("RSSMRA80A01H501U");
    }

    @Test
    void formatFiscalCode_withSpaces_trimmedAndUppercased() {
        assertThat(formatter.formatFiscalCode("  RSSMRA80A01H501U  ")).isEqualTo("RSSMRA80A01H501U");
    }

    // =========================================================================
    // trim
    // =========================================================================

    @Test
    void trim_null_returnsNull() {
        assertThat(formatter.trim(null)).isNull();
    }

    @Test
    void trim_withLeadingTrailingSpaces_trimmed() {
        assertThat(formatter.trim("  testo  ")).isEqualTo("testo");
    }

    @Test
    void trim_noSpaces_unchanged() {
        assertThat(formatter.trim("testo")).isEqualTo("testo");
    }
}

/** Barrel export — API pubblica dei componenti shared MedBook */

// MedBookForm
export { MedBookFormComponent } from './components/medbook-form/medbook-form.component';
export { MedBookFormSlotDirective } from './components/medbook-form/medbook-form-slot.directive';
export type { CellType, SelectOption, CheckboxDef, FormCell, MbFormGroup } from './components/medbook-form/medbook-form.models';

// MedBookTable
export { MedBookTableComponent } from './components/medbook-table/medbook-table.component';
export type { ColumnType, TableColumn, TableAction, TablePagination, TableSearchConfig } from './components/medbook-table/medbook-table.models';

// MedBookPage
export { MedBookPageComponent } from './components/medbook-page/medbook-page.component';

// SearchableSelect
export { SearchableSelectComponent } from './components/searchable-select/searchable-select.component';

// FieldError
export { FieldErrorComponent } from './components/field-error/field-error.component';

// ConfirmDialog
export { ConfirmDialogComponent } from './components/confirm-dialog/confirm-dialog.component';

// InfoDialog
export { InfoDialogComponent } from './components/info-dialog/info-dialog.component';

// ConsentToggle
export { ConsentToggleComponent } from './components/consent-toggle/consent-toggle.component';
export type { ConsentItem } from './components/consent-toggle/consent-toggle.component';

// AppointmentCard
export { AppointmentCardComponent } from './components/appointment-card/appointment-card.component';
export type { AppointmentCardData } from './components/appointment-card/appointment-card.component';

// CalendarScheduler
export { CalendarSchedulerComponent } from './components/calendar-scheduler/calendar-scheduler.component';
export type { SchedulerSlot, SchedulerView } from './components/calendar-scheduler/calendar-scheduler.component';

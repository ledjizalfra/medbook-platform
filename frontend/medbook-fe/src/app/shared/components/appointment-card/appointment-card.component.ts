import { Component, EventEmitter, Input, Output } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';

/**
 * Dati di un appuntamento da visualizzare nella card.
 */
export interface AppointmentCardData {
  appointmentId: string;
  slotDate: string;
  startTime: string;
  endTime: string;
  status: string;
  doctorFullName?: string;
  patientFullName?: string;
  specialization?: string;
  clinicName?: string;
}

/**
 * Card riepilogo appuntamento riutilizzabile in tutte le aree (patient, doctor, receptionist, admin).
 * Mostra data, orario, medico/paziente, specializzazione, clinica e stato con badge colorato.
 */
@Component({
  selector: 'app-appointment-card',
  imports: [DatePipe, MatCardModule, MatIconModule, MatButtonModule, MatChipsModule],
  templateUrl: './appointment-card.component.html',
  styleUrl: './appointment-card.component.scss'
})
export class AppointmentCardComponent {
  @Input({ required: true }) appointment!: AppointmentCardData;
  @Input() showDoctor = true;
  @Input() showPatient = false;
  @Output() viewDetail = new EventEmitter<string>();

  protected getStatusClass(): string {
    switch (this.appointment.status) {
      case 'PRENOTATO':    return 'status-prenotato';
      case 'IN_CORSO':     return 'status-in-corso';
      case 'COMPLETATO':   return 'status-completato';
      case 'CANCELLATO':   return 'status-cancellato';
      case 'NON_PRESENTATO': return 'status-non-presentato';
      default:             return '';
    }
  }

  protected getStatusLabel(): string {
    switch (this.appointment.status) {
      case 'PRENOTATO':    return 'Prenotato';
      case 'IN_CORSO':     return 'In corso';
      case 'COMPLETATO':   return 'Completato';
      case 'CANCELLATO':   return 'Cancellato';
      case 'NON_PRESENTATO': return 'Non presentato';
      default:             return this.appointment.status;
    }
  }
}

import { Component, EventEmitter, Input, Output, signal, computed } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

/**
 * Slot da visualizzare nello scheduler.
 */
export interface SchedulerSlot {
  id: string;
  date: string;
  startTime: string;
  endTime: string;
  label?: string;
  status: 'LIBERO' | 'PRENOTATO' | 'IN_CORSO' | 'COMPLETATO' | 'CANCELLATO' | 'NON_PRESENTATO';
  metadata?: Record<string, unknown>;
}

/** Vista attiva dello scheduler. */
export type SchedulerView = 'day' | 'week' | 'month';

/**
 * Componente calendario/scheduler riutilizzabile.
 * Mostra slot in vista giorno, settimana o mese con navigazione avanti/indietro.
 * Emette eventi al click su uno slot e al cambio di data/vista.
 */
@Component({
  selector: 'app-calendar-scheduler',
  imports: [MatCardModule, MatButtonModule, MatIconModule],
  templateUrl: './calendar-scheduler.component.html',
  styleUrl: './calendar-scheduler.component.scss'
})
export class CalendarSchedulerComponent {
  @Input() slots: SchedulerSlot[] = [];
  @Input() view: SchedulerView = 'week';
  @Output() slotClicked = new EventEmitter<SchedulerSlot>();
  @Output() dateChanged = new EventEmitter<{ dateFrom: string; dateTo: string }>();
  @Output() viewChanged = new EventEmitter<SchedulerView>();

  protected currentDate = signal(new Date());

  protected title = computed(() => {
    const d = this.currentDate();
    const opts: Intl.DateTimeFormatOptions =
      this.view === 'day'   ? { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' } :
      this.view === 'month' ? { month: 'long', year: 'numeric' } :
                              { day: 'numeric', month: 'long', year: 'numeric' };
    return d.toLocaleDateString('it-IT', opts);
  });

  protected visibleSlots = computed(() => {
    const d = this.currentDate();
    const { from, to } = this.getRange(d, this.view);
    return this.slots.filter(s => s.date >= from && s.date <= to);
  });

  protected navigate(direction: -1 | 1): void {
    const d = new Date(this.currentDate());
    switch (this.view) {
      case 'day':   d.setDate(d.getDate() + direction); break;
      case 'week':  d.setDate(d.getDate() + 7 * direction); break;
      case 'month': d.setMonth(d.getMonth() + direction); break;
    }
    this.currentDate.set(d);
    const { from, to } = this.getRange(d, this.view);
    this.dateChanged.emit({ dateFrom: from, dateTo: to });
  }

  protected setView(v: SchedulerView): void {
    this.view = v;
    this.viewChanged.emit(v);
    const { from, to } = this.getRange(this.currentDate(), v);
    this.dateChanged.emit({ dateFrom: from, dateTo: to });
  }

  protected getSlotClass(slot: SchedulerSlot): string {
    return 'slot slot-' + slot.status.toLowerCase().replace('_', '-');
  }

  private getRange(d: Date, view: SchedulerView): { from: string; to: string } {
    const fmt = (dt: Date) => dt.toISOString().slice(0, 10);
    if (view === 'day') {
      const s = fmt(d);
      return { from: s, to: s };
    }
    if (view === 'week') {
      const start = new Date(d);
      start.setDate(start.getDate() - start.getDay() + 1);
      const end = new Date(start);
      end.setDate(end.getDate() + 6);
      return { from: fmt(start), to: fmt(end) };
    }
    const start = new Date(d.getFullYear(), d.getMonth(), 1);
    const end = new Date(d.getFullYear(), d.getMonth() + 1, 0);
    return { from: fmt(start), to: fmt(end) };
  }
}

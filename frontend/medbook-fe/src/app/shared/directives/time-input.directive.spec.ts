import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule, FormControl } from '@angular/forms';
import { TimeInputDirective } from './time-input.directive';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, TimeInputDirective],
  template: `<input appTimeInput [formControl]="ctrl" />`
})
class TestHostComponent {
  ctrl = new FormControl('');
}

describe('TimeInputDirective', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let input: HTMLInputElement;
  let host: TestHostComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(TestHostComponent);
    host = fixture.componentInstance;
    fixture.detectChanges();
    input = fixture.nativeElement.querySelector('input');
  });

  // --- Helper ---
  function typeValue(value: string): void {
    input.value = value;
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
  }

  function blur(): void {
    input.dispatchEvent(new Event('blur'));
    fixture.detectChanges();
  }

  // =========================================================================
  // Input formatting
  // =========================================================================
  describe('onInput', () => {
    it('deve rimuovere caratteri non numerici', () => {
      typeValue('1a2b');
      expect(input.value).toBe('12');
    });

    it('deve inserire : dopo 2 cifre', () => {
      typeValue('1530');
      expect(input.value).toBe('15:30');
    });

    it('deve limitare le ore a 23', () => {
      typeValue('2530');
      expect(input.value).toBe('23:30');
    });

    it('deve limitare i minuti a 59', () => {
      typeValue('1575');
      expect(input.value).toBe('15:59');
    });

    it('deve limitare a 5 caratteri (HH:MM)', () => {
      typeValue('153099');
      expect(input.value).toBe('15:30');
    });

    it('deve aggiornare il form control', () => {
      typeValue('0900');
      expect(host.ctrl.value).toBe('09:00');
    });
  });

  // =========================================================================
  // Blur completion
  // =========================================================================
  describe('onBlur', () => {
    it('"9" → "09:00"', () => {
      typeValue('9');
      blur();
      expect(input.value).toBe('09:00');
    });

    it('"15" → "15:00"', () => {
      typeValue('15');
      blur();
      expect(input.value).toBe('15:00');
    });

    it('"153" → "15:30"', () => {
      typeValue('153');
      blur();
      expect(input.value).toBe('15:30');
    });

    it('"0830" → "08:30"', () => {
      typeValue('0830');
      blur();
      expect(input.value).toBe('08:30');
    });

    it('campo vuoto non cambia', () => {
      typeValue('');
      blur();
      expect(input.value).toBe('');
    });

    it('ore > 23 corrette a 23', () => {
      typeValue('25');
      blur();
      expect(input.value).toBe('23:00');
    });

    it('minuti > 59 corretti a 59', () => {
      typeValue('1299');
      blur();
      expect(input.value).toBe('12:59');
    });

    it('aggiorna il form control', () => {
      typeValue('8');
      blur();
      expect(host.ctrl.value).toBe('08:00');
    });
  });

  // =========================================================================
  // Keydown blocking
  // =========================================================================
  describe('onKeyDown', () => {
    function keydown(key: string, ctrlKey = false): boolean {
      const event = new KeyboardEvent('keydown', { key, ctrlKey, cancelable: true });
      input.dispatchEvent(event);
      return event.defaultPrevented;
    }

    it('deve permettere cifre', () => {
      expect(keydown('5')).toBe(false);
    });

    it('deve bloccare lettere', () => {
      expect(keydown('a')).toBe(true);
    });

    it('deve permettere Backspace', () => {
      expect(keydown('Backspace')).toBe(false);
    });

    it('deve permettere Tab', () => {
      expect(keydown('Tab')).toBe(false);
    });

    it('deve permettere frecce', () => {
      expect(keydown('ArrowLeft')).toBe(false);
      expect(keydown('ArrowRight')).toBe(false);
    });

    it('deve permettere Ctrl+A', () => {
      expect(keydown('a', true)).toBe(false);
    });

    it('deve permettere Ctrl+C', () => {
      expect(keydown('c', true)).toBe(false);
    });

    it('deve bloccare caratteri speciali', () => {
      expect(keydown(':')).toBe(true);
      expect(keydown('-')).toBe(true);
      expect(keydown('.')).toBe(true);
    });
  });
});

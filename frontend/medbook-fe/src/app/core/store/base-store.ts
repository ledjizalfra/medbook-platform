import { signal, Signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

/**
 * Store base con cache in-memory e TTL per una singola chiave.
 *
 * Ogni store concreto estende questa classe fornendo il TTL e la funzione
 * di caricamento HTTP. Il dato viene mantenuto in un Signal privato.
 *
 * Regole:
 * - Cache hit: dato presente E TTL non scaduto → nessuna chiamata HTTP
 * - Cache miss: dato null OPPURE TTL scaduto → esegue la chiamata HTTP
 * - Invalidazione: reimposta segnale e timestamp → reload alla prossima load()
 */
export abstract class BaseStore<T> {

  private readonly _data = signal<T | null>(null);
  private _loadedAt = 0;
  private _loading = false;

  /** Segnale in sola lettura per i componenti */
  readonly data: Signal<T | null> = this._data.asReadonly();

  constructor(private readonly ttlMs: number) {}

  /**
   * Carica il dato se non presente o se il TTL e scaduto.
   * Restituisce un Observable che emette il dato (dalla cache o dal backend).
   */
  load(fetcher: () => Observable<T>): Observable<T> {
    const now = Date.now();
    const cached = this._data();

    // Cache hit
    if (cached !== null && (now - this._loadedAt) < this.ttlMs) {
      return new Observable<T>(subscriber => {
        subscriber.next(cached);
        subscriber.complete();
      });
    }

    // Evita chiamate parallele
    if (this._loading) {
      return new Observable<T>(subscriber => {
        const check = setInterval(() => {
          const val = this._data();
          if (val !== null) {
            clearInterval(check);
            subscriber.next(val);
            subscriber.complete();
          }
        }, 50);
      });
    }

    // Cache miss — carica dal backend
    this._loading = true;
    return fetcher().pipe(
      tap(data => {
        this._data.set(data);
        this._loadedAt = Date.now();
        this._loading = false;
      })
    );
  }

  /** Forza il reload alla prossima chiamata load() */
  invalidate(): void {
    this._data.set(null);
    this._loadedAt = 0;
    this._loading = false;
  }

  /** Restituisce true se il dato e presente e il TTL non e scaduto */
  get isCached(): boolean {
    return this._data() !== null && (Date.now() - this._loadedAt) < this.ttlMs;
  }
}

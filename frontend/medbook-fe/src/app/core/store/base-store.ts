import { signal, Signal } from '@angular/core';
import { Observable, of, tap, catchError, finalize } from 'rxjs';

/**
 * Store base con cache in-memory e TTL.
 *
 * Regole:
 * - Cache hit: dato presente E TTL non scaduto → restituisce Observable sincrono
 * - Cache miss: esegue la chiamata HTTP, aggiorna il signal e il timestamp
 * - Errore HTTP: resetta lo stato loading, propaga l'errore
 * - Invalidazione: forza il reload alla prossima load()
 */
export abstract class BaseStore<T> {

  private readonly _data = signal<T | null>(null);
  private _loadedAt = 0;
  private _loading = false;
  private _inflight$: Observable<T> | null = null;

  /** Segnale in sola lettura per i componenti */
  readonly data: Signal<T | null> = this._data.asReadonly();

  constructor(private readonly ttlMs: number) {}

  /**
   * Carica il dato se non presente o se il TTL e scaduto.
   * Se una chiamata e gia in corso, condivide lo stesso Observable.
   */
  load(fetcher: () => Observable<T>): Observable<T> {
    const now = Date.now();
    const cached = this._data();

    // Cache hit — dato presente e TTL valido
    if (cached !== null && (now - this._loadedAt) < this.ttlMs) {
      return of(cached);
    }

    // Chiamata gia in corso — condividi lo stesso Observable
    if (this._loading && this._inflight$) {
      return this._inflight$;
    }

    // Cache miss — esegui la chiamata HTTP
    this._loading = true;
    this._inflight$ = fetcher().pipe(
      tap(data => {
        this._data.set(data);
        this._loadedAt = Date.now();
      }),
      catchError(err => {
        // In caso di errore, restituisci array vuoto per non bloccare i componenti
        console.warn('[BaseStore] Errore caricamento:', err);
        return of([] as unknown as T);
      }),
      finalize(() => {
        this._loading = false;
        this._inflight$ = null;
      })
    );
    return this._inflight$;
  }

  /** Forza il reload alla prossima chiamata load() */
  invalidate(): void {
    this._data.set(null);
    this._loadedAt = 0;
    this._loading = false;
    this._inflight$ = null;
  }
}

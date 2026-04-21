package it.pegaso.projectwork.medbook.commons.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Classe base astratta per repository custom che richiedono accesso diretto
 * all'{@link EntityManager}.
 * <p>
 * Fornisce metodi utility riutilizzabili da tutti i DMN per:
 * <ul>
 *   <li>Criteria API - costruzione di query type-safe programmaticamente</li>
 *   <li>Native query - SQL puro per casi di performance critica o query complesse</li>
 *   <li>Paginazione - wrapping dei risultati in {@link Page}</li>
 * </ul>
 * <p>
 * I repository custom di ogni DMN estendono questa classe e ne implementano
 * la relativa interfaccia {@code {Entity}CustomRepository}.
 */
public abstract class MedBookBaseRepository {

    @PersistenceContext
    protected EntityManager entityManager;

    /**
     * Restituisce il {@link CriteriaBuilder} dell'EntityManager corrente.
     * Punto di ingresso per la costruzione di Criteria API.
     *
     * @return istanza del CriteriaBuilder
     */
    protected CriteriaBuilder getCriteriaBuilder() {
        return entityManager.getCriteriaBuilder();
    }

    /**
     * Esegue una {@link TypedQuery} con paginazione e restituisce un {@link Page}.
     * Il conteggio totale dei risultati viene eseguito tramite una query separata.
     *
     * @param query      query tipizzata da eseguire
     * @param countQuery query di conteggio per il totale dei risultati
     * @param pageable   parametri di paginazione
     * @param <T>        tipo del risultato
     * @return pagina dei risultati
     */
    protected <T> Page<T> getPage(TypedQuery<T> query, TypedQuery<Long> countQuery, Pageable pageable) {
        // Applica offset e limit per la paginazione
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<T> content = query.getResultList();
        long total = countQuery.getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Crea una query Criteria tipizzata per il tipo di entita specificato.
     *
     * @param entityClass classe dell'entita
     * @param <T>         tipo dell'entita
     * @return {@link CriteriaQuery} tipizzata
     */
    protected <T> CriteriaQuery<T> createQuery(Class<T> entityClass) {
        return getCriteriaBuilder().createQuery(entityClass);
    }

    /**
     * Crea una query Criteria per il conteggio dei risultati.
     *
     * @return {@link CriteriaQuery} di tipo {@link Long}
     */
    protected CriteriaQuery<Long> createCountQuery() {
        return getCriteriaBuilder().createQuery(Long.class);
    }

    /**
     * Crea una native query SQL non tipizzata.
     *
     * @param sql stringa SQL nativa
     * @return {@link Query} JPA
     */
    protected Query createNativeQuery(String sql) {
        return entityManager.createNativeQuery(sql);
    }

    /**
     * Crea una native query SQL tipizzata sul tipo di entita specificato.
     *
     * @param sql         stringa SQL nativa
     * @param entityClass classe dell'entita da mappare nel risultato
     * @param <T>         tipo dell'entita
     * @return {@link Query} JPA
     */
    protected <T> Query createNativeQuery(String sql, Class<T> entityClass) {
        return entityManager.createNativeQuery(sql, entityClass);
    }
}

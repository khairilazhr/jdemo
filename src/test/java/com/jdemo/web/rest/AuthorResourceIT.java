package com.jdemo.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jdemo.IntegrationTest;
import com.jdemo.domain.Author;
import com.jdemo.repository.AuthorRepository;
import com.jdemo.service.criteria.AuthorCriteria;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link AuthorResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class AuthorResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_AGE = "AAAAAAAAAA";
    private static final String UPDATED_AGE = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_BIRTH_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_BIRTH_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_BIRTH_DATE = LocalDate.ofEpochDay(-1L);

    private static final String ENTITY_API_URL = "/api/authors";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAuthorMockMvc;

    private Author author;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Author createEntity(EntityManager em) {
        Author author = new Author().name(DEFAULT_NAME).age(DEFAULT_AGE).birthDate(DEFAULT_BIRTH_DATE);
        return author;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Author createUpdatedEntity(EntityManager em) {
        Author author = new Author().name(UPDATED_NAME).age(UPDATED_AGE).birthDate(UPDATED_BIRTH_DATE);
        return author;
    }

    @BeforeEach
    public void initTest() {
        author = createEntity(em);
    }

    @Test
    @Transactional
    void createAuthor() throws Exception {
        int databaseSizeBeforeCreate = authorRepository.findAll().size();
        // Create the Author
        restAuthorMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(author)))
            .andExpect(status().isCreated());

        // Validate the Author in the database
        List<Author> authorList = authorRepository.findAll();
        assertThat(authorList).hasSize(databaseSizeBeforeCreate + 1);
        Author testAuthor = authorList.get(authorList.size() - 1);
        assertThat(testAuthor.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAuthor.getAge()).isEqualTo(DEFAULT_AGE);
        assertThat(testAuthor.getBirthDate()).isEqualTo(DEFAULT_BIRTH_DATE);
    }

    @Test
    @Transactional
    void createAuthorWithExistingId() throws Exception {
        // Create the Author with an existing ID
        author.setId(1L);

        int databaseSizeBeforeCreate = authorRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAuthorMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(author)))
            .andExpect(status().isBadRequest());

        // Validate the Author in the database
        List<Author> authorList = authorRepository.findAll();
        assertThat(authorList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = authorRepository.findAll().size();
        // set the field null
        author.setName(null);

        // Create the Author, which fails.

        restAuthorMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(author)))
            .andExpect(status().isBadRequest());

        List<Author> authorList = authorRepository.findAll();
        assertThat(authorList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAuthors() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get all the authorList
        restAuthorMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(author.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].age").value(hasItem(DEFAULT_AGE)))
            .andExpect(jsonPath("$.[*].birthDate").value(hasItem(DEFAULT_BIRTH_DATE.toString())));
    }

    @Test
    @Transactional
    void getAuthor() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get the author
        restAuthorMockMvc
            .perform(get(ENTITY_API_URL_ID, author.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(author.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.age").value(DEFAULT_AGE))
            .andExpect(jsonPath("$.birthDate").value(DEFAULT_BIRTH_DATE.toString()));
    }

    @Test
    @Transactional
    void getAuthorsByIdFiltering() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        Long id = author.getId();

        defaultAuthorShouldBeFound("id.equals=" + id);
        defaultAuthorShouldNotBeFound("id.notEquals=" + id);

        defaultAuthorShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultAuthorShouldNotBeFound("id.greaterThan=" + id);

        defaultAuthorShouldBeFound("id.lessThanOrEqual=" + id);
        defaultAuthorShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllAuthorsByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get all the authorList where name equals to DEFAULT_NAME
        defaultAuthorShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the authorList where name equals to UPDATED_NAME
        defaultAuthorShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllAuthorsByNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get all the authorList where name not equals to DEFAULT_NAME
        defaultAuthorShouldNotBeFound("name.notEquals=" + DEFAULT_NAME);

        // Get all the authorList where name not equals to UPDATED_NAME
        defaultAuthorShouldBeFound("name.notEquals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllAuthorsByNameIsInShouldWork() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get all the authorList where name in DEFAULT_NAME or UPDATED_NAME
        defaultAuthorShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the authorList where name equals to UPDATED_NAME
        defaultAuthorShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllAuthorsByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get all the authorList where name is not null
        defaultAuthorShouldBeFound("name.specified=true");

        // Get all the authorList where name is null
        defaultAuthorShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    void getAllAuthorsByNameContainsSomething() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get all the authorList where name contains DEFAULT_NAME
        defaultAuthorShouldBeFound("name.contains=" + DEFAULT_NAME);

        // Get all the authorList where name contains UPDATED_NAME
        defaultAuthorShouldNotBeFound("name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllAuthorsByNameNotContainsSomething() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get all the authorList where name does not contain DEFAULT_NAME
        defaultAuthorShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);

        // Get all the authorList where name does not contain UPDATED_NAME
        defaultAuthorShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllAuthorsByAgeIsEqualToSomething() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get all the authorList where age equals to DEFAULT_AGE
        defaultAuthorShouldBeFound("age.equals=" + DEFAULT_AGE);

        // Get all the authorList where age equals to UPDATED_AGE
        defaultAuthorShouldNotBeFound("age.equals=" + UPDATED_AGE);
    }

    @Test
    @Transactional
    void getAllAuthorsByAgeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get all the authorList where age not equals to DEFAULT_AGE
        defaultAuthorShouldNotBeFound("age.notEquals=" + DEFAULT_AGE);

        // Get all the authorList where age not equals to UPDATED_AGE
        defaultAuthorShouldBeFound("age.notEquals=" + UPDATED_AGE);
    }

    @Test
    @Transactional
    void getAllAuthorsByAgeIsInShouldWork() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get all the authorList where age in DEFAULT_AGE or UPDATED_AGE
        defaultAuthorShouldBeFound("age.in=" + DEFAULT_AGE + "," + UPDATED_AGE);

        // Get all the authorList where age equals to UPDATED_AGE
        defaultAuthorShouldNotBeFound("age.in=" + UPDATED_AGE);
    }

    @Test
    @Transactional
    void getAllAuthorsByAgeIsNullOrNotNull() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get all the authorList where age is not null
        defaultAuthorShouldBeFound("age.specified=true");

        // Get all the authorList where age is null
        defaultAuthorShouldNotBeFound("age.specified=false");
    }

    @Test
    @Transactional
    void getAllAuthorsByAgeContainsSomething() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get all the authorList where age contains DEFAULT_AGE
        defaultAuthorShouldBeFound("age.contains=" + DEFAULT_AGE);

        // Get all the authorList where age contains UPDATED_AGE
        defaultAuthorShouldNotBeFound("age.contains=" + UPDATED_AGE);
    }

    @Test
    @Transactional
    void getAllAuthorsByAgeNotContainsSomething() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get all the authorList where age does not contain DEFAULT_AGE
        defaultAuthorShouldNotBeFound("age.doesNotContain=" + DEFAULT_AGE);

        // Get all the authorList where age does not contain UPDATED_AGE
        defaultAuthorShouldBeFound("age.doesNotContain=" + UPDATED_AGE);
    }

    @Test
    @Transactional
    void getAllAuthorsByBirthDateIsEqualToSomething() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get all the authorList where birthDate equals to DEFAULT_BIRTH_DATE
        defaultAuthorShouldBeFound("birthDate.equals=" + DEFAULT_BIRTH_DATE);

        // Get all the authorList where birthDate equals to UPDATED_BIRTH_DATE
        defaultAuthorShouldNotBeFound("birthDate.equals=" + UPDATED_BIRTH_DATE);
    }

    @Test
    @Transactional
    void getAllAuthorsByBirthDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get all the authorList where birthDate not equals to DEFAULT_BIRTH_DATE
        defaultAuthorShouldNotBeFound("birthDate.notEquals=" + DEFAULT_BIRTH_DATE);

        // Get all the authorList where birthDate not equals to UPDATED_BIRTH_DATE
        defaultAuthorShouldBeFound("birthDate.notEquals=" + UPDATED_BIRTH_DATE);
    }

    @Test
    @Transactional
    void getAllAuthorsByBirthDateIsInShouldWork() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get all the authorList where birthDate in DEFAULT_BIRTH_DATE or UPDATED_BIRTH_DATE
        defaultAuthorShouldBeFound("birthDate.in=" + DEFAULT_BIRTH_DATE + "," + UPDATED_BIRTH_DATE);

        // Get all the authorList where birthDate equals to UPDATED_BIRTH_DATE
        defaultAuthorShouldNotBeFound("birthDate.in=" + UPDATED_BIRTH_DATE);
    }

    @Test
    @Transactional
    void getAllAuthorsByBirthDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get all the authorList where birthDate is not null
        defaultAuthorShouldBeFound("birthDate.specified=true");

        // Get all the authorList where birthDate is null
        defaultAuthorShouldNotBeFound("birthDate.specified=false");
    }

    @Test
    @Transactional
    void getAllAuthorsByBirthDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get all the authorList where birthDate is greater than or equal to DEFAULT_BIRTH_DATE
        defaultAuthorShouldBeFound("birthDate.greaterThanOrEqual=" + DEFAULT_BIRTH_DATE);

        // Get all the authorList where birthDate is greater than or equal to UPDATED_BIRTH_DATE
        defaultAuthorShouldNotBeFound("birthDate.greaterThanOrEqual=" + UPDATED_BIRTH_DATE);
    }

    @Test
    @Transactional
    void getAllAuthorsByBirthDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get all the authorList where birthDate is less than or equal to DEFAULT_BIRTH_DATE
        defaultAuthorShouldBeFound("birthDate.lessThanOrEqual=" + DEFAULT_BIRTH_DATE);

        // Get all the authorList where birthDate is less than or equal to SMALLER_BIRTH_DATE
        defaultAuthorShouldNotBeFound("birthDate.lessThanOrEqual=" + SMALLER_BIRTH_DATE);
    }

    @Test
    @Transactional
    void getAllAuthorsByBirthDateIsLessThanSomething() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get all the authorList where birthDate is less than DEFAULT_BIRTH_DATE
        defaultAuthorShouldNotBeFound("birthDate.lessThan=" + DEFAULT_BIRTH_DATE);

        // Get all the authorList where birthDate is less than UPDATED_BIRTH_DATE
        defaultAuthorShouldBeFound("birthDate.lessThan=" + UPDATED_BIRTH_DATE);
    }

    @Test
    @Transactional
    void getAllAuthorsByBirthDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get all the authorList where birthDate is greater than DEFAULT_BIRTH_DATE
        defaultAuthorShouldNotBeFound("birthDate.greaterThan=" + DEFAULT_BIRTH_DATE);

        // Get all the authorList where birthDate is greater than SMALLER_BIRTH_DATE
        defaultAuthorShouldBeFound("birthDate.greaterThan=" + SMALLER_BIRTH_DATE);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultAuthorShouldBeFound(String filter) throws Exception {
        restAuthorMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(author.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].age").value(hasItem(DEFAULT_AGE)))
            .andExpect(jsonPath("$.[*].birthDate").value(hasItem(DEFAULT_BIRTH_DATE.toString())));

        // Check, that the count call also returns 1
        restAuthorMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultAuthorShouldNotBeFound(String filter) throws Exception {
        restAuthorMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restAuthorMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingAuthor() throws Exception {
        // Get the author
        restAuthorMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewAuthor() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        int databaseSizeBeforeUpdate = authorRepository.findAll().size();

        // Update the author
        Author updatedAuthor = authorRepository.findById(author.getId()).get();
        // Disconnect from session so that the updates on updatedAuthor are not directly saved in db
        em.detach(updatedAuthor);
        updatedAuthor.name(UPDATED_NAME).age(UPDATED_AGE).birthDate(UPDATED_BIRTH_DATE);

        restAuthorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedAuthor.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedAuthor))
            )
            .andExpect(status().isOk());

        // Validate the Author in the database
        List<Author> authorList = authorRepository.findAll();
        assertThat(authorList).hasSize(databaseSizeBeforeUpdate);
        Author testAuthor = authorList.get(authorList.size() - 1);
        assertThat(testAuthor.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAuthor.getAge()).isEqualTo(UPDATED_AGE);
        assertThat(testAuthor.getBirthDate()).isEqualTo(UPDATED_BIRTH_DATE);
    }

    @Test
    @Transactional
    void putNonExistingAuthor() throws Exception {
        int databaseSizeBeforeUpdate = authorRepository.findAll().size();
        author.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAuthorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, author.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(author))
            )
            .andExpect(status().isBadRequest());

        // Validate the Author in the database
        List<Author> authorList = authorRepository.findAll();
        assertThat(authorList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAuthor() throws Exception {
        int databaseSizeBeforeUpdate = authorRepository.findAll().size();
        author.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuthorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(author))
            )
            .andExpect(status().isBadRequest());

        // Validate the Author in the database
        List<Author> authorList = authorRepository.findAll();
        assertThat(authorList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAuthor() throws Exception {
        int databaseSizeBeforeUpdate = authorRepository.findAll().size();
        author.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuthorMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(author)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Author in the database
        List<Author> authorList = authorRepository.findAll();
        assertThat(authorList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAuthorWithPatch() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        int databaseSizeBeforeUpdate = authorRepository.findAll().size();

        // Update the author using partial update
        Author partialUpdatedAuthor = new Author();
        partialUpdatedAuthor.setId(author.getId());

        partialUpdatedAuthor.age(UPDATED_AGE).birthDate(UPDATED_BIRTH_DATE);

        restAuthorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAuthor.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAuthor))
            )
            .andExpect(status().isOk());

        // Validate the Author in the database
        List<Author> authorList = authorRepository.findAll();
        assertThat(authorList).hasSize(databaseSizeBeforeUpdate);
        Author testAuthor = authorList.get(authorList.size() - 1);
        assertThat(testAuthor.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAuthor.getAge()).isEqualTo(UPDATED_AGE);
        assertThat(testAuthor.getBirthDate()).isEqualTo(UPDATED_BIRTH_DATE);
    }

    @Test
    @Transactional
    void fullUpdateAuthorWithPatch() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        int databaseSizeBeforeUpdate = authorRepository.findAll().size();

        // Update the author using partial update
        Author partialUpdatedAuthor = new Author();
        partialUpdatedAuthor.setId(author.getId());

        partialUpdatedAuthor.name(UPDATED_NAME).age(UPDATED_AGE).birthDate(UPDATED_BIRTH_DATE);

        restAuthorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAuthor.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAuthor))
            )
            .andExpect(status().isOk());

        // Validate the Author in the database
        List<Author> authorList = authorRepository.findAll();
        assertThat(authorList).hasSize(databaseSizeBeforeUpdate);
        Author testAuthor = authorList.get(authorList.size() - 1);
        assertThat(testAuthor.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAuthor.getAge()).isEqualTo(UPDATED_AGE);
        assertThat(testAuthor.getBirthDate()).isEqualTo(UPDATED_BIRTH_DATE);
    }

    @Test
    @Transactional
    void patchNonExistingAuthor() throws Exception {
        int databaseSizeBeforeUpdate = authorRepository.findAll().size();
        author.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAuthorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, author.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(author))
            )
            .andExpect(status().isBadRequest());

        // Validate the Author in the database
        List<Author> authorList = authorRepository.findAll();
        assertThat(authorList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAuthor() throws Exception {
        int databaseSizeBeforeUpdate = authorRepository.findAll().size();
        author.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuthorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(author))
            )
            .andExpect(status().isBadRequest());

        // Validate the Author in the database
        List<Author> authorList = authorRepository.findAll();
        assertThat(authorList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAuthor() throws Exception {
        int databaseSizeBeforeUpdate = authorRepository.findAll().size();
        author.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuthorMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(author)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Author in the database
        List<Author> authorList = authorRepository.findAll();
        assertThat(authorList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAuthor() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        int databaseSizeBeforeDelete = authorRepository.findAll().size();

        // Delete the author
        restAuthorMockMvc
            .perform(delete(ENTITY_API_URL_ID, author.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Author> authorList = authorRepository.findAll();
        assertThat(authorList).hasSize(databaseSizeBeforeDelete - 1);
    }
}

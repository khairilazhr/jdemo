package com.jdemo.web.rest;

import static com.jdemo.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jdemo.IntegrationTest;
import com.jdemo.domain.Book;
import com.jdemo.repository.BookRepository;
import com.jdemo.service.criteria.BookCriteria;
import java.math.BigDecimal;
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
import org.springframework.util.Base64Utils;

/**
 * Integration tests for the {@link BookResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class BookResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_PUBLICATION_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_PUBLICATION_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_PUBLICATION_DATE = LocalDate.ofEpochDay(-1L);

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal(1);
    private static final BigDecimal UPDATED_PRICE = new BigDecimal(2);
    private static final BigDecimal SMALLER_PRICE = new BigDecimal(1 - 1);

    private static final byte[] DEFAULT_COVER = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_COVER = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_COVER_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_COVER_CONTENT_TYPE = "image/png";

    private static final String ENTITY_API_URL = "/api/books";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restBookMockMvc;

    private Book book;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Book createEntity(EntityManager em) {
        Book book = new Book()
            .title(DEFAULT_TITLE)
            .description(DEFAULT_DESCRIPTION)
            .publicationDate(DEFAULT_PUBLICATION_DATE)
            .price(DEFAULT_PRICE)
            .cover(DEFAULT_COVER)
            .coverContentType(DEFAULT_COVER_CONTENT_TYPE);
        return book;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Book createUpdatedEntity(EntityManager em) {
        Book book = new Book()
            .title(UPDATED_TITLE)
            .description(UPDATED_DESCRIPTION)
            .publicationDate(UPDATED_PUBLICATION_DATE)
            .price(UPDATED_PRICE)
            .cover(UPDATED_COVER)
            .coverContentType(UPDATED_COVER_CONTENT_TYPE);
        return book;
    }

    @BeforeEach
    public void initTest() {
        book = createEntity(em);
    }

    @Test
    @Transactional
    void createBook() throws Exception {
        int databaseSizeBeforeCreate = bookRepository.findAll().size();
        // Create the Book
        restBookMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(book)))
            .andExpect(status().isCreated());

        // Validate the Book in the database
        List<Book> bookList = bookRepository.findAll();
        assertThat(bookList).hasSize(databaseSizeBeforeCreate + 1);
        Book testBook = bookList.get(bookList.size() - 1);
        assertThat(testBook.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testBook.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testBook.getPublicationDate()).isEqualTo(DEFAULT_PUBLICATION_DATE);
        assertThat(testBook.getPrice()).isEqualByComparingTo(DEFAULT_PRICE);
        assertThat(testBook.getCover()).isEqualTo(DEFAULT_COVER);
        assertThat(testBook.getCoverContentType()).isEqualTo(DEFAULT_COVER_CONTENT_TYPE);
    }

    @Test
    @Transactional
    void createBookWithExistingId() throws Exception {
        // Create the Book with an existing ID
        book.setId(1L);

        int databaseSizeBeforeCreate = bookRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restBookMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(book)))
            .andExpect(status().isBadRequest());

        // Validate the Book in the database
        List<Book> bookList = bookRepository.findAll();
        assertThat(bookList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTitleIsRequired() throws Exception {
        int databaseSizeBeforeTest = bookRepository.findAll().size();
        // set the field null
        book.setTitle(null);

        // Create the Book, which fails.

        restBookMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(book)))
            .andExpect(status().isBadRequest());

        List<Book> bookList = bookRepository.findAll();
        assertThat(bookList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllBooks() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList
        restBookMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(book.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].publicationDate").value(hasItem(DEFAULT_PUBLICATION_DATE.toString())))
            .andExpect(jsonPath("$.[*].price").value(hasItem(sameNumber(DEFAULT_PRICE))))
            .andExpect(jsonPath("$.[*].coverContentType").value(hasItem(DEFAULT_COVER_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].cover").value(hasItem(Base64Utils.encodeToString(DEFAULT_COVER))));
    }

    @Test
    @Transactional
    void getBook() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get the book
        restBookMockMvc
            .perform(get(ENTITY_API_URL_ID, book.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(book.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.publicationDate").value(DEFAULT_PUBLICATION_DATE.toString()))
            .andExpect(jsonPath("$.price").value(sameNumber(DEFAULT_PRICE)))
            .andExpect(jsonPath("$.coverContentType").value(DEFAULT_COVER_CONTENT_TYPE))
            .andExpect(jsonPath("$.cover").value(Base64Utils.encodeToString(DEFAULT_COVER)));
    }

    @Test
    @Transactional
    void getBooksByIdFiltering() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        Long id = book.getId();

        defaultBookShouldBeFound("id.equals=" + id);
        defaultBookShouldNotBeFound("id.notEquals=" + id);

        defaultBookShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultBookShouldNotBeFound("id.greaterThan=" + id);

        defaultBookShouldBeFound("id.lessThanOrEqual=" + id);
        defaultBookShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllBooksByTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where title equals to DEFAULT_TITLE
        defaultBookShouldBeFound("title.equals=" + DEFAULT_TITLE);

        // Get all the bookList where title equals to UPDATED_TITLE
        defaultBookShouldNotBeFound("title.equals=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllBooksByTitleIsNotEqualToSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where title not equals to DEFAULT_TITLE
        defaultBookShouldNotBeFound("title.notEquals=" + DEFAULT_TITLE);

        // Get all the bookList where title not equals to UPDATED_TITLE
        defaultBookShouldBeFound("title.notEquals=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllBooksByTitleIsInShouldWork() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where title in DEFAULT_TITLE or UPDATED_TITLE
        defaultBookShouldBeFound("title.in=" + DEFAULT_TITLE + "," + UPDATED_TITLE);

        // Get all the bookList where title equals to UPDATED_TITLE
        defaultBookShouldNotBeFound("title.in=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllBooksByTitleIsNullOrNotNull() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where title is not null
        defaultBookShouldBeFound("title.specified=true");

        // Get all the bookList where title is null
        defaultBookShouldNotBeFound("title.specified=false");
    }

    @Test
    @Transactional
    void getAllBooksByTitleContainsSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where title contains DEFAULT_TITLE
        defaultBookShouldBeFound("title.contains=" + DEFAULT_TITLE);

        // Get all the bookList where title contains UPDATED_TITLE
        defaultBookShouldNotBeFound("title.contains=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllBooksByTitleNotContainsSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where title does not contain DEFAULT_TITLE
        defaultBookShouldNotBeFound("title.doesNotContain=" + DEFAULT_TITLE);

        // Get all the bookList where title does not contain UPDATED_TITLE
        defaultBookShouldBeFound("title.doesNotContain=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllBooksByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where description equals to DEFAULT_DESCRIPTION
        defaultBookShouldBeFound("description.equals=" + DEFAULT_DESCRIPTION);

        // Get all the bookList where description equals to UPDATED_DESCRIPTION
        defaultBookShouldNotBeFound("description.equals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllBooksByDescriptionIsNotEqualToSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where description not equals to DEFAULT_DESCRIPTION
        defaultBookShouldNotBeFound("description.notEquals=" + DEFAULT_DESCRIPTION);

        // Get all the bookList where description not equals to UPDATED_DESCRIPTION
        defaultBookShouldBeFound("description.notEquals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllBooksByDescriptionIsInShouldWork() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where description in DEFAULT_DESCRIPTION or UPDATED_DESCRIPTION
        defaultBookShouldBeFound("description.in=" + DEFAULT_DESCRIPTION + "," + UPDATED_DESCRIPTION);

        // Get all the bookList where description equals to UPDATED_DESCRIPTION
        defaultBookShouldNotBeFound("description.in=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllBooksByDescriptionIsNullOrNotNull() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where description is not null
        defaultBookShouldBeFound("description.specified=true");

        // Get all the bookList where description is null
        defaultBookShouldNotBeFound("description.specified=false");
    }

    @Test
    @Transactional
    void getAllBooksByDescriptionContainsSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where description contains DEFAULT_DESCRIPTION
        defaultBookShouldBeFound("description.contains=" + DEFAULT_DESCRIPTION);

        // Get all the bookList where description contains UPDATED_DESCRIPTION
        defaultBookShouldNotBeFound("description.contains=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllBooksByDescriptionNotContainsSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where description does not contain DEFAULT_DESCRIPTION
        defaultBookShouldNotBeFound("description.doesNotContain=" + DEFAULT_DESCRIPTION);

        // Get all the bookList where description does not contain UPDATED_DESCRIPTION
        defaultBookShouldBeFound("description.doesNotContain=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllBooksByPublicationDateIsEqualToSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where publicationDate equals to DEFAULT_PUBLICATION_DATE
        defaultBookShouldBeFound("publicationDate.equals=" + DEFAULT_PUBLICATION_DATE);

        // Get all the bookList where publicationDate equals to UPDATED_PUBLICATION_DATE
        defaultBookShouldNotBeFound("publicationDate.equals=" + UPDATED_PUBLICATION_DATE);
    }

    @Test
    @Transactional
    void getAllBooksByPublicationDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where publicationDate not equals to DEFAULT_PUBLICATION_DATE
        defaultBookShouldNotBeFound("publicationDate.notEquals=" + DEFAULT_PUBLICATION_DATE);

        // Get all the bookList where publicationDate not equals to UPDATED_PUBLICATION_DATE
        defaultBookShouldBeFound("publicationDate.notEquals=" + UPDATED_PUBLICATION_DATE);
    }

    @Test
    @Transactional
    void getAllBooksByPublicationDateIsInShouldWork() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where publicationDate in DEFAULT_PUBLICATION_DATE or UPDATED_PUBLICATION_DATE
        defaultBookShouldBeFound("publicationDate.in=" + DEFAULT_PUBLICATION_DATE + "," + UPDATED_PUBLICATION_DATE);

        // Get all the bookList where publicationDate equals to UPDATED_PUBLICATION_DATE
        defaultBookShouldNotBeFound("publicationDate.in=" + UPDATED_PUBLICATION_DATE);
    }

    @Test
    @Transactional
    void getAllBooksByPublicationDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where publicationDate is not null
        defaultBookShouldBeFound("publicationDate.specified=true");

        // Get all the bookList where publicationDate is null
        defaultBookShouldNotBeFound("publicationDate.specified=false");
    }

    @Test
    @Transactional
    void getAllBooksByPublicationDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where publicationDate is greater than or equal to DEFAULT_PUBLICATION_DATE
        defaultBookShouldBeFound("publicationDate.greaterThanOrEqual=" + DEFAULT_PUBLICATION_DATE);

        // Get all the bookList where publicationDate is greater than or equal to UPDATED_PUBLICATION_DATE
        defaultBookShouldNotBeFound("publicationDate.greaterThanOrEqual=" + UPDATED_PUBLICATION_DATE);
    }

    @Test
    @Transactional
    void getAllBooksByPublicationDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where publicationDate is less than or equal to DEFAULT_PUBLICATION_DATE
        defaultBookShouldBeFound("publicationDate.lessThanOrEqual=" + DEFAULT_PUBLICATION_DATE);

        // Get all the bookList where publicationDate is less than or equal to SMALLER_PUBLICATION_DATE
        defaultBookShouldNotBeFound("publicationDate.lessThanOrEqual=" + SMALLER_PUBLICATION_DATE);
    }

    @Test
    @Transactional
    void getAllBooksByPublicationDateIsLessThanSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where publicationDate is less than DEFAULT_PUBLICATION_DATE
        defaultBookShouldNotBeFound("publicationDate.lessThan=" + DEFAULT_PUBLICATION_DATE);

        // Get all the bookList where publicationDate is less than UPDATED_PUBLICATION_DATE
        defaultBookShouldBeFound("publicationDate.lessThan=" + UPDATED_PUBLICATION_DATE);
    }

    @Test
    @Transactional
    void getAllBooksByPublicationDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where publicationDate is greater than DEFAULT_PUBLICATION_DATE
        defaultBookShouldNotBeFound("publicationDate.greaterThan=" + DEFAULT_PUBLICATION_DATE);

        // Get all the bookList where publicationDate is greater than SMALLER_PUBLICATION_DATE
        defaultBookShouldBeFound("publicationDate.greaterThan=" + SMALLER_PUBLICATION_DATE);
    }

    @Test
    @Transactional
    void getAllBooksByPriceIsEqualToSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where price equals to DEFAULT_PRICE
        defaultBookShouldBeFound("price.equals=" + DEFAULT_PRICE);

        // Get all the bookList where price equals to UPDATED_PRICE
        defaultBookShouldNotBeFound("price.equals=" + UPDATED_PRICE);
    }

    @Test
    @Transactional
    void getAllBooksByPriceIsNotEqualToSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where price not equals to DEFAULT_PRICE
        defaultBookShouldNotBeFound("price.notEquals=" + DEFAULT_PRICE);

        // Get all the bookList where price not equals to UPDATED_PRICE
        defaultBookShouldBeFound("price.notEquals=" + UPDATED_PRICE);
    }

    @Test
    @Transactional
    void getAllBooksByPriceIsInShouldWork() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where price in DEFAULT_PRICE or UPDATED_PRICE
        defaultBookShouldBeFound("price.in=" + DEFAULT_PRICE + "," + UPDATED_PRICE);

        // Get all the bookList where price equals to UPDATED_PRICE
        defaultBookShouldNotBeFound("price.in=" + UPDATED_PRICE);
    }

    @Test
    @Transactional
    void getAllBooksByPriceIsNullOrNotNull() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where price is not null
        defaultBookShouldBeFound("price.specified=true");

        // Get all the bookList where price is null
        defaultBookShouldNotBeFound("price.specified=false");
    }

    @Test
    @Transactional
    void getAllBooksByPriceIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where price is greater than or equal to DEFAULT_PRICE
        defaultBookShouldBeFound("price.greaterThanOrEqual=" + DEFAULT_PRICE);

        // Get all the bookList where price is greater than or equal to UPDATED_PRICE
        defaultBookShouldNotBeFound("price.greaterThanOrEqual=" + UPDATED_PRICE);
    }

    @Test
    @Transactional
    void getAllBooksByPriceIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where price is less than or equal to DEFAULT_PRICE
        defaultBookShouldBeFound("price.lessThanOrEqual=" + DEFAULT_PRICE);

        // Get all the bookList where price is less than or equal to SMALLER_PRICE
        defaultBookShouldNotBeFound("price.lessThanOrEqual=" + SMALLER_PRICE);
    }

    @Test
    @Transactional
    void getAllBooksByPriceIsLessThanSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where price is less than DEFAULT_PRICE
        defaultBookShouldNotBeFound("price.lessThan=" + DEFAULT_PRICE);

        // Get all the bookList where price is less than UPDATED_PRICE
        defaultBookShouldBeFound("price.lessThan=" + UPDATED_PRICE);
    }

    @Test
    @Transactional
    void getAllBooksByPriceIsGreaterThanSomething() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList where price is greater than DEFAULT_PRICE
        defaultBookShouldNotBeFound("price.greaterThan=" + DEFAULT_PRICE);

        // Get all the bookList where price is greater than SMALLER_PRICE
        defaultBookShouldBeFound("price.greaterThan=" + SMALLER_PRICE);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultBookShouldBeFound(String filter) throws Exception {
        restBookMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(book.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].publicationDate").value(hasItem(DEFAULT_PUBLICATION_DATE.toString())))
            .andExpect(jsonPath("$.[*].price").value(hasItem(sameNumber(DEFAULT_PRICE))))
            .andExpect(jsonPath("$.[*].coverContentType").value(hasItem(DEFAULT_COVER_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].cover").value(hasItem(Base64Utils.encodeToString(DEFAULT_COVER))));

        // Check, that the count call also returns 1
        restBookMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultBookShouldNotBeFound(String filter) throws Exception {
        restBookMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restBookMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingBook() throws Exception {
        // Get the book
        restBookMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewBook() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        int databaseSizeBeforeUpdate = bookRepository.findAll().size();

        // Update the book
        Book updatedBook = bookRepository.findById(book.getId()).get();
        // Disconnect from session so that the updates on updatedBook are not directly saved in db
        em.detach(updatedBook);
        updatedBook
            .title(UPDATED_TITLE)
            .description(UPDATED_DESCRIPTION)
            .publicationDate(UPDATED_PUBLICATION_DATE)
            .price(UPDATED_PRICE)
            .cover(UPDATED_COVER)
            .coverContentType(UPDATED_COVER_CONTENT_TYPE);

        restBookMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedBook.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedBook))
            )
            .andExpect(status().isOk());

        // Validate the Book in the database
        List<Book> bookList = bookRepository.findAll();
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate);
        Book testBook = bookList.get(bookList.size() - 1);
        assertThat(testBook.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testBook.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testBook.getPublicationDate()).isEqualTo(UPDATED_PUBLICATION_DATE);
        assertThat(testBook.getPrice()).isEqualTo(UPDATED_PRICE);
        assertThat(testBook.getCover()).isEqualTo(UPDATED_COVER);
        assertThat(testBook.getCoverContentType()).isEqualTo(UPDATED_COVER_CONTENT_TYPE);
    }

    @Test
    @Transactional
    void putNonExistingBook() throws Exception {
        int databaseSizeBeforeUpdate = bookRepository.findAll().size();
        book.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBookMockMvc
            .perform(
                put(ENTITY_API_URL_ID, book.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(book))
            )
            .andExpect(status().isBadRequest());

        // Validate the Book in the database
        List<Book> bookList = bookRepository.findAll();
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchBook() throws Exception {
        int databaseSizeBeforeUpdate = bookRepository.findAll().size();
        book.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBookMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(book))
            )
            .andExpect(status().isBadRequest());

        // Validate the Book in the database
        List<Book> bookList = bookRepository.findAll();
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamBook() throws Exception {
        int databaseSizeBeforeUpdate = bookRepository.findAll().size();
        book.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBookMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(book)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Book in the database
        List<Book> bookList = bookRepository.findAll();
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateBookWithPatch() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        int databaseSizeBeforeUpdate = bookRepository.findAll().size();

        // Update the book using partial update
        Book partialUpdatedBook = new Book();
        partialUpdatedBook.setId(book.getId());

        partialUpdatedBook.title(UPDATED_TITLE).publicationDate(UPDATED_PUBLICATION_DATE);

        restBookMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBook.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedBook))
            )
            .andExpect(status().isOk());

        // Validate the Book in the database
        List<Book> bookList = bookRepository.findAll();
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate);
        Book testBook = bookList.get(bookList.size() - 1);
        assertThat(testBook.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testBook.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testBook.getPublicationDate()).isEqualTo(UPDATED_PUBLICATION_DATE);
        assertThat(testBook.getPrice()).isEqualByComparingTo(DEFAULT_PRICE);
        assertThat(testBook.getCover()).isEqualTo(DEFAULT_COVER);
        assertThat(testBook.getCoverContentType()).isEqualTo(DEFAULT_COVER_CONTENT_TYPE);
    }

    @Test
    @Transactional
    void fullUpdateBookWithPatch() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        int databaseSizeBeforeUpdate = bookRepository.findAll().size();

        // Update the book using partial update
        Book partialUpdatedBook = new Book();
        partialUpdatedBook.setId(book.getId());

        partialUpdatedBook
            .title(UPDATED_TITLE)
            .description(UPDATED_DESCRIPTION)
            .publicationDate(UPDATED_PUBLICATION_DATE)
            .price(UPDATED_PRICE)
            .cover(UPDATED_COVER)
            .coverContentType(UPDATED_COVER_CONTENT_TYPE);

        restBookMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBook.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedBook))
            )
            .andExpect(status().isOk());

        // Validate the Book in the database
        List<Book> bookList = bookRepository.findAll();
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate);
        Book testBook = bookList.get(bookList.size() - 1);
        assertThat(testBook.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testBook.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testBook.getPublicationDate()).isEqualTo(UPDATED_PUBLICATION_DATE);
        assertThat(testBook.getPrice()).isEqualByComparingTo(UPDATED_PRICE);
        assertThat(testBook.getCover()).isEqualTo(UPDATED_COVER);
        assertThat(testBook.getCoverContentType()).isEqualTo(UPDATED_COVER_CONTENT_TYPE);
    }

    @Test
    @Transactional
    void patchNonExistingBook() throws Exception {
        int databaseSizeBeforeUpdate = bookRepository.findAll().size();
        book.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBookMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, book.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(book))
            )
            .andExpect(status().isBadRequest());

        // Validate the Book in the database
        List<Book> bookList = bookRepository.findAll();
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchBook() throws Exception {
        int databaseSizeBeforeUpdate = bookRepository.findAll().size();
        book.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBookMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(book))
            )
            .andExpect(status().isBadRequest());

        // Validate the Book in the database
        List<Book> bookList = bookRepository.findAll();
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamBook() throws Exception {
        int databaseSizeBeforeUpdate = bookRepository.findAll().size();
        book.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBookMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(book)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Book in the database
        List<Book> bookList = bookRepository.findAll();
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteBook() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        int databaseSizeBeforeDelete = bookRepository.findAll().size();

        // Delete the book
        restBookMockMvc
            .perform(delete(ENTITY_API_URL_ID, book.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Book> bookList = bookRepository.findAll();
        assertThat(bookList).hasSize(databaseSizeBeforeDelete - 1);
    }
}

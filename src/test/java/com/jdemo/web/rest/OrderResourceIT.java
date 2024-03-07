package com.jdemo.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jdemo.IntegrationTest;
import com.jdemo.domain.Order;
import com.jdemo.repository.OrderRepository;
import com.jdemo.service.criteria.OrderCriteria;
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
 * Integration tests for the {@link OrderResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class OrderResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Float DEFAULT_TOTALPRICE = 1F;
    private static final Float UPDATED_TOTALPRICE = 2F;
    private static final Float SMALLER_TOTALPRICE = 1F - 1F;

    private static final String ENTITY_API_URL = "/api/orders";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restOrderMockMvc;

    private Order order;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Order createEntity(EntityManager em) {
        Order order = new Order().name(DEFAULT_NAME).totalprice(DEFAULT_TOTALPRICE);
        return order;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Order createUpdatedEntity(EntityManager em) {
        Order order = new Order().name(UPDATED_NAME).totalprice(UPDATED_TOTALPRICE);
        return order;
    }

    @BeforeEach
    public void initTest() {
        order = createEntity(em);
    }

    @Test
    @Transactional
    void createOrder() throws Exception {
        int databaseSizeBeforeCreate = orderRepository.findAll().size();
        // Create the Order
        restOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(order)))
            .andExpect(status().isCreated());

        // Validate the Order in the database
        List<Order> orderList = orderRepository.findAll();
        assertThat(orderList).hasSize(databaseSizeBeforeCreate + 1);
        Order testOrder = orderList.get(orderList.size() - 1);
        assertThat(testOrder.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testOrder.getTotalprice()).isEqualTo(DEFAULT_TOTALPRICE);
    }

    @Test
    @Transactional
    void createOrderWithExistingId() throws Exception {
        // Create the Order with an existing ID
        order.setId(1L);

        int databaseSizeBeforeCreate = orderRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(order)))
            .andExpect(status().isBadRequest());

        // Validate the Order in the database
        List<Order> orderList = orderRepository.findAll();
        assertThat(orderList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllOrders() throws Exception {
        // Initialize the database
        orderRepository.saveAndFlush(order);

        // Get all the orderList
        restOrderMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(order.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].totalprice").value(hasItem(DEFAULT_TOTALPRICE.doubleValue())));
    }

    @Test
    @Transactional
    void getOrder() throws Exception {
        // Initialize the database
        orderRepository.saveAndFlush(order);

        // Get the order
        restOrderMockMvc
            .perform(get(ENTITY_API_URL_ID, order.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(order.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.totalprice").value(DEFAULT_TOTALPRICE.doubleValue()));
    }

    @Test
    @Transactional
    void getOrdersByIdFiltering() throws Exception {
        // Initialize the database
        orderRepository.saveAndFlush(order);

        Long id = order.getId();

        defaultOrderShouldBeFound("id.equals=" + id);
        defaultOrderShouldNotBeFound("id.notEquals=" + id);

        defaultOrderShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultOrderShouldNotBeFound("id.greaterThan=" + id);

        defaultOrderShouldBeFound("id.lessThanOrEqual=" + id);
        defaultOrderShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllOrdersByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        orderRepository.saveAndFlush(order);

        // Get all the orderList where name equals to DEFAULT_NAME
        defaultOrderShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the orderList where name equals to UPDATED_NAME
        defaultOrderShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllOrdersByNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        orderRepository.saveAndFlush(order);

        // Get all the orderList where name not equals to DEFAULT_NAME
        defaultOrderShouldNotBeFound("name.notEquals=" + DEFAULT_NAME);

        // Get all the orderList where name not equals to UPDATED_NAME
        defaultOrderShouldBeFound("name.notEquals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllOrdersByNameIsInShouldWork() throws Exception {
        // Initialize the database
        orderRepository.saveAndFlush(order);

        // Get all the orderList where name in DEFAULT_NAME or UPDATED_NAME
        defaultOrderShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the orderList where name equals to UPDATED_NAME
        defaultOrderShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllOrdersByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        orderRepository.saveAndFlush(order);

        // Get all the orderList where name is not null
        defaultOrderShouldBeFound("name.specified=true");

        // Get all the orderList where name is null
        defaultOrderShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    void getAllOrdersByNameContainsSomething() throws Exception {
        // Initialize the database
        orderRepository.saveAndFlush(order);

        // Get all the orderList where name contains DEFAULT_NAME
        defaultOrderShouldBeFound("name.contains=" + DEFAULT_NAME);

        // Get all the orderList where name contains UPDATED_NAME
        defaultOrderShouldNotBeFound("name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllOrdersByNameNotContainsSomething() throws Exception {
        // Initialize the database
        orderRepository.saveAndFlush(order);

        // Get all the orderList where name does not contain DEFAULT_NAME
        defaultOrderShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);

        // Get all the orderList where name does not contain UPDATED_NAME
        defaultOrderShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllOrdersByTotalpriceIsEqualToSomething() throws Exception {
        // Initialize the database
        orderRepository.saveAndFlush(order);

        // Get all the orderList where totalprice equals to DEFAULT_TOTALPRICE
        defaultOrderShouldBeFound("totalprice.equals=" + DEFAULT_TOTALPRICE);

        // Get all the orderList where totalprice equals to UPDATED_TOTALPRICE
        defaultOrderShouldNotBeFound("totalprice.equals=" + UPDATED_TOTALPRICE);
    }

    @Test
    @Transactional
    void getAllOrdersByTotalpriceIsNotEqualToSomething() throws Exception {
        // Initialize the database
        orderRepository.saveAndFlush(order);

        // Get all the orderList where totalprice not equals to DEFAULT_TOTALPRICE
        defaultOrderShouldNotBeFound("totalprice.notEquals=" + DEFAULT_TOTALPRICE);

        // Get all the orderList where totalprice not equals to UPDATED_TOTALPRICE
        defaultOrderShouldBeFound("totalprice.notEquals=" + UPDATED_TOTALPRICE);
    }

    @Test
    @Transactional
    void getAllOrdersByTotalpriceIsInShouldWork() throws Exception {
        // Initialize the database
        orderRepository.saveAndFlush(order);

        // Get all the orderList where totalprice in DEFAULT_TOTALPRICE or UPDATED_TOTALPRICE
        defaultOrderShouldBeFound("totalprice.in=" + DEFAULT_TOTALPRICE + "," + UPDATED_TOTALPRICE);

        // Get all the orderList where totalprice equals to UPDATED_TOTALPRICE
        defaultOrderShouldNotBeFound("totalprice.in=" + UPDATED_TOTALPRICE);
    }

    @Test
    @Transactional
    void getAllOrdersByTotalpriceIsNullOrNotNull() throws Exception {
        // Initialize the database
        orderRepository.saveAndFlush(order);

        // Get all the orderList where totalprice is not null
        defaultOrderShouldBeFound("totalprice.specified=true");

        // Get all the orderList where totalprice is null
        defaultOrderShouldNotBeFound("totalprice.specified=false");
    }

    @Test
    @Transactional
    void getAllOrdersByTotalpriceIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        orderRepository.saveAndFlush(order);

        // Get all the orderList where totalprice is greater than or equal to DEFAULT_TOTALPRICE
        defaultOrderShouldBeFound("totalprice.greaterThanOrEqual=" + DEFAULT_TOTALPRICE);

        // Get all the orderList where totalprice is greater than or equal to UPDATED_TOTALPRICE
        defaultOrderShouldNotBeFound("totalprice.greaterThanOrEqual=" + UPDATED_TOTALPRICE);
    }

    @Test
    @Transactional
    void getAllOrdersByTotalpriceIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        orderRepository.saveAndFlush(order);

        // Get all the orderList where totalprice is less than or equal to DEFAULT_TOTALPRICE
        defaultOrderShouldBeFound("totalprice.lessThanOrEqual=" + DEFAULT_TOTALPRICE);

        // Get all the orderList where totalprice is less than or equal to SMALLER_TOTALPRICE
        defaultOrderShouldNotBeFound("totalprice.lessThanOrEqual=" + SMALLER_TOTALPRICE);
    }

    @Test
    @Transactional
    void getAllOrdersByTotalpriceIsLessThanSomething() throws Exception {
        // Initialize the database
        orderRepository.saveAndFlush(order);

        // Get all the orderList where totalprice is less than DEFAULT_TOTALPRICE
        defaultOrderShouldNotBeFound("totalprice.lessThan=" + DEFAULT_TOTALPRICE);

        // Get all the orderList where totalprice is less than UPDATED_TOTALPRICE
        defaultOrderShouldBeFound("totalprice.lessThan=" + UPDATED_TOTALPRICE);
    }

    @Test
    @Transactional
    void getAllOrdersByTotalpriceIsGreaterThanSomething() throws Exception {
        // Initialize the database
        orderRepository.saveAndFlush(order);

        // Get all the orderList where totalprice is greater than DEFAULT_TOTALPRICE
        defaultOrderShouldNotBeFound("totalprice.greaterThan=" + DEFAULT_TOTALPRICE);

        // Get all the orderList where totalprice is greater than SMALLER_TOTALPRICE
        defaultOrderShouldBeFound("totalprice.greaterThan=" + SMALLER_TOTALPRICE);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultOrderShouldBeFound(String filter) throws Exception {
        restOrderMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(order.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].totalprice").value(hasItem(DEFAULT_TOTALPRICE.doubleValue())));

        // Check, that the count call also returns 1
        restOrderMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultOrderShouldNotBeFound(String filter) throws Exception {
        restOrderMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restOrderMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingOrder() throws Exception {
        // Get the order
        restOrderMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewOrder() throws Exception {
        // Initialize the database
        orderRepository.saveAndFlush(order);

        int databaseSizeBeforeUpdate = orderRepository.findAll().size();

        // Update the order
        Order updatedOrder = orderRepository.findById(order.getId()).get();
        // Disconnect from session so that the updates on updatedOrder are not directly saved in db
        em.detach(updatedOrder);
        updatedOrder.name(UPDATED_NAME).totalprice(UPDATED_TOTALPRICE);

        restOrderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedOrder.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedOrder))
            )
            .andExpect(status().isOk());

        // Validate the Order in the database
        List<Order> orderList = orderRepository.findAll();
        assertThat(orderList).hasSize(databaseSizeBeforeUpdate);
        Order testOrder = orderList.get(orderList.size() - 1);
        assertThat(testOrder.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testOrder.getTotalprice()).isEqualTo(UPDATED_TOTALPRICE);
    }

    @Test
    @Transactional
    void putNonExistingOrder() throws Exception {
        int databaseSizeBeforeUpdate = orderRepository.findAll().size();
        order.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restOrderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, order.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(order))
            )
            .andExpect(status().isBadRequest());

        // Validate the Order in the database
        List<Order> orderList = orderRepository.findAll();
        assertThat(orderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchOrder() throws Exception {
        int databaseSizeBeforeUpdate = orderRepository.findAll().size();
        order.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOrderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(order))
            )
            .andExpect(status().isBadRequest());

        // Validate the Order in the database
        List<Order> orderList = orderRepository.findAll();
        assertThat(orderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamOrder() throws Exception {
        int databaseSizeBeforeUpdate = orderRepository.findAll().size();
        order.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOrderMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(order)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Order in the database
        List<Order> orderList = orderRepository.findAll();
        assertThat(orderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateOrderWithPatch() throws Exception {
        // Initialize the database
        orderRepository.saveAndFlush(order);

        int databaseSizeBeforeUpdate = orderRepository.findAll().size();

        // Update the order using partial update
        Order partialUpdatedOrder = new Order();
        partialUpdatedOrder.setId(order.getId());

        restOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedOrder.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedOrder))
            )
            .andExpect(status().isOk());

        // Validate the Order in the database
        List<Order> orderList = orderRepository.findAll();
        assertThat(orderList).hasSize(databaseSizeBeforeUpdate);
        Order testOrder = orderList.get(orderList.size() - 1);
        assertThat(testOrder.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testOrder.getTotalprice()).isEqualTo(DEFAULT_TOTALPRICE);
    }

    @Test
    @Transactional
    void fullUpdateOrderWithPatch() throws Exception {
        // Initialize the database
        orderRepository.saveAndFlush(order);

        int databaseSizeBeforeUpdate = orderRepository.findAll().size();

        // Update the order using partial update
        Order partialUpdatedOrder = new Order();
        partialUpdatedOrder.setId(order.getId());

        partialUpdatedOrder.name(UPDATED_NAME).totalprice(UPDATED_TOTALPRICE);

        restOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedOrder.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedOrder))
            )
            .andExpect(status().isOk());

        // Validate the Order in the database
        List<Order> orderList = orderRepository.findAll();
        assertThat(orderList).hasSize(databaseSizeBeforeUpdate);
        Order testOrder = orderList.get(orderList.size() - 1);
        assertThat(testOrder.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testOrder.getTotalprice()).isEqualTo(UPDATED_TOTALPRICE);
    }

    @Test
    @Transactional
    void patchNonExistingOrder() throws Exception {
        int databaseSizeBeforeUpdate = orderRepository.findAll().size();
        order.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, order.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(order))
            )
            .andExpect(status().isBadRequest());

        // Validate the Order in the database
        List<Order> orderList = orderRepository.findAll();
        assertThat(orderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchOrder() throws Exception {
        int databaseSizeBeforeUpdate = orderRepository.findAll().size();
        order.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(order))
            )
            .andExpect(status().isBadRequest());

        // Validate the Order in the database
        List<Order> orderList = orderRepository.findAll();
        assertThat(orderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamOrder() throws Exception {
        int databaseSizeBeforeUpdate = orderRepository.findAll().size();
        order.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOrderMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(order)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Order in the database
        List<Order> orderList = orderRepository.findAll();
        assertThat(orderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteOrder() throws Exception {
        // Initialize the database
        orderRepository.saveAndFlush(order);

        int databaseSizeBeforeDelete = orderRepository.findAll().size();

        // Delete the order
        restOrderMockMvc
            .perform(delete(ENTITY_API_URL_ID, order.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Order> orderList = orderRepository.findAll();
        assertThat(orderList).hasSize(databaseSizeBeforeDelete - 1);
    }
}

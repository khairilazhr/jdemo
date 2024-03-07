<template>
  <div>
    <h2 id="page-heading" data-cy="ReviewHeading">
      <span v-text="$t('jdemoApp.review.home.title')" id="review-heading">Reviews</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info mr-2" v-on:click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="$t('jdemoApp.review.home.refreshListLabel')">Refresh List</span>
        </button>
        <router-link :to="{ name: 'ReviewCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-review"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span v-text="$t('jdemoApp.review.home.createLabel')"> Create a new Review </span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && reviews && reviews.length === 0">
      <span v-text="$t('jdemoApp.review.home.notFound')">No reviews found</span>
    </div>
    <div class="table-responsive" v-if="reviews && reviews.length > 0">
      <table class="table table-striped" aria-describedby="reviews">
        <thead>
          <tr>
            <th scope="row"><span v-text="$t('global.field.id')">ID</span></th>
            <th scope="row"><span v-text="$t('jdemoApp.review.title')">Title</span></th>
            <th scope="row"><span v-text="$t('jdemoApp.review.content')">Content</span></th>
            <th scope="row"><span v-text="$t('jdemoApp.review.rating')">Rating</span></th>
            <th scope="row"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="review in reviews" :key="review.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'ReviewView', params: { reviewId: review.id } }">{{ review.id }}</router-link>
            </td>
            <td>{{ review.title }}</td>
            <td>{{ review.content }}</td>
            <td>{{ review.rating }}</td>
            <td class="text-right">
              <div class="btn-group">
                <router-link :to="{ name: 'ReviewView', params: { reviewId: review.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'ReviewEdit', params: { reviewId: review.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                  </button>
                </router-link>
                <b-button
                  v-on:click="prepareRemove(review)"
                  variant="danger"
                  class="btn btn-sm"
                  data-cy="entityDeleteButton"
                  v-b-modal.removeEntity
                >
                  <font-awesome-icon icon="times"></font-awesome-icon>
                  <span class="d-none d-md-inline" v-text="$t('entity.action.delete')">Delete</span>
                </b-button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <b-modal ref="removeEntity" id="removeEntity">
      <span slot="modal-title"
        ><span id="jdemoApp.review.delete.question" data-cy="reviewDeleteDialogHeading" v-text="$t('entity.delete.title')"
          >Confirm delete operation</span
        ></span
      >
      <div class="modal-body">
        <p id="jhi-delete-review-heading" v-text="$t('jdemoApp.review.delete.question', { id: removeId })">
          Are you sure you want to delete this Review?
        </p>
      </div>
      <div slot="modal-footer">
        <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
        <button
          type="button"
          class="btn btn-primary"
          id="jhi-confirm-delete-review"
          data-cy="entityConfirmDeleteButton"
          v-text="$t('entity.action.delete')"
          v-on:click="removeReview()"
        >
          Delete
        </button>
      </div>
    </b-modal>
  </div>
</template>

<script lang="ts" src="./review.component.ts"></script>

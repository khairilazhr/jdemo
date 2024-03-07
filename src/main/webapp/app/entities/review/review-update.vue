<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" role="form" novalidate v-on:submit.prevent="save()">
        <h2
          id="jdemoApp.review.home.createOrEditLabel"
          data-cy="ReviewCreateUpdateHeading"
          v-text="$t('jdemoApp.review.home.createOrEditLabel')"
        >
          Create or edit a Review
        </h2>
        <div>
          <div class="form-group" v-if="review.id">
            <label for="id" v-text="$t('global.field.id')">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="review.id" readonly />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('jdemoApp.review.title')" for="review-title">Title</label>
            <input
              type="text"
              class="form-control"
              name="title"
              id="review-title"
              data-cy="title"
              :class="{ valid: !$v.review.title.$invalid, invalid: $v.review.title.$invalid }"
              v-model="$v.review.title.$model"
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('jdemoApp.review.content')" for="review-content">Content</label>
            <input
              type="text"
              class="form-control"
              name="content"
              id="review-content"
              data-cy="content"
              :class="{ valid: !$v.review.content.$invalid, invalid: $v.review.content.$invalid }"
              v-model="$v.review.content.$model"
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('jdemoApp.review.rating')" for="review-rating">Rating</label>
            <input
              type="number"
              class="form-control"
              name="rating"
              id="review-rating"
              data-cy="rating"
              :class="{ valid: !$v.review.rating.$invalid, invalid: $v.review.rating.$invalid }"
              v-model.number="$v.review.rating.$model"
            />
          </div>
        </div>
        <div>
          <button type="button" id="cancel-save" class="btn btn-secondary" v-on:click="previousState()">
            <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.cancel')">Cancel</span>
          </button>
          <button
            type="submit"
            id="save-entity"
            data-cy="entityCreateSaveButton"
            :disabled="$v.review.$invalid || isSaving"
            class="btn btn-primary"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
<script lang="ts" src="./review-update.component.ts"></script>

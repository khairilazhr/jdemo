<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="review">
        <h2 class="jh-entity-heading" data-cy="reviewDetailsHeading">
          <span v-text="$t('jdemoApp.review.detail.title')">Review</span> {{ review.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="$t('jdemoApp.review.title')">Title</span>
          </dt>
          <dd>
            <span>{{ review.title }}</span>
          </dd>
          <dt>
            <span v-text="$t('jdemoApp.review.content')">Content</span>
          </dt>
          <dd>
            <span>{{ review.content }}</span>
          </dd>
          <dt>
            <span v-text="$t('jdemoApp.review.rating')">Rating</span>
          </dt>
          <dd>
            <span>{{ review.rating }}</span>
          </dd>
        </dl>
        <button type="submit" v-on:click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
        </button>
        <router-link v-if="review.id" :to="{ name: 'ReviewEdit', params: { reviewId: review.id } }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./review-details.component.ts"></script>

<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="author">
        <h2 class="jh-entity-heading" data-cy="authorDetailsHeading">
          <span v-text="$t('jdemoApp.author.detail.title')">Author</span> {{ author.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="$t('jdemoApp.author.name')">Name</span>
          </dt>
          <dd>
            <span>{{ author.name }}</span>
          </dd>
          <dt>
            <span v-text="$t('jdemoApp.author.age')">Age</span>
          </dt>
          <dd>
            <span>{{ author.age }}</span>
          </dd>
          <dt>
            <span v-text="$t('jdemoApp.author.birthDate')">Birth Date</span>
          </dt>
          <dd>
            <span>{{ author.birthDate }}</span>
          </dd>
        </dl>
        <button type="submit" v-on:click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
        </button>
        <router-link v-if="author.id" :to="{ name: 'AuthorEdit', params: { authorId: author.id } }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./author-details.component.ts"></script>

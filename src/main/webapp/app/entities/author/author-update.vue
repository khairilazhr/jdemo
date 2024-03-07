<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" role="form" novalidate v-on:submit.prevent="save()">
        <h2
          id="jdemoApp.author.home.createOrEditLabel"
          data-cy="AuthorCreateUpdateHeading"
          v-text="$t('jdemoApp.author.home.createOrEditLabel')"
        >
          Create or edit a Author
        </h2>
        <div>
          <div class="form-group" v-if="author.id">
            <label for="id" v-text="$t('global.field.id')">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="author.id" readonly />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('jdemoApp.author.name')" for="author-name">Name</label>
            <input
              type="text"
              class="form-control"
              name="name"
              id="author-name"
              data-cy="name"
              :class="{ valid: !$v.author.name.$invalid, invalid: $v.author.name.$invalid }"
              v-model="$v.author.name.$model"
              required
            />
            <div v-if="$v.author.name.$anyDirty && $v.author.name.$invalid">
              <small class="form-text text-danger" v-if="!$v.author.name.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('jdemoApp.author.age')" for="author-age">Age</label>
            <input
              type="text"
              class="form-control"
              name="age"
              id="author-age"
              data-cy="age"
              :class="{ valid: !$v.author.age.$invalid, invalid: $v.author.age.$invalid }"
              v-model="$v.author.age.$model"
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('jdemoApp.author.birthDate')" for="author-birthDate">Birth Date</label>
            <b-input-group class="mb-3">
              <b-input-group-prepend>
                <b-form-datepicker
                  aria-controls="author-birthDate"
                  v-model="$v.author.birthDate.$model"
                  name="birthDate"
                  class="form-control"
                  :locale="currentLanguage"
                  button-only
                  today-button
                  reset-button
                  close-button
                >
                </b-form-datepicker>
              </b-input-group-prepend>
              <b-form-input
                id="author-birthDate"
                data-cy="birthDate"
                type="text"
                class="form-control"
                name="birthDate"
                :class="{ valid: !$v.author.birthDate.$invalid, invalid: $v.author.birthDate.$invalid }"
                v-model="$v.author.birthDate.$model"
              />
            </b-input-group>
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
            :disabled="$v.author.$invalid || isSaving"
            class="btn btn-primary"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
<script lang="ts" src="./author-update.component.ts"></script>

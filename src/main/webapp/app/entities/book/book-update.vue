<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" role="form" novalidate v-on:submit.prevent="save()">
        <h2 id="jdemoApp.book.home.createOrEditLabel" data-cy="BookCreateUpdateHeading" v-text="$t('jdemoApp.book.home.createOrEditLabel')">
          Create or edit a Book
        </h2>
        <div>
          <div class="form-group" v-if="book.id">
            <label for="id" v-text="$t('global.field.id')">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="book.id" readonly />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('jdemoApp.book.title')" for="book-title">Title</label>
            <input
              type="text"
              class="form-control"
              name="title"
              id="book-title"
              data-cy="title"
              :class="{ valid: !$v.book.title.$invalid, invalid: $v.book.title.$invalid }"
              v-model="$v.book.title.$model"
              required
            />
            <div v-if="$v.book.title.$anyDirty && $v.book.title.$invalid">
              <small class="form-text text-danger" v-if="!$v.book.title.required" v-text="$t('entity.validation.required')">
                This field is required.
              </small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('jdemoApp.book.description')" for="book-description">Description</label>
            <input
              type="text"
              class="form-control"
              name="description"
              id="book-description"
              data-cy="description"
              :class="{ valid: !$v.book.description.$invalid, invalid: $v.book.description.$invalid }"
              v-model="$v.book.description.$model"
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('jdemoApp.book.publicationDate')" for="book-publicationDate"
              >Publication Date</label
            >
            <b-input-group class="mb-3">
              <b-input-group-prepend>
                <b-form-datepicker
                  aria-controls="book-publicationDate"
                  v-model="$v.book.publicationDate.$model"
                  name="publicationDate"
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
                id="book-publicationDate"
                data-cy="publicationDate"
                type="text"
                class="form-control"
                name="publicationDate"
                :class="{ valid: !$v.book.publicationDate.$invalid, invalid: $v.book.publicationDate.$invalid }"
                v-model="$v.book.publicationDate.$model"
              />
            </b-input-group>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('jdemoApp.book.price')" for="book-price">Price</label>
            <input
              type="number"
              class="form-control"
              name="price"
              id="book-price"
              data-cy="price"
              :class="{ valid: !$v.book.price.$invalid, invalid: $v.book.price.$invalid }"
              v-model.number="$v.book.price.$model"
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="$t('jdemoApp.book.cover')" for="book-cover">Cover</label>
            <div>
              <div v-if="book.cover" class="form-text text-danger clearfix">
                <a class="pull-left" v-on:click="openFile(book.coverContentType, book.cover)" v-text="$t('entity.action.open')">open</a
                ><br />
                <span class="pull-left">{{ book.coverContentType }}, {{ byteSize(book.cover) }}</span>
                <button
                  type="button"
                  v-on:click="
                    book.cover = null;
                    book.coverContentType = null;
                  "
                  class="btn btn-secondary btn-xs pull-right"
                >
                  <font-awesome-icon icon="times"></font-awesome-icon>
                </button>
              </div>
              <input
                type="file"
                ref="file_cover"
                id="file_cover"
                data-cy="cover"
                v-on:change="setFileData($event, book, 'cover', false)"
                v-text="$t('entity.action.addblob')"
              />
            </div>
            <input
              type="hidden"
              class="form-control"
              name="cover"
              id="book-cover"
              data-cy="cover"
              :class="{ valid: !$v.book.cover.$invalid, invalid: $v.book.cover.$invalid }"
              v-model="$v.book.cover.$model"
            />
            <input type="hidden" class="form-control" name="coverContentType" id="book-coverContentType" v-model="book.coverContentType" />
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
            :disabled="$v.book.$invalid || isSaving"
            class="btn btn-primary"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.save')">Save</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
<script lang="ts" src="./book-update.component.ts"></script>

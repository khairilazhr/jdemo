/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import Router from 'vue-router';

import * as config from '@/shared/config/config';
import ReviewUpdateComponent from '@/entities/review/review-update.vue';
import ReviewClass from '@/entities/review/review-update.component';
import ReviewService from '@/entities/review/review.service';

const localVue = createLocalVue();

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
const router = new Router();
localVue.use(Router);
localVue.component('font-awesome-icon', {});
localVue.component('b-input-group', {});
localVue.component('b-input-group-prepend', {});
localVue.component('b-form-datepicker', {});
localVue.component('b-form-input', {});

describe('Component Tests', () => {
  describe('Review Management Update Component', () => {
    let wrapper: Wrapper<ReviewClass>;
    let comp: ReviewClass;
    let reviewServiceStub: SinonStubbedInstance<ReviewService>;

    beforeEach(() => {
      reviewServiceStub = sinon.createStubInstance<ReviewService>(ReviewService);

      wrapper = shallowMount<ReviewClass>(ReviewUpdateComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: {
          reviewService: () => reviewServiceStub,
        },
      });
      comp = wrapper.vm;
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const entity = { id: 123 };
        comp.review = entity;
        reviewServiceStub.update.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(reviewServiceStub.update.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comp.review = entity;
        reviewServiceStub.create.resolves(entity);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(reviewServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        const foundReview = { id: 123 };
        reviewServiceStub.find.resolves(foundReview);
        reviewServiceStub.retrieve.resolves([foundReview]);

        // WHEN
        comp.beforeRouteEnter({ params: { reviewId: 123 } }, null, cb => cb(comp));
        await comp.$nextTick();

        // THEN
        expect(comp.review).toBe(foundReview);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        comp.previousState();
        await comp.$nextTick();

        expect(comp.$router.currentRoute.fullPath).toContain('/');
      });
    });
  });
});

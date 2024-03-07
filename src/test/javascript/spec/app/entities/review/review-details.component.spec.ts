/* tslint:disable max-line-length */
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import sinon, { SinonStubbedInstance } from 'sinon';
import VueRouter from 'vue-router';

import * as config from '@/shared/config/config';
import ReviewDetailComponent from '@/entities/review/review-details.vue';
import ReviewClass from '@/entities/review/review-details.component';
import ReviewService from '@/entities/review/review.service';
import router from '@/router';

const localVue = createLocalVue();
localVue.use(VueRouter);

config.initVueApp(localVue);
const i18n = config.initI18N(localVue);
const store = config.initVueXStore(localVue);
localVue.component('font-awesome-icon', {});
localVue.component('router-link', {});

describe('Component Tests', () => {
  describe('Review Management Detail Component', () => {
    let wrapper: Wrapper<ReviewClass>;
    let comp: ReviewClass;
    let reviewServiceStub: SinonStubbedInstance<ReviewService>;

    beforeEach(() => {
      reviewServiceStub = sinon.createStubInstance<ReviewService>(ReviewService);

      wrapper = shallowMount<ReviewClass>(ReviewDetailComponent, {
        store,
        i18n,
        localVue,
        router,
        provide: { reviewService: () => reviewServiceStub },
      });
      comp = wrapper.vm;
    });

    describe('OnInit', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        const foundReview = { id: 123 };
        reviewServiceStub.find.resolves(foundReview);

        // WHEN
        comp.retrieveReview(123);
        await comp.$nextTick();

        // THEN
        expect(comp.review).toBe(foundReview);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        const foundReview = { id: 123 };
        reviewServiceStub.find.resolves(foundReview);

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

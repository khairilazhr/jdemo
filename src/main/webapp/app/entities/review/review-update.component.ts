import { Component, Vue, Inject } from 'vue-property-decorator';

import { IReview, Review } from '@/shared/model/review.model';
import ReviewService from './review.service';

const validations: any = {
  review: {
    title: {},
    content: {},
    rating: {},
  },
};

@Component({
  validations,
})
export default class ReviewUpdate extends Vue {
  @Inject('reviewService') private reviewService: () => ReviewService;
  public review: IReview = new Review();
  public isSaving = false;
  public currentLanguage = '';

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.reviewId) {
        vm.retrieveReview(to.params.reviewId);
      }
    });
  }

  created(): void {
    this.currentLanguage = this.$store.getters.currentLanguage;
    this.$store.watch(
      () => this.$store.getters.currentLanguage,
      () => {
        this.currentLanguage = this.$store.getters.currentLanguage;
      }
    );
  }

  public save(): void {
    this.isSaving = true;
    if (this.review.id) {
      this.reviewService()
        .update(this.review)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('jdemoApp.review.updated', { param: param.id });
          return this.$root.$bvToast.toast(message.toString(), {
            toaster: 'b-toaster-top-center',
            title: 'Info',
            variant: 'info',
            solid: true,
            autoHideDelay: 5000,
          });
        });
    } else {
      this.reviewService()
        .create(this.review)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('jdemoApp.review.created', { param: param.id });
          this.$root.$bvToast.toast(message.toString(), {
            toaster: 'b-toaster-top-center',
            title: 'Success',
            variant: 'success',
            solid: true,
            autoHideDelay: 5000,
          });
        });
    }
  }

  public retrieveReview(reviewId): void {
    this.reviewService()
      .find(reviewId)
      .then(res => {
        this.review = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {}
}
